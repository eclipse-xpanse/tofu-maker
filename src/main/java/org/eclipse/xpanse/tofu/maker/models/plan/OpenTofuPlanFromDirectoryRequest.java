/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.tofu.maker.models.plan;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.Data;

/**
 * Data model for the generating open tofu plan.
 */
@Data
public class OpenTofuPlanFromDirectoryRequest {

    @Schema(description = "Id of the request.")
    UUID requestId;

    @NotNull
    @Schema(description = "Key-value pairs of variables that must be used to execute the "
            + "OpenTofu request.",
            additionalProperties = Schema.AdditionalPropertiesValue.TRUE)
    Map<String, Object> variables;

    @Schema(description = "Key-value pairs of variables that must be injected as environment "
            + "variables to open tofu process.",
            additionalProperties = Schema.AdditionalPropertiesValue.TRUE)
    Map<String, String> envVariables = new HashMap<>();
}
