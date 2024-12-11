/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.tofu.maker.models.validation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;

/** Defines the openTofu validation result. */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenTofuValidationResult {

    @NotNull
    @Schema(description = "Defines if the scripts is valid.")
    private boolean valid;

    @Schema(description = "The version of the OpenTofu binary used to execute scripts.")
    private String openTofuVersionUsed;

    @Schema(description = "List of validation errors.")
    private List<OpenTofuValidateDiagnostics> diagnostics;
}
