/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.tofu.maker.security.oauth2.config;

import static org.springframework.web.cors.CorsConfiguration.ALL;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.Collections;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.tofu.maker.models.response.Response;
import org.eclipse.xpanse.tofu.maker.models.response.ResultType;
import org.eclipse.xpanse.tofu.maker.security.oauth2.introspector.OauthOpaqueTokenIntrospector;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.frameoptions.XFrameOptionsHeaderWriter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Configuration applied on all web endpoints defined for this application. Any configuration on
 * specific resources is applied in addition to these global rules.
 */
@Slf4j
@Profile("oauth")
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
public class Oauth2WebSecurityConfig {

    @Value("${authorization.token.type:JWT}")
    private String authTokenType;

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuerUri;

    @Value("${spring.security.oauth2.resourceserver.opaquetoken.introspection-uri}")
    private String introspectionUri;

    @Value("${spring.security.oauth2.resourceserver.opaquetoken.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.resourceserver.opaquetoken.client-secret}")
    private String clientSecret;

    @Value("${springwolf.path.base:/queues}")
    private String springwolfPathBase;

    @Resource private Oauth2JwtDecoder oauth2JwtDecoder;

    /**
     * Configures basic security handler per HTTP session.
     *
     * @param http security configuration
     */
    @Bean
    public SecurityFilterChain apiFilterChain(HttpSecurity http) throws Exception {
        // accept cors requests and allow preflight checks
        http.cors(
                httpSecurityCorsConfigurer ->
                        httpSecurityCorsConfigurer.configurationSource(corsConfigurationSource()));

        http.authorizeHttpRequests(
                arc -> {
                    arc.requestMatchers(AntPathRequestMatcher.antMatcher("/swagger-ui/**"))
                            .permitAll();
                    arc.requestMatchers(
                                    AntPathRequestMatcher.antMatcher(springwolfPathBase + "/**"))
                            .permitAll();
                    arc.requestMatchers(AntPathRequestMatcher.antMatcher("/v3/**")).permitAll();
                    arc.requestMatchers(AntPathRequestMatcher.antMatcher("/error")).permitAll();
                    arc.anyRequest().authenticated();
                });

        http.csrf(AbstractHttpConfigurer::disable);

        http.headers(
                headersConfigurer ->
                        headersConfigurer.addHeaderWriter(
                                new XFrameOptionsHeaderWriter(
                                        XFrameOptionsHeaderWriter.XFrameOptionsMode.SAMEORIGIN)));

        // set custom exception handler
        http.exceptionHandling(
                exceptionHandlingConfigurer ->
                        exceptionHandlingConfigurer.authenticationEntryPoint(
                                (httpRequest, httpResponse, authException) -> {
                                    httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                                    httpResponse.setCharacterEncoding("UTF-8");
                                    httpResponse.setContentType(MediaType.APPLICATION_JSON_VALUE);
                                    ObjectMapper objectMapper = new ObjectMapper();
                                    Response responseModel =
                                            Response.errorResponse(
                                                    ResultType.UNAUTHORIZED,
                                                    Collections.singletonList(
                                                            ResultType.UNAUTHORIZED.toValue()));
                                    String resBody = objectMapper.writeValueAsString(responseModel);
                                    PrintWriter printWriter = httpResponse.getWriter();
                                    printWriter.print(resBody);
                                    printWriter.flush();
                                    printWriter.close();
                                }));

        if (StringUtils.equalsIgnoreCase(Oauth2Constants.AUTH_TYPE_TOKEN, authTokenType)) {
            // Config custom OpaqueTokenIntrospector
            http.oauth2ResourceServer(
                    oauth2 ->
                            oauth2.opaqueToken(
                                    opaque ->
                                            opaque.introspector(
                                                    new OauthOpaqueTokenIntrospector(
                                                            introspectionUri,
                                                            clientId,
                                                            clientSecret))));
        }

        if (StringUtils.equalsIgnoreCase(Oauth2Constants.AUTH_TYPE_JWT, authTokenType)) {
            // Config custom JwtAuthenticationConverter
            http.oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwtDecoder()));
        }
        return http.build();
    }

    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowCredentials(true);
        configuration.addAllowedHeader(ALL);
        configuration.addAllowedMethod(ALL);
        configuration.addAllowedOriginPattern(ALL);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    @ConditionalOnProperty(name = "authorization.token.type", havingValue = "JWT")
    JwtDecoder jwtDecoder() {
        return oauth2JwtDecoder.createJwtDecoder(issuerUri);
    }
}
