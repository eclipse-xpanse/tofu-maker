/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.tofu.maker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;

/** Main entry class to tofu-maker. This class can be directly executed to start the server. */
@EnableRetry
@EnableAsync
@EnableCaching
@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
public class OpenTofuMakerApplication {

    public static void main(String[] args) {
        SpringApplication.run(OpenTofuMakerApplication.class, args);
    }
}
