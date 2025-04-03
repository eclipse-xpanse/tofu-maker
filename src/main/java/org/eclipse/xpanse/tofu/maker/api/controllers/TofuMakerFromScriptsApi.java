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
import org.eclipse.xpanse.tofu.maker.models.request.scripts.OpenTofuAsyncRequestWithScripts;
import org.eclipse.xpanse.tofu.maker.models.request.scripts.OpenTofuRequestWithScripts;
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

/** API methods implemented by tofu-maker. */
@Slf4j
@CrossOrigin
@Profile("!amqp")
@RestController
@RequestMapping("/tofu-maker/scripts/")
public class TofuMakerFromScriptsApi {

    @Resource private OpenTofuRequestService requestService;

    /**
     * Method to validate resources by scripts.
     *
     * @return Returns the status of the deployment.
     */
    @Tag(
            name = "OpenTofuFromScripts",
            description =
                    "APIs for running OpenTofu commands on the scripts sent via request body.")
    @Operation(description = "Deploy resources via OpenTofu")
    @PostMapping(value = "/validate", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public OpenTofuValidationResult validateWithScripts(
            @Valid @RequestBody OpenTofuRequestWithScripts request) {
        return requestService.handleOpenTofuValidateRequest(request);
    }

    /**
     * Method to deploy resources by scripts.
     *
     * @return Returns the status of the deployment.
     */
    @Tag(
            name = "OpenTofuFromScripts",
            description =
                    "APIs for running OpenTofu commands on the scripts sent via request body.")
    @Operation(description = "Deploy resources via OpenTofu")
    @PostMapping(value = "/deploy", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public OpenTofuResult deployWithScripts(
            @Valid @RequestBody OpenTofuRequestWithScripts request) {
        return requestService.handleOpenTofuDeploymentRequest(request);
    }

    /**
     * Method to modify resources by scripts.
     *
     * @return Returns the status of the deployment.
     */
    @Tag(
            name = "OpenTofuFromScripts",
            description =
                    "APIs for running OpenTofu commands on the scripts sent via request body.")
    @Operation(description = "Modify resources via OpenTofu")
    @PostMapping(value = "/modify", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public OpenTofuResult modifyWithScripts(
            @Valid @RequestBody OpenTofuRequestWithScripts request) {
        return requestService.handleOpenTofuDeploymentRequest(request);
    }

    /**
     * Method to destroy resources by scripts.
     *
     * @return Returns the status of to Destroy.
     */
    @Tag(
            name = "OpenTofuFromScripts",
            description =
                    "APIs for running OpenTofu commands on the scripts sent via request body.")
    @Operation(description = "Destroy resources via OpenTofu")
    @PostMapping(value = "/destroy", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public OpenTofuResult destroyWithScripts(
            @Valid @RequestBody OpenTofuRequestWithScripts request) {
        return requestService.handleOpenTofuDeploymentRequest(request);
    }

    /** Method to async deploy resources by scripts. */
    @Tag(
            name = "OpenTofuFromScripts",
            description =
                    "APIs for running OpenTofu commands on the scripts sent via request body.")
    @Operation(description = "async deploy resources via OpenTofu")
    @PostMapping(value = "/deploy/async", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void asyncDeployWithScripts(
            @Valid @RequestBody OpenTofuAsyncRequestWithScripts request) {
        requestService.processAsyncDeploymentRequest(request);
    }

    /** Method to async modify resources by scripts. */
    @Tag(
            name = "OpenTofuFromScripts",
            description =
                    "APIs for running OpenTofu commands on the scripts sent via request body.")
    @Operation(description = "async modify resources via OpenTofu")
    @PostMapping(value = "/modify/async", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void asyncModifyWithScripts(
            @Valid @RequestBody OpenTofuAsyncRequestWithScripts request) {
        requestService.processAsyncDeploymentRequest(request);
    }

    /** Method to async destroy resources by scripts. */
    @Tag(
            name = "OpenTofuFromScripts",
            description =
                    "APIs for running OpenTofu commands on the scripts sent via request body.")
    @Operation(description = "Async destroy the OpenTofu modules")
    @DeleteMapping(value = "/destroy/async", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void asyncDestroyWithScripts(
            @Valid @RequestBody OpenTofuAsyncRequestWithScripts request) {
        requestService.processAsyncDeploymentRequest(request);
    }

    /**
     * Method to get OpenTofu plan as a JSON string from the list of script files provided.
     *
     * @return Returns the OpenTofu plan as a JSON string.
     */
    @Tag(
            name = "OpenTofuFromScripts",
            description =
                    "APIs for running OpenTofu commands on the scripts sent via request body.")
    @Operation(
            description = "Get OpenTofu Plan as JSON string from the list of script files provided")
    @PostMapping(value = "/plan", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public OpenTofuPlan planWithScripts(@Valid @RequestBody OpenTofuRequestWithScripts request) {
        return requestService.handleOpenTofuPlanRequest(request);
    }
}
