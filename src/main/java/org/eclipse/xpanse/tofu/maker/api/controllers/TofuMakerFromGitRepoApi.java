/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.tofu.maker.api.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.tofu.maker.models.request.git.OpenTofuAsyncRequestWithScriptsGitRepo;
import org.eclipse.xpanse.tofu.maker.models.request.git.OpenTofuRequestWithScriptsGitRepo;
import org.eclipse.xpanse.tofu.maker.models.response.OpenTofuPlan;
import org.eclipse.xpanse.tofu.maker.models.response.OpenTofuResult;
import org.eclipse.xpanse.tofu.maker.models.response.validation.OpenTofuValidationResult;
import org.eclipse.xpanse.tofu.maker.opentofu.service.OpenTofuRequestService;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** REST controller for running OpenTofu modules from a GIT repo. */
@Slf4j
@CrossOrigin
@Profile("!amqp")
@RestController
@RequestMapping("/tofu-maker/git")
public class TofuMakerFromGitRepoApi {

    @Resource private OpenTofuRequestService requestService;

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
            @Valid @RequestBody OpenTofuRequestWithScriptsGitRepo request) {
        return requestService.handleOpenTofuValidateRequest(request);
    }

    /**
     * Method to get OpenTofu plan as a JSON string from the GIT repo provided.
     *
     * @return Returns the OpenTofu plan as a JSON string.
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
            @Valid @RequestBody OpenTofuRequestWithScriptsGitRepo request) {
        return requestService.handleOpenTofuPlanRequest(request);
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
            @Valid @RequestBody OpenTofuRequestWithScriptsGitRepo request) {
        return requestService.handleOpenTofuDeploymentRequest(request);
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
            @Valid @RequestBody OpenTofuRequestWithScriptsGitRepo request) {
        return requestService.handleOpenTofuDeploymentRequest(request);
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
            @Valid @RequestBody OpenTofuRequestWithScriptsGitRepo request) {
        return requestService.handleOpenTofuDeploymentRequest(request);
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
            @Valid @RequestBody OpenTofuAsyncRequestWithScriptsGitRepo request) {
        requestService.processAsyncDeploymentRequest(request);
    }

    /** Method to async modify resources from the provided GIT Repo. */
    @Tag(
            name = "OpenTofuFromGitRepo",
            description =
                    "APIs for running OpenTofu commands using OpenTofu scripts from a GIT Repo.")
    @Operation(description = "async deploy resources via OpenTofu")
    @PostMapping(value = "/modify/async", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void asyncModifyFromGitRepo(
            @Valid @RequestBody OpenTofuAsyncRequestWithScriptsGitRepo request) {
        requestService.processAsyncDeploymentRequest(request);
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
            @Valid @RequestBody OpenTofuAsyncRequestWithScriptsGitRepo request) {
        requestService.processAsyncDeploymentRequest(request);
    }
}
