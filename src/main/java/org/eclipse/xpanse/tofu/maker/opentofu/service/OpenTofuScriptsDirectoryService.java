/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.tofu.maker.opentofu.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.tofu.maker.async.TaskConfiguration;
import org.eclipse.xpanse.tofu.maker.models.enums.HealthStatus;
import org.eclipse.xpanse.tofu.maker.models.exceptions.InvalidOpenTofuToolException;
import org.eclipse.xpanse.tofu.maker.models.exceptions.OpenTofuExecutorException;
import org.eclipse.xpanse.tofu.maker.models.request.directory.OpenTofuAsyncRequestWithScriptsDirectory;
import org.eclipse.xpanse.tofu.maker.models.request.directory.OpenTofuRequestWithScriptsDirectory;
import org.eclipse.xpanse.tofu.maker.models.response.OpenTofuPlan;
import org.eclipse.xpanse.tofu.maker.models.response.OpenTofuResult;
import org.eclipse.xpanse.tofu.maker.models.response.TofuMakerSystemStatus;
import org.eclipse.xpanse.tofu.maker.models.response.validation.OpenTofuValidationResult;
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
public class OpenTofuScriptsDirectoryService {

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
    @Resource private OpenTofuScriptsDirectoryHelper scriptsHelper;
    @Resource private OpenTofuResultPersistenceManage resultPersistenceManage;

    /**
     * Perform Tofu-Maker health checks by creating a OpenTofu test configuration file.
     *
     * @return TofuMakerSystemStatus.
     */
    public TofuMakerSystemStatus tfHealthCheck(UUID requestId) {
        String taskWorkspace = scriptsHelper.buildTaskWorkspace(requestId.toString());
        scriptsHelper.prepareDeploymentFilesWithScripts(
                taskWorkspace, Map.of(HELLO_WORLD_TF_NAME, HELLO_WORLD_TEMPLATE), null);
        OpenTofuRequestWithScriptsDirectory request = new OpenTofuRequestWithScriptsDirectory();
        request.setScriptsDirectory(taskWorkspace);
        OpenTofuValidationResult tofuValidationResult = tfValidateWithScriptsDirectory(request);
        TofuMakerSystemStatus systemStatus = new TofuMakerSystemStatus();
        systemStatus.setRequestId(requestId);
        if (tofuValidationResult.isValid()) {
            systemStatus.setHealthStatus(HealthStatus.OK);
            return systemStatus;
        }
        scriptsHelper.deleteTaskWorkspace(taskWorkspace);
        systemStatus.setHealthStatus(HealthStatus.NOK);
        return systemStatus;
    }

    /**
     * Executes open tofu validate command.
     *
     * @return TfValidationResult.
     */
    public OpenTofuValidationResult tfValidateWithScriptsDirectory(
            OpenTofuRequestWithScriptsDirectory request) {
        try {
            String executorPath =
                    installer.getExecutorPathThatMatchesRequiredVersion(
                            request.getOpenTofuVersion());
            SystemCmdResult result =
                    executor.tfValidate(executorPath, request.getScriptsDirectory());
            OpenTofuValidationResult validationResult =
                    new ObjectMapper()
                            .readValue(
                                    result.getCommandStdOutput(), OpenTofuValidationResult.class);
            validationResult.setRequestId(request.getRequestId());
            validationResult.setOpenTofuVersionUsed(
                    versionHelper.getExactVersionOfExecutor(executorPath));
            return validationResult;
        } catch (JsonProcessingException | InvalidOpenTofuToolException ex) {
            throw new OpenTofuExecutorException("Failed get open tofu validation result.", ex);
        }
    }

