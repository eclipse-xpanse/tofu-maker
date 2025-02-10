/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.tofu.maker.models.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.tofu.maker.models.exceptions.UnsupportedEnumValueException;

/**
 * The authentication type class when calling back. This is not used at moment. It is kept for
 * future purpose when clients plan to support multiple authentication types in concurrently.
 */
public enum AuthType {
    NONE("none"),
    OAUTH2("oauth"),
    HMAC("hmac");

    private final String authType;

    AuthType(String authType) {
        this.authType = authType;
    }

    /** Convert string to enum object. */
    @JsonCreator
    public AuthType getByValue(String value) {
        for (AuthType authType : values()) {
            if (authType.authType.equals(StringUtils.upperCase(value))) {
                return authType;
            }
        }
        throw new UnsupportedEnumValueException(
                String.format("HealthStatus value %s is not supported.", value));
    }

    /** For RuntimeState deserialize. */
    @JsonValue
    public String toValue() {
        return this.authType;
    }
}
