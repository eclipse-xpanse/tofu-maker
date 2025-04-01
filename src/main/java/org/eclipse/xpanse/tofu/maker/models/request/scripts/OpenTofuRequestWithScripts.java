/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.tofu.maker.models.request.scripts;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.io.Serial;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.eclipse.xpanse.tofu.maker.models.request.OpenTofuRequest;

/** The open tofu request for executing command based on the scripts files. */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "OpenTofu request with scripts files")
public class OpenTofuRequestWithScripts extends OpenTofuRequest {

    @Serial private static final long serialVersionUID = 7464467836284819109L;

    @NotNull
    @NotEmpty
    @Schema(
            description =
                    "Map stores file name and content of all script files for deploy request.")
    private Map<String, String> scriptFiles;
}
