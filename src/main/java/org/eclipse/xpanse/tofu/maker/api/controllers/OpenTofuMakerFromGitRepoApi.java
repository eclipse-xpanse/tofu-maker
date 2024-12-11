/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.tofu.maker.api.controllers;

import static org.eclipse.xpanse.tofu.maker.logging.CustomRequestIdGenerator.REQUEST_ID;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Objects;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.tofu.maker.models.plan.OpenTofuPlan;
import org.eclipse.xpanse.tofu.maker.models.plan.OpenTofuPlanFromGitRepoRequest;
import org.eclipse.xpanse.tofu.maker.models.request.git.OpenTofuAsyncDeployFromGitRepoRequest;
import org.eclipse.xpanse.tofu.maker.models.request.git.OpenTofuAsyncDestroyFromGitRepoRequest;
import org.eclipse.xpanse.tofu.maker.models.request.git.OpenTofuAsyncModifyFromGitRepoRequest;
import org.eclipse.xpanse.tofu.maker.models.request.git.OpenTofuDeployFromGitRepoRequest;
import org.eclipse.xpanse.tofu.maker.models.request.git.OpenTofuDestroyFromGitRepoRequest;
import org.eclipse.xpanse.tofu.maker.models.request.git.OpenTofuModifyFromGitRepoRequest;
import org.eclipse.xpanse.tofu.maker.models.response.OpenTofuResult;
import org.eclipse.xpanse.tofu.maker.models.validation.OpenTofuValidationResult;
import org.eclipse.xpanse.tofu.maker.opentofu.service.OpenTofuGitRepoService;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** REST controller for running open tofu modules from a GIT repo. */
@Slf4j
@CrossOrigin
@RestController
@RequestMapping("/tofu-maker/git")
public class OpenTofuMakerFromGitRepoApi {

    private final OpenTofuGitRepoService openTofuGitRepoService;

    public OpenTofuMakerFromGitRepoApi(OpenTofuGitRepoService openTofuGitRepoService) {
        this.openTofuGitRepoService = openTofuGitRepoService;
    }

