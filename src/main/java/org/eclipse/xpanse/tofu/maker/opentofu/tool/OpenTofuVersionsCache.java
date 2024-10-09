/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.tofu.maker.opentofu.tool;


import static org.eclipse.xpanse.tofu.maker.cache.CaffeineCacheConfig.OPENTOFU_VERSIONS_CACHE_NAME;

import jakarta.annotation.Resource;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

/**
 * Bean to update the cache of versions of OpenTofu.
 */
@Slf4j
@Component
public class OpenTofuVersionsCache {

    @Resource
    private OpenTofuVersionsFetcher versionsFetcher;

    /**
     * Get the available versions of OpenTofu.
     *
     * @return Set of available versions.
     */
    @Cacheable(value = OPENTOFU_VERSIONS_CACHE_NAME, key = "'all'")
    public Set<String> getAvailableVersions() {
        try {
            return versionsFetcher.fetchAvailableVersionsFromOpenTofuWebsite();
        } catch (Exception e) {
            return versionsFetcher.getDefaultVersionsFromConfig();
        }
    }

    /**
     * Update the cache of versions of OpenTofu.
     *
     * @param versions List of available versions.
     */
    @Cacheable(value = OPENTOFU_VERSIONS_CACHE_NAME, key = "'all'")
    public void updateCachedVersions(Set<String> versions) {
        log.info("Updated OpenTofu versions cache with versions:{}.", versions);
    }

}
