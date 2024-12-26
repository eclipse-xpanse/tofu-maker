/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.tofu.maker.opentofu.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.tofu.maker.async.TaskConfiguration;
import org.eclipse.xpanse.tofu.maker.models.OpenTofuMakerSystemStatus;
import org.eclipse.xpanse.tofu.maker.models.enums.HealthStatus;
import org.eclipse.xpanse.tofu.maker.models.exceptions.InvalidOpenTofuToolException;
import org.eclipse.xpanse.tofu.maker.models.exceptions.OpenTofuExecutorException;
import org.eclipse.xpanse.tofu.maker.models.plan.OpenTofuPlan;
import org.eclipse.xpanse.tofu.maker.models.plan.OpenTofuPlanFromDirectoryRequest;
import org.eclipse.xpanse.tofu.maker.models.request.directory.OpenTofuAsyncDeployFromDirectoryRequest;
import org.eclipse.xpanse.tofu.maker.models.request.directory.OpenTofuAsyncDestroyFromDirectoryRequest;
import org.eclipse.xpanse.tofu.maker.models.request.directory.OpenTofuAsyncModifyFromDirectoryRequest;
import org.eclipse.xpanse.tofu.maker.models.request.directory.OpenTofuDeployFromDirectoryRequest;
import org.eclipse.xpanse.tofu.maker.models.request.directory.OpenTofuDestroyFromDirectoryRequest;
import org.eclipse.xpanse.tofu.maker.models.request.directory.OpenTofuModifyFromDirectoryRequest;
import org.eclipse.xpanse.tofu.maker.models.response.OpenTofuResult;
import org.eclipse.xpanse.tofu.maker.models.validation.OpenTofuValidationResult;
import org.eclipse.xpanse.tofu.maker.opentofu.OpenTofuExecutor;
import org.eclipse.xpanse.tofu.maker.opentofu.tool.OpenTofuInstaller;
import org.eclipse.xpanse.tofu.maker.opentofu.tool.OpenTofuVersionsHelper;
import org.eclipse.xpanse.tofu.maker.opentofu.utils.SystemCmdResult;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/** OpenTofu service classes are deployed form Directory. */
@Slf4j
@Service
public class OpenTofuDirectoryService {

    private static final String HELLO_WORLD_TF_NAME = "hello_world.tf";
    private static final String HELLO_WORLD_TEMPLATE =
            """
            output "hello_world" {
                value = "Hello, World!"
            }
            """;
    @Resource private OpenTofuExecutor executor;
    @Resource private OpenTofuInstaller installer;
    @Resource private RestTemplate restTemplate;
    @Resource private OpenTofuVersionsHelper versionHelper;
    @Resource private OpenTofuScriptsHelper scriptsHelper;
    @Resource private OpenTofuResultPersistenceManage openTofuResultPersistenceManage;

    /**
     * Perform OpenTofu health checks by creating a OpenTofu test configuration file.
     *
     * @return OpenTofuBootSystemStatus.
     */
    public OpenTofuMakerSystemStatus tfHealthCheck() {
        String taskWorkspace = scriptsHelper.buildTaskWorkspace(UUID.randomUUID().toString());
        scriptsHelper.prepareDeploymentFilesWithScripts(
                taskWorkspace, Map.of(HELLO_WORLD_TF_NAME, HELLO_WORLD_TEMPLATE), null);
        OpenTofuValidationResult openTofuValidationResult =
                tfValidateFromDirectory(taskWorkspace, null);
        OpenTofuMakerSystemStatus systemStatus = new OpenTofuMakerSystemStatus();
        if (openTofuValidationResult.isValid()) {
            systemStatus.setHealthStatus(HealthStatus.OK);
            return systemStatus;
        }
        scriptsHelper.deleteTaskWorkspace(taskWorkspace);
        systemStatus.setHealthStatus(HealthStatus.NOK);
        return systemStatus;
    }

