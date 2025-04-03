/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.tofu.maker.opentofu.service;

import static org.eclipse.xpanse.tofu.maker.logging.CustomRequestIdGenerator.REQUEST_ID;

import jakarta.annotation.Resource;
import java.io.File;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.tofu.maker.models.enums.RequestType;
import org.eclipse.xpanse.tofu.maker.models.exceptions.InvalidOpenTofuRequestException;
import org.eclipse.xpanse.tofu.maker.models.exceptions.UnsupportedEnumValueException;
import org.eclipse.xpanse.tofu.maker.models.request.OpenTofuRequest;
import org.eclipse.xpanse.tofu.maker.models.request.directory.OpenTofuAsyncRequestWithScriptsDirectory;
import org.eclipse.xpanse.tofu.maker.models.request.directory.OpenTofuRequestWithScriptsDirectory;
import org.eclipse.xpanse.tofu.maker.models.request.git.OpenTofuAsyncRequestWithScriptsGitRepo;
import org.eclipse.xpanse.tofu.maker.models.request.git.OpenTofuRequestWithScriptsGitRepo;
import org.eclipse.xpanse.tofu.maker.models.request.git.OpenTofuScriptsGitRepoDetails;
import org.eclipse.xpanse.tofu.maker.models.request.scripts.OpenTofuAsyncRequestWithScripts;
import org.eclipse.xpanse.tofu.maker.models.request.scripts.OpenTofuRequestWithScripts;
import org.eclipse.xpanse.tofu.maker.models.response.OpenTofuPlan;
import org.eclipse.xpanse.tofu.maker.models.response.OpenTofuResult;
import org.eclipse.xpanse.tofu.maker.models.response.TofuMakerSystemStatus;
import org.eclipse.xpanse.tofu.maker.models.response.validation.OpenTofuValidateDiagnostics;
import org.eclipse.xpanse.tofu.maker.models.response.validation.OpenTofuValidationResult;
import org.slf4j.MDC;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

/** OpenTofu service classes are deployed form Directory. */
@Slf4j
@Service
public class OpenTofuRequestService {

    @Resource private OpenTofuScriptsDirectoryService scriptsDirectoryService;
    @Resource private OpenTofuScriptsDirectoryHelper scriptsDirectoryHelper;

    /** Handle the request of health check. */
    public TofuMakerSystemStatus healthCheck(UUID requestId) {
        MDC.put(REQUEST_ID, requestId.toString());
        return scriptsDirectoryService.tfHealthCheck(requestId);
    }

    /**
     * Handle OpenTofu validate request and return result.
     *
     * @param request request.
     * @return OpenTofuValidationResult.
     */
    public OpenTofuValidationResult handleOpenTofuValidateRequest(OpenTofuRequest request) {
        OpenTofuRequestWithScriptsDirectory requestWithDirectory =
                convertRequestWithScriptsDirectory(request);
        return scriptsDirectoryService.tfValidateWithScriptsDirectory(requestWithDirectory);
    }

    /**
     * Handle the plan request and return the OpenTofuPlan.
     *
     * @param request request.
     * @return OpenTofuPlan.
     */
    public OpenTofuPlan handleOpenTofuPlanRequest(OpenTofuRequest request) {
        OpenTofuRequestWithScriptsDirectory requestWithDirectory =
                convertRequestWithScriptsDirectory(request);
        return scriptsDirectoryService.getOpenTofuPlanWithScriptsDirectory(requestWithDirectory);
    }

    /**
     * Handle the OpenTofu request and return the OpenTofuResult.
     *
     * @param request request.
     * @return OpenTofuResult.
     */
    public OpenTofuResult handleOpenTofuDeploymentRequest(OpenTofuRequest request) {
        OpenTofuRequestWithScriptsDirectory requestWithDirectory =
                convertRequestWithScriptsDirectory(request);
        switch (request.getRequestType()) {
            case RequestType.DEPLOY -> {
                return scriptsDirectoryService.deployWithScriptsDirectory(requestWithDirectory);
            }
            case RequestType.MODIFY -> {
                return scriptsDirectoryService.modifyWithScriptsDirectory(requestWithDirectory);
            }
            case RequestType.DESTROY -> {
                return scriptsDirectoryService.destroyWithScriptsDirectory(requestWithDirectory);
            }
            default ->
                    throw new UnsupportedEnumValueException(
                            String.format(
                                    "RequestType value %s is not supported.",
                                    request.getRequestType().toValue()));
        }
    }

