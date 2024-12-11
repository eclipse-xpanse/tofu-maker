/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 *
 */

package org.eclipse.xpanse.tofu.maker.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Caffeine cache configuration class. */
@Slf4j
@Configuration
public class CaffeineCacheConfig {
    public static final String OPENTOFU_VERSIONS_CACHE_NAME = "OPENTOFU_VERSIONS_CACHE";

    /**
     * Config cache manager with caffeine.
     *
     * @return caffeineCacheManager
     */
    @Bean
    public CacheManager caffeineCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.registerCustomCache(OPENTOFU_VERSIONS_CACHE_NAME, getOpenTofuVersionsCache());
        return cacheManager;
    }

    private Cache<Object, Object> getOpenTofuVersionsCache() {
        return Caffeine.newBuilder().build();
    }
}
