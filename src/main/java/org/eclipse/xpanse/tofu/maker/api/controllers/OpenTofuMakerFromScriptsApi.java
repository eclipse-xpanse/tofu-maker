/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.tofu.maker.api.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Objects;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.tofu.maker.models.plan.OpenTofuPlan;
import org.eclipse.xpanse.tofu.maker.models.plan.OpenTofuPlanWithScriptsRequest;
import org.eclipse.xpanse.tofu.maker.models.request.scripts.OpenTofuAsyncDeployFromScriptsRequest;
import org.eclipse.xpanse.tofu.maker.models.request.scripts.OpenTofuAsyncDestroyFromScriptsRequest;
import org.eclipse.xpanse.tofu.maker.models.request.scripts.OpenTofuDeployWithScriptsRequest;
import org.eclipse.xpanse.tofu.maker.models.request.scripts.OpenTofuDestroyWithScriptsRequest;
import org.eclipse.xpanse.tofu.maker.models.response.OpenTofuResult;
import org.eclipse.xpanse.tofu.maker.models.validation.OpenTofuValidationResult;
import org.eclipse.xpanse.tofu.maker.opentofu.service.OpenTofuScriptsService;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * API methods implemented by tofu-maker.
 */
@Slf4j
@CrossOrigin
@RestController
@RequestMapping("/tofu-maker/scripts/")
public class OpenTofuMakerFromScriptsApi {

    private final OpenTofuScriptsService openTofuScriptsService;

    public OpenTofuMakerFromScriptsApi(OpenTofuScriptsService openTofuScriptsService) {
        this.openTofuScriptsService = openTofuScriptsService;
    }

    /**
     * Method to validate resources by scripts.
     *
     * @return Returns the status of the deployment.
     */
    @Tag(name = "OpenTofuFromScripts", description =
            "APIs for running OpenTofu commands on the scripts sent via request body.")
    @Operation(description = "Deploy resources via OpenTofu")
    @PostMapping(value = "/validate", produces =
            MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public OpenTofuValidationResult validateWithScripts(
            @Valid @RequestBody OpenTofuDeployWithScriptsRequest request,
            @RequestHeader(name = "X-Custom-RequestId", required = false) UUID uuid) {
        if (Objects.isNull(uuid)) {
            uuid = UUID.randomUUID();
        }
        MDC.put("TASK_ID", uuid.toString());
        return openTofuScriptsService.validateWithScripts(request);
    }

    /**
     * Method to deploy resources by scripts.
     *
     * @return Returns the status of the deployment.
     */
    @Tag(name = "OpenTofuFromScripts", description =
            "APIs for running OpenTofu commands on the scripts sent via request body.")
    @Operation(description = "Deploy resources via OpenTofu")
    @PostMapping(value = "/deploy", produces =
            MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public OpenTofuResult deployWithScripts(
            @Valid @RequestBody OpenTofuDeployWithScriptsRequest request,
            @RequestHeader(name = "X-Custom-RequestId", required = false) UUID uuid) {
        if (Objects.isNull(uuid)) {
            uuid = UUID.randomUUID();
        }
        MDC.put("TASK_ID", uuid.toString());
        return openTofuScriptsService.deployWithScripts(request, uuid);
    }

    /**
     * Method to destroy resources by scripts.
     *
     * @return Returns the status of to Destroy.
     */
    @Tag(name = "OpenTofuFromScripts", description =
            "APIs for running OpenTofu commands on the scripts sent via request body.")
    @Operation(description = "Destroy resources via OpenTofu")
    @PostMapping(value = "/destroy", produces =
            MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public OpenTofuResult destroyWithScripts(
            @Valid @RequestBody OpenTofuDestroyWithScriptsRequest request,
            @RequestHeader(name = "X-Custom-RequestId", required = false) UUID uuid) {
        if (Objects.isNull(uuid)) {
            uuid = UUID.randomUUID();
        }
        MDC.put("TASK_ID", uuid.toString());
        return openTofuScriptsService.destroyWithScripts(request, uuid);
    }

    /**
     * Method to async deploy resources by scripts.
     */
    @Tag(name = "OpenTofuFromScripts", description =
            "APIs for running OpenTofu commands on the scripts sent via request body.")
    @Operation(description = "async deploy resources via OpenTofu")
    @PostMapping(value = "/deploy/async", produces =
            MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void asyncDeployWithScripts(
            @Valid @RequestBody OpenTofuAsyncDeployFromScriptsRequest asyncDeployRequest,
            @RequestHeader(name = "X-Custom-RequestId", required = false) UUID uuid) {
        if (Objects.isNull(uuid)) {
            uuid = UUID.randomUUID();
        }
        MDC.put("TASK_ID", uuid.toString());
        openTofuScriptsService.asyncDeployWithScripts(asyncDeployRequest, uuid);
    }

    /**
     * Method to async destroy resources by scripts.
     */
    @Tag(name = "OpenTofuFromScripts", description =
            "APIs for running OpenTofu commands on the scripts sent via request body.")
    @Operation(description = "Async destroy the OpenTofu modules")
    @DeleteMapping(value = "/destroy/async",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void asyncDestroyWithScripts(
            @Valid @RequestBody OpenTofuAsyncDestroyFromScriptsRequest asyncDestroyRequest,
            @RequestHeader(name = "X-Custom-RequestId", required = false) UUID uuid) {
        if (Objects.isNull(uuid)) {
            uuid = UUID.randomUUID();
        }
        MDC.put("TASK_ID", uuid.toString());
        openTofuScriptsService.asyncDestroyWithScripts(asyncDestroyRequest, uuid);
    }

    /**
     * Method to get OpenTofu plan as a JSON string from the list of script files provided.
     *
     * @return Returns the openTofu plan as a JSON string.
     */
    @Tag(name = "OpenTofuFromScripts", description =
            "APIs for running OpenTofu commands on the scripts sent via request body.")
    @Operation(description =
            "Get OpenTofu Plan as JSON string from the list of script files provided")
    @PostMapping(value = "/plan", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public OpenTofuPlan planWithScripts(
            @Valid @RequestBody OpenTofuPlanWithScriptsRequest request,
            @RequestHeader(name = "X-Custom-RequestId", required = false) UUID uuid) {
        if (Objects.isNull(uuid)) {
            uuid = UUID.randomUUID();
        }
        MDC.put("TASK_ID", uuid.toString());
        return openTofuScriptsService.getOpenTofuPlanFromScripts(request, uuid);
    }
}