    /**
     * Process the async deployment request.
     *
     * @param request request.
     */
    public void processAsyncDeploymentRequest(OpenTofuRequest request) {
        OpenTofuAsyncRequestWithScriptsDirectory requestWithDirectory =
                (OpenTofuAsyncRequestWithScriptsDirectory)
                        convertRequestWithScriptsDirectory(request);
        switch (request.getRequestType()) {
            case RequestType.DEPLOY ->
                    scriptsDirectoryService.asyncDeployWithScriptsDirectory(requestWithDirectory);
            case RequestType.MODIFY ->
                    scriptsDirectoryService.asyncModifyWithScriptsDirectory(requestWithDirectory);
            case RequestType.DESTROY ->
                    scriptsDirectoryService.asyncDestroyWithScriptsDirectory(requestWithDirectory);
            default ->
                    throw new UnsupportedEnumValueException(
                            String.format(
                                    "RequestType value %s is not supported.",
                                    request.getRequestType().toValue()));
        }
    }

    private OpenTofuRequestWithScriptsDirectory convertRequestWithScriptsDirectory(
            OpenTofuRequest request) {
        validateOpenTofuRequest(request);
        return switch (request) {
            case OpenTofuRequestWithScriptsDirectory requestWithDirectory -> requestWithDirectory;
            case OpenTofuRequestWithScriptsGitRepo requestWithScriptsGitRepo ->
                    convertRequestWithGitToDirectory(requestWithScriptsGitRepo);
            case OpenTofuRequestWithScripts requestWithScripts ->
                    convertRequestWithScriptsToDirectory(requestWithScripts);
            default ->
                    throw new UnsupportedEnumValueException(
                            String.format(
                                    "RequestType value %s is not supported.",
                                    request.getRequestType().toValue()));
        };
    }

    /**
     * Validate the OpenTofu request.
     *
     * @param request request.
     */
    private void validateOpenTofuRequest(OpenTofuRequest request) {
        MDC.put(REQUEST_ID, request.getRequestId().toString());
        if (RequestType.DESTROY == request.getRequestType()
                || RequestType.MODIFY == request.getRequestType()) {
            if (StringUtils.isBlank(request.getTfState())) {
                String errorMessage =
                        String.format(
                                "OpenTofu state is required for request with order type %s.",
                                request.getRequestType());
                log.error(errorMessage);
                throw new InvalidOpenTofuRequestException(errorMessage);
            }
        }
        if (request instanceof OpenTofuRequestWithScriptsDirectory requestWithDirectory) {
            List<File> scriptFiles =
                    scriptsDirectoryHelper.getDeploymentFilesFromTaskWorkspace(
                            requestWithDirectory.getScriptsDirectory());
            if (CollectionUtils.isEmpty(scriptFiles)) {
                String errorMessage =
                        String.format(
                                "No OpenTofu scripts files found in the directory %s.",
                                requestWithDirectory.getScriptsDirectory());
                log.error(errorMessage);
                throw new InvalidOpenTofuRequestException(errorMessage);
            }
            requestWithDirectory.setScriptFiles(scriptFiles);
        }
    }

    /**
     * Get the error validate result.
     *
     * @param request request
     * @param e exception
     * @return OpenTofuValidationResult
     */
    public OpenTofuValidationResult getErrorValidateResult(OpenTofuRequest request, Exception e) {
        OpenTofuValidationResult result = new OpenTofuValidationResult();
        result.setRequestId(request.getRequestId());
        result.setValid(false);
        result.setOpenTofuVersionUsed(request.getOpenTofuVersion());
        OpenTofuValidateDiagnostics diagnostics = new OpenTofuValidateDiagnostics();
        diagnostics.setDetail(e.getMessage());
        result.setDiagnostics(List.of(diagnostics));
        return result;
    }

