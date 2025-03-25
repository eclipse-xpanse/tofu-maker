/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.tofu.maker.models.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;
import lombok.Data;
import org.eclipse.xpanse.tofu.maker.models.enums.HealthStatus;

/** Describes health status of the system. */
@Data
public class TofuMakerSystemStatus implements Serializable {

    @Serial private static final long serialVersionUID = -1815652331256247643L;

    @NotNull
    @Schema(description = "ID of the request.")
    private UUID requestId;

    @NotNull
    @Schema(description = "The health status of api service.")
    private HealthStatus healthStatus;

    @NotBlank
    @Schema(description = "The service type of tofu-maker.")
    private String serviceType;

    @NotBlank
    @Schema(description = "The url of tofu-maker service.")
    private String serviceUrl;

    @Schema(description = "The error message of tofu-maker service.")
    private String errorMessage;
}
