/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.tofu.maker.security.oauth2.introspector;


import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;
import org.springframework.security.oauth2.server.resource.introspection.SpringOpaqueTokenIntrospector;

/**
 * Customize the OAuth2AuthoritiesOpaqueTokenIntrospector implements OpaqueTokenIntrospector.
 */
@Slf4j
public class OauthOpaqueTokenIntrospector implements OpaqueTokenIntrospector {

    private final OpaqueTokenIntrospector opaqueTokenIntrospector;

    /**
     * Constructor.
     *
     * @param introspectionUri The url of IAM server to verify token.
     * @param clientId         The id of api client created in IAM server.
     * @param clientSecret     The secret of api client created in IAM server.
     */
    public OauthOpaqueTokenIntrospector(String introspectionUri,
                                        String clientId,
                                        String clientSecret) {
        opaqueTokenIntrospector =
                new SpringOpaqueTokenIntrospector(introspectionUri, clientId, clientSecret);
    }

    /**
     * Verify token.
     *
     * @param token The token of current user.
     * @return OAuth2AuthenticatedPrincipal
     */
    public OAuth2AuthenticatedPrincipal introspect(String token) {
        return this.opaqueTokenIntrospector.introspect(token);
    }

}