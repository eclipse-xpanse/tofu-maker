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
import org.eclipse.xpanse.tofu.maker.models.enums.DeploymentScenario;

/**
 * Data model for the OpenTofu modify requests.
 */
@Data
public class OpenTofuModifyFromDirectoryRequest {

    @NotNull
    @Schema(description = "Flag to control if the deployment must only generate the OpenTofu "
            + "or it must also apply the changes.")
    Boolean isPlanOnly;

    @Schema(description = "This value can be set by the client if they wish to know the type of"
            + "request for which the callback response is generated from tofu-maker. There will be"
            + "no difference in the way request is executed. This information is only set in the"
            + "callback response again for the client to handle the callback response accordingly.")
    DeploymentScenario deploymentScenario;

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
