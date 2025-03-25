/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.tofu.maker.models.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.tofu.maker.models.exceptions.UnsupportedEnumValueException;

/** The types of open tofu request. */
public enum RequestType {
    VALIDATE("validate"),
    PLAN("plan"),
    DEPLOY("deploy"),
    MODIFY("modify"),
    DESTROY("destroy");

    private final String type;

    RequestType(String type) {
        this.type = type;
    }

    /** Convert string to RequestType. */
    @JsonCreator
    public RequestType getByValue(String value) {
        for (RequestType type : values()) {
            if (StringUtils.equalsIgnoreCase(type.type, value)) {
                return type;
            }
        }
        throw new UnsupportedEnumValueException(
                String.format("RequestType value %s is not supported.", value));
    }

    /** For RequestType deserialize. */
    @JsonValue
    public String toValue() {
        return this.type;
    }
}
