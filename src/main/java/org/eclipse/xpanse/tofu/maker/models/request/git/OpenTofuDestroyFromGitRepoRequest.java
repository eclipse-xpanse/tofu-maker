/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.tofu.maker.models.request.git;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.eclipse.xpanse.tofu.maker.models.request.directory.OpenTofuDestroyFromDirectoryRequest;

/**
 * Data model for open tofu destroy requests using scripts from a GIT Repo.
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class OpenTofuDestroyFromGitRepoRequest extends OpenTofuDestroyFromDirectoryRequest {

    @Schema(description = "GIT Repo details from where the scripts can be fetched.")
    OpenTofuScriptGitRepoDetails gitRepoDetails;

    @NotNull
    @Schema(description = "The .tfState file content after deployment")
    private String tfState;
}
