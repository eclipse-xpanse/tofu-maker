/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.tofu.maker.models.request.directory;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.io.Serial;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.eclipse.xpanse.tofu.maker.models.request.webhook.WebhookConfig;

/**
 * The OpenTofu async request for executing command based on the directory where the scripts file
 * exist.
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "OpenTofu async request with directory where scripts files exist")
public class OpenTofuAsyncRequestWithScriptsDirectory extends OpenTofuRequestWithScriptsDirectory {

    @Serial private static final long serialVersionUID = 3604091444011192314L;

    @NotNull
    @Schema(description = "Configuration information of webhook.")
    private WebhookConfig webhookConfig;
}
