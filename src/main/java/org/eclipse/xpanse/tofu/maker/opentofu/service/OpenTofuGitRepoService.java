/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.tofu.maker.opentofu.service;

import java.io.File;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.xpanse.tofu.maker.async.TaskConfiguration;
import org.eclipse.xpanse.tofu.maker.models.exceptions.GitRepoCloneException;
import org.eclipse.xpanse.tofu.maker.models.exceptions.OpenTofuExecutorException;
import org.eclipse.xpanse.tofu.maker.models.plan.OpenTofuPlan;
import org.eclipse.xpanse.tofu.maker.models.plan.OpenTofuPlanFromGitRepoRequest;
import org.eclipse.xpanse.tofu.maker.models.request.git.OpenTofuAsyncDeployFromGitRepoRequest;
import org.eclipse.xpanse.tofu.maker.models.request.git.OpenTofuAsyncDestroyFromGitRepoRequest;
import org.eclipse.xpanse.tofu.maker.models.request.git.OpenTofuDeployFromGitRepoRequest;
import org.eclipse.xpanse.tofu.maker.models.request.git.OpenTofuDestroyFromGitRepoRequest;
import org.eclipse.xpanse.tofu.maker.models.request.git.OpenTofuScriptGitRepoDetails;
import org.eclipse.xpanse.tofu.maker.models.response.OpenTofuResult;
import org.eclipse.xpanse.tofu.maker.models.validation.OpenTofuValidationResult;
import org.eclipse.xpanse.tofu.maker.opentofu.OpenTofuExecutor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Bean to manage all OpenTofu execution using scripts from a GIT Repo.
 */
@Slf4j
@Component
public class OpenTofuGitRepoService extends OpenTofuDirectoryService {

    private final RestTemplate restTemplate;
    private final OpenTofuExecutor executor;
    private final OpenTofuScriptsHelper openTofuScriptsHelper;

    /**
     * Constructor for OpenTofuGitRepoService bean.
     */
    public OpenTofuGitRepoService(OpenTofuExecutor executor, RestTemplate restTemplate,
                                  OpenTofuScriptsHelper openTofuScriptsHelper) {
        super(executor, restTemplate);
        this.restTemplate = restTemplate;
        this.executor = executor;
        this.openTofuScriptsHelper = openTofuScriptsHelper;
    }

    /**
     * Method of deployment a service using a script.
     */
    public OpenTofuValidationResult validateWithScripts(
            OpenTofuDeployFromGitRepoRequest request) {
        UUID uuid = UUID.randomUUID();
        buildDeployEnv(request.getGitRepoDetails(), uuid);
        return tfValidateFromDirectory(
                getScriptsLocationInRepo(request.getGitRepoDetails(), uuid));
    }

    /**
     * Method to get openTofu plan.
     */
    public OpenTofuPlan getOpenTofuPlanFromGitRepo(OpenTofuPlanFromGitRepoRequest request,
                                                    UUID uuid) {
        buildDeployEnv(request.getGitRepoDetails(), uuid);
        return getOpenTofuPlanFromDirectory(request,
                getScriptsLocationInRepo(request.getGitRepoDetails(), uuid));
    }

    /**
     * Method of deployment a service using a script.
     */
    public OpenTofuResult deployFromGitRepo(OpenTofuDeployFromGitRepoRequest request, UUID uuid) {
        buildDeployEnv(request.getGitRepoDetails(), uuid);
        return deployFromDirectory(request, getScriptsLocationInRepo(
                request.getGitRepoDetails(), uuid));
    }

    /**
     * Method of destroy a service using a script.
     */
    public OpenTofuResult destroyFromGitRepo(OpenTofuDestroyFromGitRepoRequest request,
                                             UUID uuid) {
        buildDestroyEnv(request.getGitRepoDetails(), request.getTfState(), uuid);
        return destroyFromDirectory(request, getScriptsLocationInRepo(
                request.getGitRepoDetails(), uuid));
    }

