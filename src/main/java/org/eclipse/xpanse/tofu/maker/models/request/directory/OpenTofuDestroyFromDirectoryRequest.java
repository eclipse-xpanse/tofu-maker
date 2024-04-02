/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.tofu.maker.models.request.directory;

import static io.swagger.v3.oas.annotations.media.Schema.AdditionalPropertiesValue.TRUE;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import org.eclipse.xpanse.tofu.maker.models.enums.DeploymentScenario;

/**
 * Data model for the OpenTofu destroy requests.
 */
@Data
public class OpenTofuDestroyFromDirectoryRequest {

    @Schema(description = "This value can be set by the client if they wish to know the type of"
            + "request for which the callback response is generated from tofu-maker. There will be"
            + "no difference in the way request is executed. This information is only set in the"
            + "callback response again for the client to handle the callback response accordingly.")
    DeploymentScenario deploymentScenario;

    @NotNull
    @Schema(description = "Key-value pairs of regular variables that must be used to execute the "
            + "OpenTofu request.",
            additionalProperties = TRUE)
    Map<String, Object> variables;

    @Schema(description = "Key-value pairs of variables that must be injected as environment "
            + "variables to OpenTofu process.",
            additionalProperties = TRUE)
    Map<String, String> envVariables = new HashMap<>();
}
