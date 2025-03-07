/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.tofu.maker.models.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.Builder;
import lombok.Data;

/** Data model for re-fetching order result. */
@Data
@Builder
public class ReFetchResult {

    @NotNull
    @Schema(description = "Id of the request order.")
    private UUID requestId;

    @NotNull
    @Schema(description = "State of the re-fetching result of order.")
    private ReFetchState state;

    @Schema(description = "Result of the service order executed by open tofu.")
    private OpenTofuResult openTofuResult;

    @Schema(description = "Error message of the re-fetching result of order.")
    private String errorMessage;
}
