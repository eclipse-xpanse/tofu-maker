/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.tofu.maker.models;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.eclipse.xpanse.tofu.maker.models.enums.HealthStatus;

/**
 * Describes health status of the system.
 */
@Data
public class OpenTofuMakerSystemStatus {

    @NotNull
    @Schema(description = "The health status of api service.")
    private HealthStatus healthStatus;
}