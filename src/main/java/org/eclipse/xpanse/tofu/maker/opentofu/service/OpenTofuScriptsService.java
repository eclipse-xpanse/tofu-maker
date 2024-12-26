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
import org.eclipse.xpanse.tofu.maker.async.TaskConfiguration;
import org.eclipse.xpanse.tofu.maker.models.plan.OpenTofuPlan;
import org.eclipse.xpanse.tofu.maker.models.plan.OpenTofuPlanWithScriptsRequest;
import org.eclipse.xpanse.tofu.maker.models.request.scripts.OpenTofuAsyncDeployFromScriptsRequest;
import org.eclipse.xpanse.tofu.maker.models.request.scripts.OpenTofuAsyncDestroyFromScriptsRequest;
import org.eclipse.xpanse.tofu.maker.models.request.scripts.OpenTofuAsyncModifyFromScriptsRequest;
import org.eclipse.xpanse.tofu.maker.models.request.scripts.OpenTofuDeployWithScriptsRequest;
import org.eclipse.xpanse.tofu.maker.models.request.scripts.OpenTofuDestroyWithScriptsRequest;
import org.eclipse.xpanse.tofu.maker.models.request.scripts.OpenTofuModifyWithScriptsRequest;
import org.eclipse.xpanse.tofu.maker.models.response.OpenTofuResult;
import org.eclipse.xpanse.tofu.maker.models.validation.OpenTofuValidationResult;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/** OpenTofu service classes are deployed form Scripts. */
@Slf4j
@Service
public class OpenTofuScriptsService {

    @Resource private RestTemplate restTemplate;
    @Resource private OpenTofuScriptsHelper scriptsHelper;
    @Resource private OpenTofuDirectoryService directoryService;
    @Resource private OpenTofuResultPersistenceManage openTofuResultPersistenceManage;

    /** /** Method of deployment a service using a script. */
    public OpenTofuValidationResult validateWithScripts(OpenTofuDeployWithScriptsRequest request) {
        String taskWorkspace = scriptsHelper.buildTaskWorkspace(UUID.randomUUID().toString());
        scriptsHelper.prepareDeploymentFilesWithScripts(
                taskWorkspace, request.getScriptFiles(), null);
        return directoryService.tfValidateFromDirectory(
                taskWorkspace, request.getOpenTofuVersion());
    }

    /** Method of deployment a service using a script. */
    public OpenTofuResult deployWithScripts(OpenTofuDeployWithScriptsRequest request, UUID uuid) {
        String taskWorkspace = scriptsHelper.buildTaskWorkspace(uuid.toString());
        List<File> files =
                scriptsHelper.prepareDeploymentFilesWithScripts(
                        taskWorkspace, request.getScriptFiles(), null);
        return directoryService.deployFromDirectory(request, taskWorkspace, files);
    }

    /** Method of modify a service using a script. */
    public OpenTofuResult modifyWithScripts(OpenTofuModifyWithScriptsRequest request, UUID uuid) {
        String taskWorkspace = scriptsHelper.buildTaskWorkspace(uuid.toString());
        List<File> files =
                scriptsHelper.prepareDeploymentFilesWithScripts(
                        taskWorkspace, request.getScriptFiles(), request.getTfState());
        return directoryService.modifyFromDirectory(request, taskWorkspace, files);
    }

    /** Method of destroy a service using a script. */
    public OpenTofuResult destroyWithScripts(OpenTofuDestroyWithScriptsRequest request, UUID uuid) {
        String taskWorkspace = scriptsHelper.buildTaskWorkspace(uuid.toString());
        List<File> files =
                scriptsHelper.prepareDeploymentFilesWithScripts(
                        taskWorkspace, request.getScriptFiles(), request.getTfState());
        return directoryService.destroyFromDirectory(request, taskWorkspace, files);
    }

    /** Method to get terraform plan. */
    public OpenTofuPlan getOpenTofuPlanFromScripts(
            OpenTofuPlanWithScriptsRequest request, UUID uuid) {
        String taskWorkspace = scriptsHelper.buildTaskWorkspace(uuid.toString());
        scriptsHelper.prepareDeploymentFilesWithScripts(
                taskWorkspace, request.getScriptFiles(), null);
        return directoryService.getOpenTofuPlanFromDirectory(request, uuid.toString());
    }

    /** Async deploy a source by terraform. */
    @Async(TaskConfiguration.TASK_EXECUTOR_NAME)
    public void asyncDeployWithScripts(
            OpenTofuAsyncDeployFromScriptsRequest asyncDeployRequest, UUID uuid) {
        OpenTofuResult result;
        try {
            result = deployWithScripts(asyncDeployRequest, uuid);
        } catch (RuntimeException e) {
            result =
                    OpenTofuResult.builder()
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
        sendOpenTofuResult(url, result);
    }

    /** Async modify a source by terraform. */
    @Async(TaskConfiguration.TASK_EXECUTOR_NAME)
    public void asyncModifyWithScripts(
            OpenTofuAsyncModifyFromScriptsRequest asyncModifyRequest, UUID uuid) {
        OpenTofuResult result;
        try {
            result = modifyWithScripts(asyncModifyRequest, uuid);
        } catch (RuntimeException e) {
            result =
                    OpenTofuResult.builder()
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
        sendOpenTofuResult(url, result);
    }

    /** Async destroy resource of the service. */
    @Async(TaskConfiguration.TASK_EXECUTOR_NAME)
    public void asyncDestroyWithScripts(OpenTofuAsyncDestroyFromScriptsRequest request, UUID uuid) {
        OpenTofuResult result;
        try {
            result = destroyWithScripts(request, uuid);
        } catch (RuntimeException e) {
            result =
                    OpenTofuResult.builder()
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
        sendOpenTofuResult(url, result);
    }

    private void sendOpenTofuResult(String url, OpenTofuResult result) {
        try {
            restTemplate.postForLocation(url, result);
        } catch (RestClientException e) {
            openTofuResultPersistenceManage.persistOpenTofuResult(result);
        }
    }
}