    /**
     * Method to validate resources by scripts.
     *
     * @return Returns the status of the deployment.
     */
    @Tag(
            name = "OpenTofuFromGitRepo",
            description =
                    "APIs for running OpenTofu commands using OpenTofu scripts from a GIT Repo.")
    @Operation(description = "Deploy resources via OpenTofu")
    @PostMapping(value = "/validate", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public OpenTofuValidationResult validateScriptsFromGitRepo(
            @Valid @RequestBody OpenTofuDeployFromGitRepoRequest request) {
        UUID uuid =
                Objects.nonNull(request.getRequestId())
                        ? request.getRequestId()
                        : UUID.randomUUID();
        MDC.put(REQUEST_ID, uuid.toString());
        request.setRequestId(uuid);
        return openTofuGitRepoService.validateWithScripts(request);
    }

    /**
     * Method to get OpenTofu plan as a JSON string from the GIT repo provided.
     *
     * @return Returns the open tofu plan as a JSON string.
     */
    @Tag(
            name = "OpenTofuFromGitRepo",
            description =
                    "APIs for running OpenTofu commands using OpenTofu scripts from a GIT Repo.")
    @Operation(
            description = "Get OpenTofu Plan as JSON string from the list of script files provided")
    @PostMapping(value = "/plan", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public OpenTofuPlan planFromGitRepo(
            @Valid @RequestBody OpenTofuPlanFromGitRepoRequest request) {
        UUID uuid =
                Objects.nonNull(request.getRequestId())
                        ? request.getRequestId()
                        : UUID.randomUUID();
        MDC.put(REQUEST_ID, uuid.toString());
        request.setRequestId(uuid);
        return openTofuGitRepoService.getOpenTofuPlanFromGitRepo(request, uuid);
    }

    /**
     * Method to deploy resources using scripts from the GIT Repo provided.
     *
     * @return Returns the status of the deployment.
     */
    @Tag(
            name = "OpenTofuFromGitRepo",
            description =
                    "APIs for running OpenTofu commands using OpenTofu scripts from a GIT Repo.")
    @Operation(description = "Deploy resources via OpenTofu")
    @PostMapping(value = "/deploy", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public OpenTofuResult deployFromGitRepo(
            @Valid @RequestBody OpenTofuDeployFromGitRepoRequest request) {
        UUID uuid =
                Objects.nonNull(request.getRequestId())
                        ? request.getRequestId()
                        : UUID.randomUUID();
        MDC.put(REQUEST_ID, uuid.toString());
        request.setRequestId(uuid);
        return openTofuGitRepoService.deployFromGitRepo(request, uuid);
    }

    /**
     * Method to modify resources using scripts from the GIT Repo provided.
     *
     * @return Returns the status of the deployment.
     */
    @Tag(
            name = "OpenTofuFromGitRepo",
            description =
                    "APIs for running OpenTofu commands using OpenTofu scripts from a GIT Repo.")
    @Operation(description = "Modify resources via OpenTofu")
    @PostMapping(value = "/modify", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public OpenTofuResult modifyFromGitRepo(
            @Valid @RequestBody OpenTofuModifyFromGitRepoRequest request) {
        UUID uuid =
                Objects.nonNull(request.getRequestId())
                        ? request.getRequestId()
                        : UUID.randomUUID();
        MDC.put(REQUEST_ID, uuid.toString());
        request.setRequestId(uuid);
        return openTofuGitRepoService.modifyFromGitRepo(request, uuid);
    }

    /**
     * MMethod to deploy resources using scripts from the GIT Repo provided.
     *
     * @return Returns the status of to Destroy.
     */
    @Tag(
            name = "OpenTofuFromGitRepo",
            description =
                    "APIs for running OpenTofu commands using OpenTofu scripts from a GIT Repo.")
    @Operation(description = "Destroy resources via OpenTofu")
    @PostMapping(value = "/destroy", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public OpenTofuResult destroyFromGitRepo(
            @Valid @RequestBody OpenTofuDestroyFromGitRepoRequest request) {
        UUID uuid =
                Objects.nonNull(request.getRequestId())
                        ? request.getRequestId()
                        : UUID.randomUUID();
        MDC.put(REQUEST_ID, uuid.toString());
        request.setRequestId(uuid);
        return openTofuGitRepoService.destroyFromGitRepo(request, uuid);
    }

    /** Method to async deploy resources from the provided GIT Repo. */
    @Tag(
            name = "OpenTofuFromGitRepo",
            description =
                    "APIs for running OpenTofu commands using OpenTofu scripts from a GIT Repo.")
    @Operation(description = "async deploy resources via OpenTofu")
    @PostMapping(value = "/deploy/async", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void asyncDeployFromGitRepo(
            @Valid @RequestBody OpenTofuAsyncDeployFromGitRepoRequest request) {
        UUID uuid =
                Objects.nonNull(request.getRequestId())
                        ? request.getRequestId()
                        : UUID.randomUUID();
        MDC.put(REQUEST_ID, uuid.toString());
        request.setRequestId(uuid);
        openTofuGitRepoService.asyncDeployFromGitRepo(request, uuid);
    }

    /** Method to async modify resources from the provided GIT Repo. */
    @Tag(
            name = "OpenTofuFromGitRepo",
            description =
                    "APIs for running OpenTofu commands using OpenTofu scripts from a GIT Repo.")
    @Operation(description = "async modify resources via OpenTofu")
    @PostMapping(value = "/modify/async", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void asyncModifyFromGitRepo(
            @Valid @RequestBody OpenTofuAsyncModifyFromGitRepoRequest request) {
        UUID uuid =
                Objects.nonNull(request.getRequestId())
                        ? request.getRequestId()
                        : UUID.randomUUID();
        MDC.put(REQUEST_ID, uuid.toString());
        request.setRequestId(uuid);
        openTofuGitRepoService.asyncModifyFromGitRepo(request, uuid);
    }

    /** Method to async destroy resources by scripts. */
    @Tag(
            name = "OpenTofuFromGitRepo",
            description =
                    "APIs for running OpenTofu commands using OpenTofu scripts from a GIT Repo.")
    @Operation(description = "Async destroy the OpenTofu modules")
    @DeleteMapping(value = "/destroy/async", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void asyncDestroyFromGitRepo(
            @Valid @RequestBody OpenTofuAsyncDestroyFromGitRepoRequest request) {
        UUID uuid =
                Objects.nonNull(request.getRequestId())
                        ? request.getRequestId()
                        : UUID.randomUUID();
        MDC.put(REQUEST_ID, uuid.toString());
        request.setRequestId(uuid);
        openTofuGitRepoService.asyncDestroyFromGitRepo(request, uuid);
    }
}
