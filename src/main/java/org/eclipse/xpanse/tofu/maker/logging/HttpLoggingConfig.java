/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.tofu.maker.logging;

import java.util.List;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/** Reads HTTP logging related configuration from spring context. */
@Configuration
public class HttpLoggingConfig {
    private static boolean isHttpLoggingEnabled;
    @Getter private static List<String> excludedUris;

    @Value("${http.logging.enabled:true}")
    public void setIsHttpLoggingEnabled(boolean isHttpLoggingEnabled) {
        HttpLoggingConfig.isHttpLoggingEnabled = isHttpLoggingEnabled;
    }

    @Value("${http.logging.exclude.uri:}#{T(java.util.Collections).emptyList()}")
    public void setExcludedUris(List<String> excludedUris) {
        HttpLoggingConfig.excludedUris = excludedUris;
    }

    public static boolean isHttpLoggingEnabled() {
        return isHttpLoggingEnabled;
    }
}
