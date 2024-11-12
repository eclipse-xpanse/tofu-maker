/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.tofu.maker.opentofu.service;

import jakarta.annotation.Resource;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.tofu.maker.async.TaskConfiguration;
import org.eclipse.xpanse.tofu.maker.models.plan.OpenTofuPlan;
import org.eclipse.xpanse.tofu.maker.models.plan.OpenTofuPlanFromGitRepoRequest;
import org.eclipse.xpanse.tofu.maker.models.request.git.OpenTofuAsyncDeployFromGitRepoRequest;
import org.eclipse.xpanse.tofu.maker.models.request.git.OpenTofuAsyncDestroyFromGitRepoRequest;
import org.eclipse.xpanse.tofu.maker.models.request.git.OpenTofuAsyncModifyFromGitRepoRequest;
import org.eclipse.xpanse.tofu.maker.models.request.git.OpenTofuDeployFromGitRepoRequest;
import org.eclipse.xpanse.tofu.maker.models.request.git.OpenTofuDestroyFromGitRepoRequest;
import org.eclipse.xpanse.tofu.maker.models.request.git.OpenTofuModifyFromGitRepoRequest;
import org.eclipse.xpanse.tofu.maker.models.request.git.OpenTofuScriptGitRepoDetails;
import org.eclipse.xpanse.tofu.maker.models.response.OpenTofuResult;
import org.eclipse.xpanse.tofu.maker.models.validation.OpenTofuValidationResult;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Bean to manage all OpenTofu execution using scripts from a GIT Repo.
 */
@Slf4j
@Component
public class OpenTofuGitRepoService {

    @Resource
    private RestTemplate restTemplate;
    @Resource
    private OpenTofuScriptsHelper scriptsHelper;
    @Resource
    private OpenTofuDirectoryService directoryService;

    /**
     * Method of deployment a service using a script.
     */
    public OpenTofuValidationResult validateWithScripts(
            OpenTofuDeployFromGitRepoRequest request) {
        String taskWorkspace = scriptsHelper.buildTaskWorkspace(UUID.randomUUID().toString());
        scriptsHelper.prepareDeploymentFilesWithGitRepo(
                taskWorkspace, request.getGitRepoDetails(), null);
        String scriptsPath = getScriptsLocationInTaskWorkspace(
                request.getGitRepoDetails(), taskWorkspace);
        return directoryService.tfValidateFromDirectory(scriptsPath, request.getOpenTofuVersion());
    }

    /**
     * Method to get terraform plan.
     */
    public OpenTofuPlan getOpenTofuPlanFromGitRepo(
            OpenTofuPlanFromGitRepoRequest request, UUID uuid) {
        String taskWorkspace = scriptsHelper.buildTaskWorkspace(uuid.toString());
        scriptsHelper.prepareDeploymentFilesWithGitRepo(
                taskWorkspace, request.getGitRepoDetails(), null);
        String scriptsPath = getScriptsLocationInTaskWorkspace(
                request.getGitRepoDetails(), taskWorkspace);
        return directoryService.getOpenTofuPlanFromDirectory(request, scriptsPath);
    }

    /**
     * Method of deployment a service using a script.
     */
    public OpenTofuResult deployFromGitRepo(OpenTofuDeployFromGitRepoRequest request, UUID uuid) {
        String taskWorkspace = scriptsHelper.buildTaskWorkspace(uuid.toString());
        List<File> scriptFiles = scriptsHelper.prepareDeploymentFilesWithGitRepo(
                taskWorkspace, request.getGitRepoDetails(), null);
        String scriptsPath = getScriptsLocationInTaskWorkspace(
                request.getGitRepoDetails(), taskWorkspace);
        OpenTofuResult result =
                directoryService.deployFromDirectory(request, scriptsPath, scriptFiles);
        scriptsHelper.deleteTaskWorkspace(taskWorkspace);
        return result;
    }