    /**
     * Async deploy a source by openTofu.
     */
    @Async(TaskConfiguration.TASK_EXECUTOR_NAME)
    public void asyncDeployFromGitRepo(
            OpenTofuAsyncDeployFromGitRepoRequest asyncDeployRequest, UUID uuid) {
        OpenTofuResult result;
        try {
            result = deployFromGitRepo(asyncDeployRequest, uuid);
        } catch (RuntimeException e) {
            result = OpenTofuResult.builder()
                    .commandStdOutput(null)
                    .commandStdError(e.getMessage())
                    .isCommandSuccessful(false)
                    .terraformState(null)
                    .importantFileContentMap(new HashMap<>())
                    .build();
        }
        String url = asyncDeployRequest.getWebhookConfig().getUrl();
        log.info("Deployment service complete, callback POST url:{}, requestBody:{}", url, result);
        restTemplate.postForLocation(url, result);
    }

    /**
     * Async destroy resource of the service.
     */
    @Async(TaskConfiguration.TASK_EXECUTOR_NAME)
    public void asyncDestroyFromGitRepo(OpenTofuAsyncDestroyFromGitRepoRequest request,
                                        UUID uuid) {
        OpenTofuResult result;
        try {
            result = destroyFromGitRepo(request, uuid);
        } catch (RuntimeException e) {
            result = OpenTofuResult.builder()
                    .commandStdOutput(null)
                    .commandStdError(e.getMessage())
                    .isCommandSuccessful(false)
                    .terraformState(null)
                    .importantFileContentMap(new HashMap<>())
                    .build();
        }

        String url = request.getWebhookConfig().getUrl();
        log.info("Destroy service complete, callback POST url:{}, requestBody:{}", url, result);
        restTemplate.postForLocation(url, result);
    }

    private void buildDeployEnv(OpenTofuScriptGitRepoDetails openTofuScriptGitRepoDetails,
                                UUID uuid) {
        String workspace = executor.getModuleFullPath(uuid.toString());
        buildWorkspace(workspace);
        extractScripts(workspace, openTofuScriptGitRepoDetails);
    }

    private void buildWorkspace(String workspace) {
        log.info("start create workspace");
        File ws = new File(workspace);
        if (!ws.exists() && !ws.mkdirs()) {
            throw new OpenTofuExecutorException(
                    "Create workspace failed, File path not created: " + ws.getAbsolutePath());
        }
        log.info("workspace create success, Working directory is " + ws.getAbsolutePath());
    }

    private void extractScripts(String workspace,
                                OpenTofuScriptGitRepoDetails openTofuScriptGitRepoDetails) {
        log.info("Cloning GIT repo");
        try {
            CloneCommand cloneCommand = new CloneCommand();
            cloneCommand.setURI(openTofuScriptGitRepoDetails.getRepoUrl());
            cloneCommand.setProgressMonitor(null);
            cloneCommand.setDirectory(new File(workspace));
            cloneCommand.setBranch(openTofuScriptGitRepoDetails.getBranch());
            cloneCommand.call();
        } catch (GitAPIException e) {
            log.error(e.getMessage(), e);
            throw new GitRepoCloneException(e.getMessage());
        }
    }

    private void buildDestroyEnv(OpenTofuScriptGitRepoDetails openTofuScriptGitRepoDetails,
                                 String tfState, UUID uuid) {
        buildDeployEnv(openTofuScriptGitRepoDetails, uuid);
        openTofuScriptsHelper.createTfStateFile(tfState,
                uuid + File.separator + openTofuScriptGitRepoDetails.getScriptPath());
    }

    private String getScriptsLocationInRepo(
            OpenTofuScriptGitRepoDetails openTofuScriptGitRepoDetails,
            UUID uuid) {
        if (Objects.nonNull(openTofuScriptGitRepoDetails.getScriptPath())) {
            return uuid + File.separator + openTofuScriptGitRepoDetails.getScriptPath();
        }
        return uuid.toString();
    }


}
