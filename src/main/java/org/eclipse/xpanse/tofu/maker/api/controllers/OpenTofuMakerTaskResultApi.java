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
import java.util.Objects;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.tofu.maker.models.response.OpenTofuResult;
import org.eclipse.xpanse.tofu.maker.models.response.ReFetchResult;
import org.eclipse.xpanse.tofu.maker.models.response.ReFetchState;
import org.eclipse.xpanse.tofu.maker.opentofu.service.OpenTofuResultPersistenceManage;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
@CrossOrigin
@RestController
@RequestMapping("/tofu-maker/task")
public class OpenTofuMakerTaskResultApi {

    @Resource private OpenTofuResultPersistenceManage openTofuResultPersistenceManage;

    /**
     * Retrieve stored openTofu result.
     *
     * @param requestId id of the request
     * @return response entity of the openTofu result
     */
    @Tag(
            name = "RetrieveOpenTofuResult",
            description = "APIs to manually fetching task results from tofu-maker.")
    @Operation(
            description =
                    "Method to retrieve stored openTofu result in case tofu-maker "
                            + "receives a failure while sending the openTofu result via callback.")
    @GetMapping(value = "/result/{requestId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<OpenTofuResult> getStoredTaskResultByRequestId(
            @Parameter(name = "requestId", description = "id of the request")
                    @PathVariable("requestId")
                    UUID requestId) {
        return openTofuResultPersistenceManage.retrieveOpenTofuResultByRequestId(requestId);
    }

    /**
     * Batch retrieve stored openTofu results.
     *
     * @param requestIds list of request ids
     * @return list of re-fetch results
     */
    @Tag(
            name = "RetrieveOpenTofuResult",
            description = "APIs to manually fetching task results from tofu-maker.")
    @Operation(description = "Method to batch retrieve stored openTofu results from tofu-maker.")
    @PostMapping(value = "/results/batch", consumes = MediaType.APPLICATION_JSON_VALUE)
    public List<ReFetchResult> getBatchTaskResults(
            @Parameter(description = "List of request IDs") @RequestBody List<UUID> requestIds) {
        if (CollectionUtils.isEmpty(requestIds)) {
            throw new IllegalArgumentException("requestIds cannot be empty.");
        }
        List<ReFetchResult> reFetchResults = new ArrayList<>();
        requestIds.forEach(
                requestId -> {
                    try {
                        ResponseEntity<OpenTofuResult> result =
                                openTofuResultPersistenceManage.retrieveOpenTofuResultByRequestId(
                                        requestId);
                        if (result.getStatusCode() == HttpStatus.OK
                                && Objects.nonNull(result.getBody())) {
                            reFetchResults.add(
                                    ReFetchResult.builder()
                                            .openTofuResult(result.getBody())
                                            .state(ReFetchState.OK)
                                            .build());
                        } else {
                            reFetchResults.add(
                                    ReFetchResult.builder()
                                            .state(
                                                    ReFetchState
                                                            .RESULT_ALREADY_RETURNED_OR_REQUEST_ID_INVALID)
                                            .build());
                        }
                    } catch (Exception e) {
                        reFetchResults.add(
                                ReFetchResult.builder()
                                        .state(
                                                ReFetchState
                                                        .RESULT_ALREADY_RETURNED_OR_REQUEST_ID_INVALID)
                                        .build());
                    }
                });
        return reFetchResults;
    }
}
