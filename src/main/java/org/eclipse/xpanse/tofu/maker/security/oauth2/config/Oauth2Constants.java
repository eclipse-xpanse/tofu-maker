/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.tofu.maker.security.oauth2.config;

/** Constants for OAuth2 Authorization. */
public class Oauth2Constants {

    /** Auth token type: JWT. */
    public static final String AUTH_TYPE_JWT = "JWT";

    /** Auth token type: OpaqueToken. */
    public static final String AUTH_TYPE_TOKEN = "OpaqueToken";

    /** Mandatory scope to request the profile of the user. */
    public static final String OPENID_SCOPE = "openid";
}