    /**
     * Executes openTofu validate command.
     *
     * @return TfValidationResult.
     */
    public OpenTofuValidationResult tfValidateFromDirectory(
            String taskWorkspace, String openTofuVersion) {
        try {
            String executorPath =
                    installer.getExecutorPathThatMatchesRequiredVersion(openTofuVersion);
            SystemCmdResult result = executor.tfValidate(executorPath, taskWorkspace);
            OpenTofuValidationResult validationResult =
                    new ObjectMapper()
                            .readValue(
                                    result.getCommandStdOutput(), OpenTofuValidationResult.class);
            validationResult.setOpenTofuVersionUsed(
                    versionHelper.getExactVersionOfExecutor(executorPath));
            return validationResult;
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Serialising string to object failed.", ex);
        }
    }

    /** Deploy a source by openTofu. */
    public OpenTofuResult deployFromDirectory(
            OpenTofuDeployFromDirectoryRequest request,
            String taskWorkspace,
            List<File> scriptFiles) {
        SystemCmdResult result;
        String executorPath = null;
        try {
            executorPath =
                    installer.getExecutorPathThatMatchesRequiredVersion(
                            request.getOpenTofuVersion());
            if (Boolean.TRUE.equals(request.getIsPlanOnly())) {
                result =
                        executor.tfPlan(
                                executorPath,
                                request.getVariables(),
                                request.getEnvVariables(),
                                taskWorkspace);
            } else {
                result =
                        executor.tfApply(
                                executorPath,
                                request.getVariables(),
                                request.getEnvVariables(),
                                taskWorkspace);
            }
        } catch (InvalidOpenTofuToolException | OpenTofuExecutorException tfEx) {
            log.error("OpenTofu deploy service failed. error:{}", tfEx.getMessage());
            result = new SystemCmdResult();
            result.setCommandSuccessful(false);
            result.setCommandStdError(tfEx.getMessage());
        }
        OpenTofuResult openTofuResult =
                transSystemCmdResultToOpenTofuResult(result, taskWorkspace, scriptFiles);
        openTofuResult.setOpenTofuVersionUsed(
                versionHelper.getExactVersionOfExecutor(executorPath));
        scriptsHelper.deleteTaskWorkspace(taskWorkspace);
        return openTofuResult;
    }

    /** Modify a source by openTofu. */
    public OpenTofuResult modifyFromDirectory(
            OpenTofuModifyFromDirectoryRequest request,
            String taskWorkspace,
            List<File> scriptFiles) {
        SystemCmdResult result;
        String executorPath = null;
        try {
            executorPath =
                    installer.getExecutorPathThatMatchesRequiredVersion(
                            request.getOpenTofuVersion());
            if (Boolean.TRUE.equals(request.getIsPlanOnly())) {
                result =
                        executor.tfPlan(
                                executorPath,
                                request.getVariables(),
                                request.getEnvVariables(),
                                taskWorkspace);
            } else {
                result =
                        executor.tfApply(
                                executorPath,
                                request.getVariables(),
                                request.getEnvVariables(),
                                taskWorkspace);
            }
        } catch (InvalidOpenTofuToolException | OpenTofuExecutorException tfEx) {
            log.error("OpenTofu deploy service failed. error:{}", tfEx.getMessage());
            result = new SystemCmdResult();
            result.setCommandSuccessful(false);
            result.setCommandStdError(tfEx.getMessage());
        }
        OpenTofuResult openTofuResult =
                transSystemCmdResultToOpenTofuResult(result, taskWorkspace, scriptFiles);
        openTofuResult.setOpenTofuVersionUsed(
                versionHelper.getExactVersionOfExecutor(executorPath));
        scriptsHelper.deleteTaskWorkspace(taskWorkspace);
        openTofuResult.setRequestId(request.getRequestId());
        return openTofuResult;
    }

