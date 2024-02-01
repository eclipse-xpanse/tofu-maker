/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.tofu.maker.models.request.git;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Data model for defining the GIT repo information to fetch the scripts.
 */
@Data
public class OpenTofuScriptGitRepoDetails {

    @NotNull
    @Schema(description = "url of the GIT repo. This repo will be cloned.")
    private String repoUrl;
    @NotNull
    @Schema(description = "Branch to be checked-out after the repo is cloned.")
    private String branch;
    @Schema(description = "Location of the scripts. "
            + "If not provided, the scripts will be executed from root folder of the repo.")
    private String scriptPath;
}
