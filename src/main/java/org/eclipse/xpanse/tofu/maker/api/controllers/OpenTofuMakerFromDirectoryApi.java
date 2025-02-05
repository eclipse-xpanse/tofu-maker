/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.tofu.maker.api.controllers;

import static org.eclipse.xpanse.tofu.maker.logging.CustomRequestIdGenerator.REQUEST_ID;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
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
import org.eclipse.xpanse.tofu.maker.opentofu.service.OpenTofuDirectoryService;
import org.eclipse.xpanse.tofu.maker.opentofu.service.OpenTofuScriptsHelper;
import org.eclipse.xpanse.tofu.maker.opentofu.tool.OpenTofuVersionsHelper;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** REST controller for running open tofu modules directly on the provided directory. */
@Slf4j
@CrossOrigin
@RestController
@RequestMapping("/tofu-maker/directory")
public class OpenTofuMakerFromDirectoryApi {
    @Resource private OpenTofuDirectoryService directoryService;
    @Resource private OpenTofuScriptsHelper scriptsHelper;

    /**
     * Method to validate OpenTofu modules.
     *
     * @return Returns the validation status of the OpenTofu module in a workspace.
     */
    @Tag(
            name = "OpenTofuFromDirectory",
            description = "APIs for running OpenTofu commands inside a provided directory.")
    @Operation(description = "Validate the OpenTofu modules in the given directory.")
    @GetMapping(
            value = "/validate/{module_directory}/{opentofu_version}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public OpenTofuValidationResult validateFromDirectory(
            @Parameter(
                            name = "module_directory",
                            description = "directory name where the OpenTofu module files exist.")
                    @PathVariable("module_directory")
                    String moduleDirectory,
            @Parameter(
                            name = "opentofu_version",
                            description = "version of OpenTofu to execute the module files.")
                    @NotBlank
                    @Pattern(regexp = OpenTofuVersionsHelper.OPENTOFU_REQUIRED_VERSION_REGEX)
                    @PathVariable("opentofu_version")
                    String openTofuVersion) {
        UUID uuid = UUID.randomUUID();
        MDC.put(REQUEST_ID, uuid.toString());
        return directoryService.tfValidateFromDirectory(moduleDirectory, openTofuVersion);
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
    @PostMapping(value = "/deploy/{module_directory}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public OpenTofuResult deployFromDirectory(
            @Parameter(
                            name = "module_directory",
                            description = "directory name where the OpenTofu module files exist.")
                    @PathVariable("module_directory")
                    String moduleDirectory,
            @Valid @RequestBody OpenTofuDeployFromDirectoryRequest request) {
        UUID uuid =
                Objects.nonNull(request.getRequestId())
                        ? request.getRequestId()
                        : UUID.randomUUID();
        MDC.put(REQUEST_ID, uuid.toString());
        request.setRequestId(uuid);
        List<File> scriptFiles = scriptsHelper.getDeploymentFilesFromTaskWorkspace(moduleDirectory);
        return directoryService.deployFromDirectory(request, moduleDirectory, scriptFiles);
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
    @PostMapping(value = "/modify/{module_directory}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public OpenTofuResult modifyFromDirectory(
            @Parameter(
                            name = "module_directory",
                            description = "directory name where the OpenTofu module files exist.")
                    @PathVariable("module_directory")
                    String moduleDirectory,
            @Valid @RequestBody OpenTofuModifyFromDirectoryRequest request) {
        UUID uuid =
                Objects.nonNull(request.getRequestId())
                        ? request.getRequestId()
                        : UUID.randomUUID();
        MDC.put(REQUEST_ID, uuid.toString());
        request.setRequestId(uuid);
        List<File> scriptFiles = scriptsHelper.getDeploymentFilesFromTaskWorkspace(moduleDirectory);
        return directoryService.modifyFromDirectory(request, moduleDirectory, scriptFiles);
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
    @DeleteMapping(
            value = "/destroy/{module_directory}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public OpenTofuResult destroyFromDirectory(
            @Parameter(
                            name = "module_directory",
                            description = "directory name where the OpenTofu module files exist.")
                    @PathVariable("module_directory")
                    String moduleDirectory,
            @Valid @RequestBody OpenTofuDestroyFromDirectoryRequest request) {
        UUID uuid =
                Objects.nonNull(request.getRequestId())
                        ? request.getRequestId()
                        : UUID.randomUUID();
        MDC.put(REQUEST_ID, uuid.toString());
        request.setRequestId(uuid);
        List<File> scriptFiles = scriptsHelper.getDeploymentFilesFromTaskWorkspace(moduleDirectory);
        return directoryService.destroyFromDirectory(request, moduleDirectory, scriptFiles);
    }

    /**
     * Method to get OpenTofu plan as a JSON string from a directory.
     *
     * @return Returns the open tofu plan as a JSON string.
     */
    @Tag(
            name = "OpenTofuFromDirectory",
            description = "APIs for running OpenTofu commands inside a provided directory.")
    @Operation(description = "Get OpenTofu Plan as JSON string from the given directory.")
    @PostMapping(value = "/plan/{module_directory}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public OpenTofuPlan plan(
            @Parameter(
                            name = "module_directory",
                            description = "directory name where the OpenTofu module files exist.")
                    @PathVariable("module_directory")
                    String moduleDirectory,
            @Valid @RequestBody OpenTofuPlanFromDirectoryRequest request) {
        UUID uuid =
                Objects.nonNull(request.getRequestId())
                        ? request.getRequestId()
                        : UUID.randomUUID();
        MDC.put(REQUEST_ID, uuid.toString());
        request.setRequestId(uuid);
        return directoryService.getOpenTofuPlanFromDirectory(request, moduleDirectory);
    }

    /** Method to async deploy resources from the given directory. */
    @Tag(
            name = "OpenTofuFromDirectory",
            description = "APIs for running OpenTofu commands inside a provided directory.")
    @Operation(description = "async deploy resources via OpenTofu from the given directory.")
    @PostMapping(
            value = "/deploy/async/{module_directory}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void asyncDeployFromDirectory(
            @Parameter(
                            name = "module_directory",
                            description = "directory name where the OpenTofu module files exist.")
                    @PathVariable("module_directory")
                    String moduleDirectory,
            @Valid @RequestBody OpenTofuAsyncDeployFromDirectoryRequest request) {
        UUID uuid =
                Objects.nonNull(request.getRequestId())
                        ? request.getRequestId()
                        : UUID.randomUUID();
        MDC.put(REQUEST_ID, uuid.toString());
        request.setRequestId(uuid);
        List<File> scriptFiles = scriptsHelper.getDeploymentFilesFromTaskWorkspace(moduleDirectory);
        directoryService.asyncDeployWithScripts(request, moduleDirectory, scriptFiles);
    }

    /** Method to async modify resources from the given directory. */
    @Tag(
            name = "OpenTofuFromDirectory",
            description = "APIs for running OpenTofu commands inside a provided directory.")
    @Operation(description = "async modify resources via OpenTofu from the given directory.")
    @PostMapping(
            value = "/modify/async/{module_directory}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void asyncModifyFromDirectory(
            @Parameter(
                            name = "module_directory",
                            description = "directory name where the OpenTofu module files exist.")
                    @PathVariable("module_directory")
                    String moduleDirectory,
            @Valid @RequestBody OpenTofuAsyncModifyFromDirectoryRequest request) {
        UUID uuid =
                Objects.nonNull(request.getRequestId())
                        ? request.getRequestId()
                        : UUID.randomUUID();
        MDC.put(REQUEST_ID, uuid.toString());
        request.setRequestId(uuid);
        List<File> scriptFiles = scriptsHelper.getDeploymentFilesFromTaskWorkspace(moduleDirectory);
        directoryService.asyncModifyWithScripts(request, moduleDirectory, scriptFiles);
    }

    /** Method to async destroy resources from the given directory. */
    @Tag(
            name = "OpenTofuFromDirectory",
            description = "APIs for running OpenTofu commands inside a provided directory.")
    @Operation(description = "async destroy resources via OpenTofu from the given directory.")
    @DeleteMapping(
            value = "/destroy/async/{module_directory}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void asyncDestroyFromDirectory(
            @Parameter(
                            name = "module_directory",
                            description = "directory name where the OpenTofu module files exist.")
                    @PathVariable("module_directory")
                    String moduleDirectory,
            @Valid @RequestBody OpenTofuAsyncDestroyFromDirectoryRequest request) {
        UUID uuid =
                Objects.nonNull(request.getRequestId())
                        ? request.getRequestId()
                        : UUID.randomUUID();
        MDC.put(REQUEST_ID, uuid.toString());
        request.setRequestId(uuid);
        List<File> scriptFiles = scriptsHelper.getDeploymentFilesFromTaskWorkspace(moduleDirectory);
        directoryService.asyncDestroyWithScripts(request, moduleDirectory, scriptFiles);
    }
}