    /** Destroy resource of the service. */
    public OpenTofuResult destroyFromDirectory(
            OpenTofuDestroyFromDirectoryRequest request,
            String taskWorkspace,
            List<File> scriptFiles) {
        SystemCmdResult result;
        String executorPath = null;
        try {
            executorPath =
                    installer.getExecutorPathThatMatchesRequiredVersion(
                            request.getOpenTofuVersion());
            result =
                    executor.tfDestroy(
                            executorPath,
                            request.getVariables(),
                            request.getEnvVariables(),
                            taskWorkspace);
        } catch (InvalidOpenTofuToolException | OpenTofuExecutorException tfEx) {
            log.error("OpenTofu destroy service failed. error:{}", tfEx.getMessage());
            result = new SystemCmdResult();
            result.setCommandSuccessful(false);
            result.setCommandStdError(tfEx.getMessage());
        }
        OpenTofuResult openTofuResult =
                transSystemCmdResultToOpenTofuResult(result, taskWorkspace, scriptFiles);
        openTofuResult.setOpenTofuVersionUsed(
                versionHelper.getExactVersionOfExecutor(executorPath));
        scriptsHelper.deleteTaskWorkspace(taskWorkspace);
        openTofuResult.setRequestId(request.getRequestId());
        return openTofuResult;
    }

    /** Executes openTofu plan command on a directory and returns the plan as a JSON string. */
    public OpenTofuPlan getOpenTofuPlanFromDirectory(
            OpenTofuPlanFromDirectoryRequest request, String taskWorkspace) {
        String executorPath =
                installer.getExecutorPathThatMatchesRequiredVersion(request.getOpenTofuVersion());
        String result =
                executor.getOpenTofuPlanAsJson(
                        executorPath,
                        request.getVariables(),
                        request.getEnvVariables(),
                        taskWorkspace);
        scriptsHelper.deleteTaskWorkspace(taskWorkspace);
        OpenTofuPlan openTofuPlan = OpenTofuPlan.builder().plan(result).build();
        openTofuPlan.setOpenTofuVersionUsed(versionHelper.getExactVersionOfExecutor(executorPath));
        return openTofuPlan;
    }

    /** Async deploy a source by openTofu. */
    @Async(TaskConfiguration.TASK_EXECUTOR_NAME)
    public void asyncDeployWithScripts(
            OpenTofuAsyncDeployFromDirectoryRequest asyncDeployRequest,
            String taskWorkspace,
            List<File> scriptFiles) {
        OpenTofuResult result;
        try {
            result = deployFromDirectory(asyncDeployRequest, taskWorkspace, scriptFiles);
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

    /** Async modify a source by openTofu. */
    @Async(TaskConfiguration.TASK_EXECUTOR_NAME)
    public void asyncModifyWithScripts(
            OpenTofuAsyncModifyFromDirectoryRequest asyncModifyRequest,
            String taskWorkspace,
            List<File> scriptFiles) {
        OpenTofuResult result;
        try {
            result = modifyFromDirectory(asyncModifyRequest, taskWorkspace, scriptFiles);
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
        log.info("Deployment service complete, callback POST url:{}, requestBody:{}", url, result);
        sendOpenTofuResult(url, result);
    }

    /** Async destroy resource of the service. */
    @Async(TaskConfiguration.TASK_EXECUTOR_NAME)
    public void asyncDestroyWithScripts(
            OpenTofuAsyncDestroyFromDirectoryRequest request,
            String taskWorkspace,
            List<File> scriptFiles) {
        OpenTofuResult result;
        try {
            result = destroyFromDirectory(request, taskWorkspace, scriptFiles);
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

    private OpenTofuResult transSystemCmdResultToOpenTofuResult(
            SystemCmdResult result, String taskWorkspace, List<File> scriptFiles) {
        OpenTofuResult openTofuResult =
                OpenTofuResult.builder().isCommandSuccessful(result.isCommandSuccessful()).build();
        BeanUtils.copyProperties(result, openTofuResult);
        openTofuResult.setTerraformState(scriptsHelper.getTerraformState(taskWorkspace));
        openTofuResult.setGeneratedFileContentMap(
                scriptsHelper.getDeploymentGeneratedFilesContent(taskWorkspace, scriptFiles));
        return openTofuResult;
    }
}