    /**
     * Get the error plan result.
     *
     * @param request request
     * @param e exception
     * @return OpenTofuPlan
     */
    public OpenTofuPlan getErrorPlanResult(OpenTofuRequest request, Exception e) {
        return OpenTofuPlan.builder()
                .requestId(request.getRequestId())
                .openTofuVersionUsed(request.getOpenTofuVersion())
                .errorMessage(e.getMessage())
                .build();
    }

    /**
     * Get the error deployment result.
     *
     * @param request request
     * @param e exception
     * @return OpenTofuResult
     */
    public OpenTofuResult getErrorDeploymentResult(OpenTofuRequest request, Exception e) {
        return OpenTofuResult.builder()
                .requestId(request.getRequestId())
                .openTofuVersionUsed(request.getOpenTofuVersion())
                .isCommandSuccessful(false)
                .commandStdError(e.getMessage())
                .build();
    }

    /**
     * Transform OpenTofuRequestWithScriptsGitRepo to OpenTofuRequestWithScriptsDirectory.
     *
     * @param request request with git repo.
     * @return request with scripts directory.
     */
    private OpenTofuRequestWithScriptsDirectory convertRequestWithGitToDirectory(
            OpenTofuRequestWithScriptsGitRepo request) {
        OpenTofuRequestWithScriptsDirectory requestWithDirectory =
                new OpenTofuRequestWithScriptsDirectory();
        if (request instanceof OpenTofuAsyncRequestWithScriptsGitRepo) {
            requestWithDirectory = new OpenTofuAsyncRequestWithScriptsDirectory();
        }
        BeanUtils.copyProperties(request, requestWithDirectory);
        String taskWorkspace =
                scriptsDirectoryHelper.buildTaskWorkspace(request.getRequestId().toString());
        String scriptsPath =
                getScriptsLocationInTaskWorkspace(request.getGitRepoDetails(), taskWorkspace);
        requestWithDirectory.setScriptsDirectory(scriptsPath);
        List<File> scriptFiles =
                scriptsDirectoryHelper.prepareDeploymentFilesWithGitRepo(
                        taskWorkspace, request.getGitRepoDetails(), request.getTfState());
        requestWithDirectory.setScriptFiles(scriptFiles);
        return requestWithDirectory;
    }

    private String getScriptsLocationInTaskWorkspace(
            OpenTofuScriptsGitRepoDetails scriptsGitRepoDetails, String taskWorkSpace) {
        if (StringUtils.isNotBlank(scriptsGitRepoDetails.getScriptPath())) {
            return taskWorkSpace + File.separator + scriptsGitRepoDetails.getScriptPath();
        }
        return taskWorkSpace;
    }

    /**
     * Transform OpenTofuRequestWithScripts to OpenTofuRequestWithScriptsDirectory.
     *
     * @param request request with scripts.
     * @return request with scripts directory.
     */
    private OpenTofuRequestWithScriptsDirectory convertRequestWithScriptsToDirectory(
            OpenTofuRequestWithScripts request) {
        OpenTofuRequestWithScriptsDirectory requestWithDirectory =
                new OpenTofuRequestWithScriptsDirectory();
        if (request instanceof OpenTofuAsyncRequestWithScripts) {
            requestWithDirectory = new OpenTofuAsyncRequestWithScriptsDirectory();
        }
        BeanUtils.copyProperties(request, requestWithDirectory);
        String scriptsPath =
                scriptsDirectoryHelper.buildTaskWorkspace(request.getRequestId().toString());
        requestWithDirectory.setScriptsDirectory(scriptsPath);
        List<File> scriptFilesList =
                scriptsDirectoryHelper.prepareDeploymentFilesWithScripts(
                        scriptsPath, request.getScriptFiles(), request.getTfState());
        requestWithDirectory.setScriptFiles(scriptFilesList);
        return requestWithDirectory;
    }
}
