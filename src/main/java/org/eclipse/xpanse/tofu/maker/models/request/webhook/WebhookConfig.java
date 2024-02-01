/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.tofu.maker.models.request.webhook;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.eclipse.xpanse.tofu.maker.models.enums.AuthType;

/**
 * Configuration information class of webhook.
 */
@Data
public class WebhookConfig {

    @NotNull
    @Schema(description = "Callback address after deployment/destroy is completed.")
    private String url;

    @NotNull
    @Schema(description = "The permission type when calling back.")
    private AuthType authType;
}
