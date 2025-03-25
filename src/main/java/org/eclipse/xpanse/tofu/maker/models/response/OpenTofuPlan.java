/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.tofu.maker.models.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Data model to represent open tofu plan output. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OpenTofuPlan implements Serializable {

    @Serial private static final long serialVersionUID = -2597757062433827230L;

    @NotNull
    @Schema(description = "ID of the request")
    private UUID requestId;

    @NotNull
    @Schema(description = "OpenTofu plan as a JSON string")
    private String plan;

    @Schema(description = "The version of the OpenTofu binary used to execute scripts.")
    private String openTofuVersionUsed;

    @Schema(description = "The error message of executing tofu plan command.")
    private String errorMessage;
}
