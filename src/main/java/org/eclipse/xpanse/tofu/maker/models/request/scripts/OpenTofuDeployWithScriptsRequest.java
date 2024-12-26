/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.tofu.maker.models.request.scripts;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.eclipse.xpanse.tofu.maker.models.request.directory.OpenTofuDeployFromDirectoryRequest;

/** OpenTofu uses the request object deployed by the script. */
@EqualsAndHashCode(callSuper = true)
@Data
public class OpenTofuDeployWithScriptsRequest extends OpenTofuDeployFromDirectoryRequest {

    @NotNull
    @NotEmpty
    @Schema(
            description =
                    "Map stores file name and content of all script files for deploy request.")
    private Map<String, String> scriptFiles;
}
