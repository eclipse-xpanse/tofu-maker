/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.tofu.maker.models.response.validation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;
import lombok.Data;

/** Defines the openTofu validation result. */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenTofuValidationResult implements Serializable {

    @Serial private static final long serialVersionUID = 5757817675719071741L;

    @NotNull
    @Schema(description = "ID of the request.")
    private UUID requestId;

    @NotNull
    @Schema(description = "Defines if the scripts is valid.")
    private boolean valid;

    @Schema(description = "The version of the OpenTofu binary used to execute scripts.")
    private String openTofuVersionUsed;

    @Schema(description = "List of validation errors.")
    private List<OpenTofuValidateDiagnostics> diagnostics;
}
