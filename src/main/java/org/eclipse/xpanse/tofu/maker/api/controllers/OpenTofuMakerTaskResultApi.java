/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.tofu.maker.api.controllers;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.tofu.maker.models.response.OpenTofuResult;
import org.eclipse.xpanse.tofu.maker.opentofu.service.OpenTofuResultPersistenceManage;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for manage the task form tofu-maker.
 */
@Slf4j
@CrossOrigin
@RestController
@RequestMapping("/tofu-maker/task")
public class OpenTofuMakerTaskResultApi {

    @Resource
    private OpenTofuResultPersistenceManage openTofuResultPersistenceManage;

    @Tag(name = "RetrieveOpenTofuResult", description =
            "APIs for manage the task form tofu-maker.")
    @Operation(description = "Method to retrieve stored openTofu result in case tofu-maker "
            + "receives a failure while sending the openTofu result via callback.")
    @GetMapping(value = "/result/{requestId}", produces =
            MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public OpenTofuResult getStoredTaskResultByRequestId(
            @Parameter(name = "requestId",
                    description = "id of the request")
            @PathVariable("requestId") String requestId) {
        return openTofuResultPersistenceManage.retrieveOpenTofuResultByRequestId(requestId);
    }
}
