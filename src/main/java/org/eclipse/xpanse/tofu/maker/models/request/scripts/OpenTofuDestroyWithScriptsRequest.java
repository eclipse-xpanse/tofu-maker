/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.tofu.maker.models.request.scripts;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.List;
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
    UUID requestId;

    @NotNull
    @Schema(description = "List of script files for destroy requests deployed via scripts")
    private List<String> scripts;

    @NotNull
    @Schema(description = "The .tfState file content after deployment")
    private String tfState;
}
