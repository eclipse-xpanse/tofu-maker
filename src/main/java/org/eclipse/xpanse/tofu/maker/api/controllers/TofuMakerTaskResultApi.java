/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.tofu.maker.api.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.tofu.maker.models.response.ReFetchResult;
import org.eclipse.xpanse.tofu.maker.opentofu.service.OpenTofuResultPersistenceManage;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** REST controller for manage the task form tofu-maker. */
@Slf4j
@Profile("!amqp")
@CrossOrigin
@RestController
@RequestMapping("/tofu-maker/task")
public class TofuMakerTaskResultApi {

    @Resource private OpenTofuResultPersistenceManage resultPersistenceManage;

    /**
     * Fetch the stored opentofu result.
     *
     * @param requestId id of the request
     * @return opentofu result
     */
    @Tag(
            name = "RetrieveOpenTofuResult",
            description = "APIs to manually fetching task results from tofu-maker.")
    @Operation(
            description =
                    "Method to retrieve stored opentofu result in case tofu-maker "
                            + "receives a failure while sending the opentofu result via callback.")
    @GetMapping(value = "/result/{requestId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public ReFetchResult getStoredTaskResultByRequestId(
            @Parameter(name = "requestId", description = "id of the request")
                    @PathVariable("requestId")
                    UUID requestId) {
        return resultPersistenceManage.retrieveOpenTofuResultByRequestId(requestId);
    }

    /**
     * Batch retrieve stored opentofu results.
     *
     * @param requestIds list of requestIds
     * @return list of reFetchResults
     */
    @Tag(
            name = "RetrieveOpenTofuResult",
            description = "APIs to manually fetching task results from tofu-maker.")
    @Operation(description = "Method to batch retrieve stored opentofu result from tofu-maker.")
    @PostMapping(value = "/results/batch", consumes = MediaType.APPLICATION_JSON_VALUE)
    public List<ReFetchResult> getBatchTaskResults(
            @Parameter(description = "List of request IDs", required = true) @RequestBody
                    List<UUID> requestIds) {
        if (CollectionUtils.isEmpty(requestIds)) {
            throw new IllegalArgumentException("requestIds cannot be empty.");
        }
        List<ReFetchResult> reFetchResults = new ArrayList<>();
        requestIds.forEach(
                requestId -> {
                    ReFetchResult reFetchResult =
                            resultPersistenceManage.retrieveOpenTofuResultByRequestId(requestId);
                    reFetchResults.add(reFetchResult);
                });
        return reFetchResults;
    }
}
