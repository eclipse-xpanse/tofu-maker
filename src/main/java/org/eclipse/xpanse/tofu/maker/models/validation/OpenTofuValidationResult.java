/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.tofu.maker.models.validation;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import lombok.Data;

/**
 * Defines the openTofu validation result.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenTofuValidationResult {

    private boolean valid;
    private List<OpenTofuValidateDiagnostics> diagnostics;
}
