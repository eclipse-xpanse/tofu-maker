/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.tofu.maker.opentofu.service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.tofu.maker.async.TaskConfiguration;
import org.eclipse.xpanse.tofu.maker.models.exceptions.OpenTofuExecutorException;
import org.eclipse.xpanse.tofu.maker.models.plan.OpenTofuPlan;
import org.eclipse.xpanse.tofu.maker.models.plan.OpenTofuPlanWithScriptsRequest;
import org.eclipse.xpanse.tofu.maker.models.request.scripts.OpenTofuAsyncDeployFromScriptsRequest;
import org.eclipse.xpanse.tofu.maker.models.request.scripts.OpenTofuAsyncDestroyFromScriptsRequest;
import org.eclipse.xpanse.tofu.maker.models.request.scripts.OpenTofuDeployWithScriptsRequest;
import org.eclipse.xpanse.tofu.maker.models.request.scripts.OpenTofuDestroyWithScriptsRequest;
import org.eclipse.xpanse.tofu.maker.models.response.OpenTofuResult;
import org.eclipse.xpanse.tofu.maker.models.validation.OpenTofuValidationResult;
import org.eclipse.xpanse.tofu.maker.opentofu.OpenTofuExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

/**
 * OpenTofu service classes are deployed form Scripts.
 */
@Slf4j
@Service
public class OpenTofuScriptsService extends OpenTofuDirectoryService {

    private static final String FILE_SUFFIX = ".tf";
    private final RestTemplate restTemplate;
    private final OpenTofuExecutor executor;
    private final OpenTofuScriptsHelper openTofuScriptsHelper;

    /**
     * OpenTofuScriptsService constructor.
     */
    @Autowired
    public OpenTofuScriptsService(OpenTofuExecutor executor, RestTemplate restTemplate,
                                  OpenTofuScriptsHelper openTofuScriptsHelper) {
        super(executor, restTemplate);
        this.executor = executor;
        this.restTemplate = restTemplate;

        this.openTofuScriptsHelper = openTofuScriptsHelper;
    }

    /**
     * Method of deployment a service using a script.
     */
    public OpenTofuValidationResult validateWithScripts(
            OpenTofuDeployWithScriptsRequest request) {
        UUID uuid = UUID.randomUUID();
        buildDeployEnv(request.getScripts(), uuid);
        return tfValidateFromDirectory(uuid.toString());
    }

    /**
     * Method of deployment a service using a script.
     */
    public OpenTofuResult deployWithScripts(OpenTofuDeployWithScriptsRequest request, UUID uuid) {
        buildDeployEnv(request.getScripts(), uuid);
        return deployFromDirectory(request, uuid.toString());
    }

    /**
     * Method of destroy a service using a script.
     */
    public OpenTofuResult destroyWithScripts(OpenTofuDestroyWithScriptsRequest request,
                                             UUID uuid) {
        buildDestroyEnv(request.getScripts(), request.getTfState(), uuid);
        return destroyFromDirectory(request, uuid.toString());
    }

    /**
     * Method to get OpenTofu plan.
     */
    public OpenTofuPlan getOpenTofuPlanFromScripts(OpenTofuPlanWithScriptsRequest request,
                                                    UUID uuid) {
        buildDeployEnv(request.getScripts(), uuid);
        return getOpenTofuPlanFromDirectory(request, uuid.toString());
    }

    /**
     * Async deploy a source by OpenTofu.
     */
    @Async(TaskConfiguration.TASK_EXECUTOR_NAME)
    public void asyncDeployWithScripts(
            OpenTofuAsyncDeployFromScriptsRequest asyncDeployRequest, UUID uuid) {
        OpenTofuResult result;
        try {
            result = deployWithScripts(asyncDeployRequest, uuid);
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
    public void asyncDestroyWithScripts(OpenTofuAsyncDestroyFromScriptsRequest request,
                                        UUID uuid) {
        OpenTofuResult result;
        try {
            result = destroyWithScripts(request, uuid);
        } catch (RuntimeException e) {
            result = OpenTofuResult.builder()
                    .destroyScenario(request.getDestroyScenario())
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

    private void buildDeployEnv(List<String> scripts, UUID uuid) {
        String workspace = executor.getModuleFullPath(uuid.toString());
        buildWorkspace(workspace);
        buildScriptFiles(workspace, uuid, scripts);
    }

    private void buildDestroyEnv(List<String> scripts, String tfState, UUID uuid) {
        buildDeployEnv(scripts, uuid);
        openTofuScriptsHelper.createTfStateFile(tfState, uuid.toString());
    }

    private void buildWorkspace(String workspace) {
        log.info("start create workspace");
        File ws = new File(workspace);
        if (!ws.exists() && !ws.mkdirs()) {
            throw new OpenTofuExecutorException(
                    "Create workspace failed, File path not created: " + ws.getAbsolutePath());
        }
        log.info("workspace create success,Working directory is " + ws.getAbsolutePath());
    }

    private void buildScriptFiles(String workspace, UUID uuid, List<String> scripts) {
        log.info("start build OpenTofu script");
        if (CollectionUtils.isEmpty(scripts)) {
            throw new OpenTofuExecutorException("OpenTofu scripts create error, OpenTofu "
                    + "scripts not exists");
        }
        StringBuilder scriptBuilder = new StringBuilder();
        for (String script : scripts) {
            scriptBuilder.append(script).append(System.lineSeparator());
        }
        String fileName = workspace + File.separator + uuid + FILE_SUFFIX;
        boolean overwrite = new File(fileName).exists();
        try (FileWriter scriptWriter = new FileWriter(fileName, overwrite)) {
            scriptWriter.write(scriptBuilder.toString());
            log.info("OpenTofu script create success, fileName: {}", fileName);
        } catch (IOException ex) {
            log.error("OpenTofu script create failed.", ex);
            throw new OpenTofuExecutorException("OpenTofu script create failed.", ex);
        }
    }
}