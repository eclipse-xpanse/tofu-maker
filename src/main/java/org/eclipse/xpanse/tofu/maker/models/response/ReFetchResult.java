/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.tofu.maker.models.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

/** Data model for re-fetching order result. */
@Data
@Builder
public class ReFetchResult {

    @NotNull
    @Schema(description = "State of the re-fetching request")
    private ReFetchState state;

    @Schema(description = "Result of the service order executed by open tofu.")
    private OpenTofuResult openTofuResult;
}