    /**
     * Method of modify a service using a script.
     */
    public OpenTofuResult modifyFromGitRepo(OpenTofuModifyFromGitRepoRequest request, UUID uuid) {
        String taskWorkspace = scriptsHelper.buildTaskWorkspace(uuid.toString());
        List<File> scriptFiles = scriptsHelper.prepareDeploymentFilesWithGitRepo(
                taskWorkspace, request.getGitRepoDetails(), request.getTfState());
        String scriptsPath = getScriptsLocationInTaskWorkspace(
                request.getGitRepoDetails(), taskWorkspace);
        OpenTofuResult result =
                directoryService.modifyFromDirectory(request, scriptsPath, scriptFiles);
        scriptsHelper.deleteTaskWorkspace(taskWorkspace);
        return result;
    }

    /**
     * Method of destroy a service using a script.
     */
    public OpenTofuResult destroyFromGitRepo(
            OpenTofuDestroyFromGitRepoRequest request, UUID uuid) {
        String taskWorkspace = scriptsHelper.buildTaskWorkspace(uuid.toString());
        List<File> scriptFiles = scriptsHelper.prepareDeploymentFilesWithGitRepo(
                taskWorkspace, request.getGitRepoDetails(), request.getTfState());
        String scriptsPath = getScriptsLocationInTaskWorkspace(
                request.getGitRepoDetails(), taskWorkspace);
        OpenTofuResult result =
                directoryService.destroyFromDirectory(request, scriptsPath, scriptFiles);
        scriptsHelper.deleteTaskWorkspace(taskWorkspace);
        return result;
    }

    /**
     * Async deploy a source by terraform.
     */
    @Async(TaskConfiguration.TASK_EXECUTOR_NAME)
    public void asyncDeployFromGitRepo(OpenTofuAsyncDeployFromGitRepoRequest asyncDeployRequest,
                                       UUID uuid) {
        OpenTofuResult result;
        try {
            result = deployFromGitRepo(asyncDeployRequest, uuid);
        } catch (RuntimeException e) {
            result = OpenTofuResult.builder()
                    .commandStdOutput(null)
                    .commandStdError(e.getMessage())
                    .isCommandSuccessful(false)
                    .terraformState(null)
                    .generatedFileContentMap(new HashMap<>())
                    .build();
        }
        result.setRequestId(asyncDeployRequest.getRequestId());
        String url = asyncDeployRequest.getWebhookConfig().getUrl();
        log.info("Deployment service complete, callback POST url:{}, requestBody:{}", url, result);
        restTemplate.postForLocation(url, result);
    }

    /**
     * Async modify a source by terraform.
     */
    @Async(TaskConfiguration.TASK_EXECUTOR_NAME)
    public void asyncModifyFromGitRepo(OpenTofuAsyncModifyFromGitRepoRequest asyncModifyRequest,
                                       UUID uuid) {
        OpenTofuResult result;
        try {
            result = modifyFromGitRepo(asyncModifyRequest, uuid);
        } catch (RuntimeException e) {
            result = OpenTofuResult.builder()
                    .commandStdOutput(null)
                    .commandStdError(e.getMessage())
                    .isCommandSuccessful(false)
                    .terraformState(null)
                    .generatedFileContentMap(new HashMap<>())
                    .build();
        }
        result.setRequestId(asyncModifyRequest.getRequestId());
        String url = asyncModifyRequest.getWebhookConfig().getUrl();
        log.info("Modify service complete, callback POST url:{}, requestBody:{}", url, result);
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
                    .generatedFileContentMap(new HashMap<>())
                    .build();
        }
        result.setRequestId(request.getRequestId());
        String url = request.getWebhookConfig().getUrl();
        log.info("Destroy service complete, callback POST url:{}, requestBody:{}", url, result);
        restTemplate.postForLocation(url, result);
    }


    private String getScriptsLocationInTaskWorkspace(
            OpenTofuScriptGitRepoDetails terraformScriptGitRepoDetails, String taskWorkSpace) {
        if (StringUtils.isNotBlank(terraformScriptGitRepoDetails.getScriptPath())) {
            return taskWorkSpace + File.separator + terraformScriptGitRepoDetails.getScriptPath();
        }
        return taskWorkSpace;
    }

}
