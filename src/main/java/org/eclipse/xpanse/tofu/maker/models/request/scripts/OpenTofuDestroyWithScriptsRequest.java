/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.tofu.maker.models.request.scripts;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import java.util.UUID;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.eclipse.xpanse.tofu.maker.models.request.directory.OpenTofuDestroyFromDirectoryRequest;

/**
 * OpenTofu uses the request object destroy by the script.
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class OpenTofuDestroyWithScriptsRequest extends OpenTofuDestroyFromDirectoryRequest {

    @Schema(description = "Id of the request.")
    private UUID requestId;

    @NotNull
    @NotEmpty
    @Schema(
            description =
                    "Map stores file name and content of all script files for destroy request")
    private Map<String, String> scriptFiles;

    @NotNull
    @Schema(description = "The .tfState file content after deployment")
    private String tfState;
}
