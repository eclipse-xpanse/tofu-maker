/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.tofu.maker.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Configuration springdoc openAPI. */
@Configuration
public class SpringDocOpenApiConfig {

    @Value("${app.version:1.0.0}")
    private String version;

    /** Configuration openAPI. */
    @Bean
    public OpenAPI configOpenApi() {
        return new OpenAPI()
                .info(
                        new Info()
                                .title("Tofu-Maker API")
                                .description("RESTful Services to interact with opentofu CLI")
                                .version(version));
    }
}
