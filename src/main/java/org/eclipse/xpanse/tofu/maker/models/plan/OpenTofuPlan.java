/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.tofu.maker.models.plan;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

/**
 * Data model to represent open tofu plan output.
 */
@Data
@Builder
public class OpenTofuPlan {

    @NotNull
    @Schema(description = "OpenTofu plan as a JSON string")
    String plan;

    @Schema(description = "The version of the OpenTofu binary used to execute scripts.")
    String openTofuVersionUsed;
}
