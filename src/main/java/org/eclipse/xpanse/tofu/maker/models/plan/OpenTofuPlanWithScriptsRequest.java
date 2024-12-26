/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.tofu.maker.models.plan;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Data model for the generating openTofu plan.
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class OpenTofuPlanWithScriptsRequest extends OpenTofuPlanFromDirectoryRequest {

    @NotNull
    @NotEmpty
    @Schema(
            description =
                    "Map stores file name and content of all script files for generating terraform"
                            + " plan.")
    private Map<String, String> scriptFiles;

}
