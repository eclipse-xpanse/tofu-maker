/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.tofu.maker.models.response.validation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/** This data class holds the diagnostics details returned by the OpenTofu validator. */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenTofuValidateDiagnostics {

    @Schema(description = "Detail of validation error.")
    private String detail;
}
