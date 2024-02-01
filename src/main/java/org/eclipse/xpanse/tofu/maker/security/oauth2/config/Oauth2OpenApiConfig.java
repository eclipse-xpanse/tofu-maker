/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.tofu.maker.security.oauth2.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.OAuthFlow;
import io.swagger.v3.oas.annotations.security.OAuthFlows;
import io.swagger.v3.oas.annotations.security.OAuthScope;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;


/**
 * Configuration springdoc security OAuth2.
 */
@Profile("oauth")
@OpenAPIDefinition(
        info = @Info(
                title = "Tofu-Maker API",
                description = "RESTful Services to interact with Tofu-Maker runtime",
                version = "${app.version}"
        ),
        security = @SecurityRequirement(name = "OAuth2Flow",
                scopes = {Oauth2Constants.OPENID_SCOPE})
)
@SecurityScheme(
        name = "OAuth2Flow",
        type = SecuritySchemeType.OAUTH2,
        flows = @OAuthFlows(authorizationCode =
        @OAuthFlow(
                authorizationUrl = "${springdoc.oAuthFlow.authorizationUrl}",
                tokenUrl = "${springdoc.oAuthFlow.tokenUrl}",
                scopes = {
                        @OAuthScope(name = Oauth2Constants.OPENID_SCOPE,
                                description = "mandatory must be selected.")
                }
        )
        )
)
@Configuration
public class Oauth2OpenApiConfig {
}
