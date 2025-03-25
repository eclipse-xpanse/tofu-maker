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
import org.eclipse.xpanse.tofu.maker.models.request.directory.OpenTofuAsyncRequestWithScriptsDirectory;
import org.eclipse.xpanse.tofu.maker.models.request.directory.OpenTofuRequestWithScriptsDirectory;
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

/** REST controller for running OpenTofu modules directly on the provided directory. */
@Slf4j
@Profile("!amqp")
@CrossOrigin
@RestController
@RequestMapping("/tofu-maker/directory")
public class TofuMakerFromDirectoryApi {

    @Resource private OpenTofuRequestService requestService;

    /**
     * Method to validate OpenTofu modules.
     *
     * @return Returns the validation status of the OpenTofu module in a workspace.
     */
    @Tag(
            name = "OpenTofuFromDirectory",
            description = "APIs for running OpenTofu commands inside a provided directory.")
    @Operation(description = "Validate the OpenTofu modules in the given directory.")
    @PostMapping(value = "/validate", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public OpenTofuValidationResult validateFromDirectory(
            @Valid @RequestBody OpenTofuRequestWithScriptsDirectory request) {
        return requestService.handleOpenTofuValidateRequest(request);
    }

    /**
     * Method to get OpenTofu plan as a JSON string from a directory.
     *
     * @return Returns the OpenTofu plan as a JSON string.
     */
    @Tag(
            name = "OpenTofuFromDirectory",
            description = "APIs for running OpenTofu commands inside a provided directory.")
    @Operation(description = "Get OpenTofu Plan as JSON string from the given directory.")
    @PostMapping(value = "/plan", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public OpenTofuPlan plan(@Valid @RequestBody OpenTofuRequestWithScriptsDirectory request) {
        return requestService.handleOpenTofuPlanRequest(request);
    }

    /**
     * Method to deploy resources requested in a workspace.
     *
     * @return Returns the status of the deployment.
     */
    @Tag(
            name = "OpenTofuFromDirectory",
            description = "APIs for running OpenTofu commands inside a provided directory.")
    @Operation(description = "Deploy resources via OpenTofu from the given directory.")
    @PostMapping(value = "/deploy", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public OpenTofuResult deployFromDirectory(
            @Valid @RequestBody OpenTofuRequestWithScriptsDirectory request) {
        return requestService.handleOpenTofuDeploymentRequest(request);
    }

    /**
     * Method to modify resources requested in a workspace.
     *
     * @return Returns the status of the deployment.
     */
    @Tag(
            name = "OpenTofuFromDirectory",
            description = "APIs for running OpenTofu commands inside a provided directory.")
    @Operation(description = "Modify resources via OpenTofu from the given directory.")
    @PostMapping(value = "/modify", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public OpenTofuResult modifyFromDirectory(
            @Valid @RequestBody OpenTofuRequestWithScriptsDirectory request) {
        return requestService.handleOpenTofuDeploymentRequest(request);
    }

    /**
     * Method to destroy resources requested in a workspace.
     *
     * @return Returns the status of the resources destroy.
     */
    @Tag(
            name = "OpenTofuFromDirectory",
            description = "APIs for running OpenTofu commands inside a provided directory.")
    @Operation(description = "Destroy the resources from the given directory.")
    @DeleteMapping(value = "/destroy", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public OpenTofuResult destroyFromDirectory(
            @Valid @RequestBody OpenTofuRequestWithScriptsDirectory request) {
        return requestService.handleOpenTofuDeploymentRequest(request);
    }

    /** Method to async deploy resources from the given directory. */
    @Tag(
            name = "OpenTofuFromDirectory",
            description = "APIs for running OpenTofu commands inside a provided directory.")
    @Operation(description = "async deploy resources via OpenTofu from the given directory.")
    @PostMapping(value = "/deploy/async", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void asyncDeployFromDirectory(
            @Valid @RequestBody OpenTofuAsyncRequestWithScriptsDirectory request) {
        requestService.processAsyncDeploymentRequest(request);
    }

    /** Method to async modify resources from the given directory. */
    @Tag(
            name = "OpenTofuFromDirectory",
            description = "APIs for running OpenTofu commands inside a provided directory.")
    @Operation(description = "async modify resources via OpenTofu from the given directory.")
    @PostMapping(value = "/modify/async", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void asyncModifyFromDirectory(
            @Valid @RequestBody OpenTofuAsyncRequestWithScriptsDirectory request) {
        requestService.processAsyncDeploymentRequest(request);
    }

    /** Method to async destroy resources from the given directory. */
    @Tag(
            name = "OpenTofuFromDirectory",
            description = "APIs for running OpenTofu commands inside a provided directory.")
    @Operation(description = "async destroy resources via OpenTofu from the given directory.")
    @DeleteMapping(value = "/destroy/async", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void asyncDestroyFromDirectory(
            @Valid @RequestBody OpenTofuAsyncRequestWithScriptsDirectory request) {
        requestService.processAsyncDeploymentRequest(request);
    }
}
