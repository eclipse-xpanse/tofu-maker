/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.tofu.maker.models.request.directory;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;
import lombok.Data;

/**
 * Data model for the OpenTofu deploy requests.
 */
@Data
public class OpenTofuDeployFromDirectoryRequest {

    @NotNull
    @Schema(description = "Flag to control if the deployment must only generate the OpenTofu "
            + "or it must also apply the changes.")
    Boolean isPlanOnly;

    @NotNull
    @Schema(description = "Key-value pairs of variables that must be used to execute the "
            + "OpenTofu request.",
            additionalProperties = Schema.AdditionalPropertiesValue.TRUE)
    Map<String, Object> variables;

    @Schema(description = "Key-value pairs of variables that must be injected as environment "
            + "variables to OpenTofu process.",
            additionalProperties = Schema.AdditionalPropertiesValue.TRUE)
    Map<String, String> envVariables = new HashMap<>();
}
