/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.tofu.maker.models.validation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * This data class holds the diagnostics details returned by the OpenTofu validator.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenTofuValidateDiagnostics {

    private String detail;

}
