/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.tofu.maker.models.plan;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.eclipse.xpanse.tofu.maker.models.request.git.OpenTofuScriptGitRepoDetails;

/**
 * Data model for the generating open tofu plan using OpenTofu scripts from a GIT repo.
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class OpenTofuPlanFromGitRepoRequest extends OpenTofuPlanFromDirectoryRequest {

    @Schema(description = "GIT Repo details from where the scripts can be fetched.")
    OpenTofuScriptGitRepoDetails gitRepoDetails;
}
