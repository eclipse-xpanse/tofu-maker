/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.tofu.maker.models.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.io.Serial;
import java.io.Serializable;
import java.util.Map;
import java.util.UUID;
import lombok.Data;
import org.eclipse.xpanse.tofu.maker.models.enums.RequestType;
import org.eclipse.xpanse.tofu.maker.models.request.directory.OpenTofuAsyncRequestWithScriptsDirectory;
import org.eclipse.xpanse.tofu.maker.models.request.directory.OpenTofuRequestWithScriptsDirectory;
import org.eclipse.xpanse.tofu.maker.models.request.git.OpenTofuAsyncRequestWithScriptsGitRepo;
import org.eclipse.xpanse.tofu.maker.models.request.git.OpenTofuRequestWithScriptsGitRepo;
import org.eclipse.xpanse.tofu.maker.models.request.scripts.OpenTofuAsyncRequestWithScripts;
import org.eclipse.xpanse.tofu.maker.models.request.scripts.OpenTofuRequestWithScripts;
import org.eclipse.xpanse.tofu.maker.opentofu.tool.OpenTofuVersionsHelper;

/** Data model for the OpenTofu request. */
@Data
@Schema(
        description = "OpenTofu request base",
        subTypes = {
            OpenTofuRequestWithScriptsDirectory.class,
            OpenTofuAsyncRequestWithScriptsDirectory.class,
            OpenTofuRequestWithScripts.class,
            OpenTofuAsyncRequestWithScripts.class,
            OpenTofuRequestWithScriptsGitRepo.class,
            OpenTofuAsyncRequestWithScriptsGitRepo.class,
        })
public abstract class OpenTofuRequest implements Serializable {

    @Serial private static final long serialVersionUID = 10696793105264423L;

    @Schema(description = "Id of the request.")
    @NotNull
    private UUID requestId;

    @NotNull
    @Schema(description = "Type of the openTofu request.")
    private RequestType requestType;

    @NotNull
    @NotBlank
    @Pattern(regexp = OpenTofuVersionsHelper.OPENTOFU_REQUIRED_VERSION_REGEX)
    @Schema(description = "The required version of openTofu which will execute the scripts.")
    private String openTofuVersion;

    @NotNull
    @Schema(
            description =
                    "Flag to control if the deployment must only generate the openTofu "
                            + "or it must also apply the changes.")
    private Boolean isPlanOnly;

    @NotNull
    @Schema(
            description =
                    "Key-value pairs of variables that must be used to execute the "
                            + "OpenTofu request.")
    private Map<String, Object> variables;

    @Schema(
            description =
                    "Key-value pairs of variables that must be injected as environment "
                            + "variables to openTofu process.")
    private Map<String, String> envVariables;

    @Schema(description = "OpenTofu state as a string.")
    private String tfState;
}
