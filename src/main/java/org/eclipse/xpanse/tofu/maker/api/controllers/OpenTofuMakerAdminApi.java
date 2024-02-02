/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.tofu.maker.api.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.tofu.maker.models.OpenTofuMakerSystemStatus;
import org.eclipse.xpanse.tofu.maker.opentofu.service.OpenTofuDirectoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for admin services of tofu-maker.
 */
@Slf4j
@CrossOrigin
@RestController
@RequestMapping("/tofu-maker")
public class OpenTofuMakerAdminApi {

    private final OpenTofuDirectoryService openTofuDirectoryService;

    @Autowired
    public OpenTofuMakerAdminApi(
            @Qualifier("openTofuDirectoryService")
            OpenTofuDirectoryService openTofuDirectoryService) {
        this.openTofuDirectoryService = openTofuDirectoryService;
    }

    /**
     * Method to find out the current state of the system.
     *
     * @return Returns the current state of the system.
     */
    @Tag(name = "Admin", description =
            "Admin services for managing the application.")
    @Operation(description = "Check health of OpenTofu Maker API service")
    @GetMapping(value = "/health", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public OpenTofuMakerSystemStatus healthCheck() {
        return openTofuDirectoryService.tfHealthCheck();
    }

}
