/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.tofu.maker.models.request.git;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.eclipse.xpanse.tofu.maker.models.request.webhook.WebhookConfig;

/** Data model for open tofu async modify requests using scripts from a GIT Repo. */
@EqualsAndHashCode(callSuper = true)
@Data
public class OpenTofuAsyncModifyFromGitRepoRequest extends OpenTofuModifyFromGitRepoRequest {

    @NotNull
    @Schema(description = "Configuration information of webhook.")
    private WebhookConfig webhookConfig;
}