    /** Deploy a source by open tofu. */
    public OpenTofuResult deployWithScriptsDirectory(OpenTofuRequestWithScriptsDirectory request) {
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
                                request.getScriptsDirectory());
            } else {
                result =
                        executor.tfApply(
                                executorPath,
                                request.getVariables(),
                                request.getEnvVariables(),
                                request.getScriptsDirectory());
            }
        } catch (InvalidOpenTofuToolException | OpenTofuExecutorException tfEx) {
            log.error("OpenTofu deploy service failed. error:{}", tfEx.getMessage());
            result = new SystemCmdResult();
            result.setCommandSuccessful(false);
            result.setCommandStdError(tfEx.getMessage());
        }
        OpenTofuResult tofuResult = transSystemCmdResultToOpenTofuResult(result, request);
        tofuResult.setOpenTofuVersionUsed(versionHelper.getExactVersionOfExecutor(executorPath));
        scriptsHelper.deleteTaskWorkspace(request.getScriptsDirectory());
        return tofuResult;
    }

    /** Modify a source by open tofu. */
    public OpenTofuResult modifyWithScriptsDirectory(OpenTofuRequestWithScriptsDirectory request) {
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
                                request.getScriptsDirectory());
            } else {
                result =
                        executor.tfApply(
                                executorPath,
                                request.getVariables(),
                                request.getEnvVariables(),
                                request.getScriptsDirectory());
            }
        } catch (InvalidOpenTofuToolException | OpenTofuExecutorException tfEx) {
            log.error("OpenTofu deploy service failed. error:{}", tfEx.getMessage());
            result = new SystemCmdResult();
            result.setCommandSuccessful(false);
            result.setCommandStdError(tfEx.getMessage());
        }
        OpenTofuResult tofuResult = transSystemCmdResultToOpenTofuResult(result, request);
        tofuResult.setOpenTofuVersionUsed(versionHelper.getExactVersionOfExecutor(executorPath));
        scriptsHelper.deleteTaskWorkspace(request.getScriptsDirectory());
        tofuResult.setRequestId(request.getRequestId());
        return tofuResult;
    }

    /** Destroy resource of the service. */
    public OpenTofuResult destroyWithScriptsDirectory(OpenTofuRequestWithScriptsDirectory request) {
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
                            request.getScriptsDirectory());
        } catch (InvalidOpenTofuToolException | OpenTofuExecutorException tfEx) {
            log.error("OpenTofu destroy service failed. error:{}", tfEx.getMessage());
            result = new SystemCmdResult();
            result.setCommandSuccessful(false);
            result.setCommandStdError(tfEx.getMessage());
        }
        OpenTofuResult tofuResult = transSystemCmdResultToOpenTofuResult(result, request);
        tofuResult.setOpenTofuVersionUsed(versionHelper.getExactVersionOfExecutor(executorPath));
        scriptsHelper.deleteTaskWorkspace(request.getScriptsDirectory());
        tofuResult.setRequestId(request.getRequestId());
        return tofuResult;
    }

    /** Executes open tofu plan command on a directory and returns the plan as a JSON string. */
    public OpenTofuPlan getOpenTofuPlanWithScriptsDirectory(
            OpenTofuRequestWithScriptsDirectory request) {
        String executorPath =
                installer.getExecutorPathThatMatchesRequiredVersion(request.getOpenTofuVersion());
        String result =
                executor.getOpenTofuPlanAsJson(
                        executorPath,
                        request.getVariables(),
                        request.getEnvVariables(),
                        request.getScriptsDirectory());
        scriptsHelper.deleteTaskWorkspace(request.getScriptsDirectory());
        OpenTofuPlan tofuPlan =
                OpenTofuPlan.builder().plan(result).requestId(request.getRequestId()).build();
        tofuPlan.setOpenTofuVersionUsed(versionHelper.getExactVersionOfExecutor(executorPath));
        return tofuPlan;
    }

    /** Async deploy a source by open tofu. */
    @Async(TaskConfiguration.TASK_EXECUTOR_NAME)
    public void asyncDeployWithScriptsDirectory(
            OpenTofuAsyncRequestWithScriptsDirectory asyncDeployRequest) {
        OpenTofuResult result;
        try {
            result = deployWithScriptsDirectory(asyncDeployRequest);
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

    /** Async modify a source by open tofu. */
    @Async(TaskConfiguration.TASK_EXECUTOR_NAME)
    public void asyncModifyWithScriptsDirectory(
            OpenTofuAsyncRequestWithScriptsDirectory asyncModifyRequest) {
        OpenTofuResult result;
        try {
            result = modifyWithScriptsDirectory(asyncModifyRequest);
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
    public void asyncDestroyWithScriptsDirectory(OpenTofuAsyncRequestWithScriptsDirectory request) {
        OpenTofuResult result;
        try {
            result = destroyWithScriptsDirectory(request);
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
            log.error("error while sending open tofu result", e);
            resultPersistenceManage.persistOpenTofuResult(result);
        }
    }

    private OpenTofuResult transSystemCmdResultToOpenTofuResult(
            SystemCmdResult result, OpenTofuRequestWithScriptsDirectory request) {
        OpenTofuResult tofuResult =
                OpenTofuResult.builder()
                        .isCommandSuccessful(result.isCommandSuccessful())
                        .requestId(request.getRequestId())
                        .build();
        try {
            BeanUtils.copyProperties(result, tofuResult);
            tofuResult.setTerraformState(
                    scriptsHelper.getTerraformState(request.getScriptsDirectory()));
            tofuResult.setGeneratedFileContentMap(
                    scriptsHelper.getDeploymentGeneratedFilesContent(
                            request.getScriptsDirectory(), request.getScriptFiles()));
        } catch (Exception e) {
            log.error("Failed to get open tofu state and generated files content.", e);
        }
        return tofuResult;
    }
}
