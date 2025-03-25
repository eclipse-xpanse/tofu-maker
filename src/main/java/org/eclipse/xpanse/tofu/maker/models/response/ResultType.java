/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.tofu.maker.models.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/** Result codes for the REST API. */
public enum ResultType {
    BAD_PARAMETERS("Parameters Invalid"),
    UNPROCESSABLE_ENTITY("Unprocessable Entity"),
    OPENTOFU_EXECUTION_FAILED("OpenTofu Execution Failed"),
    UNSUPPORTED_ENUM_VALUE("Unsupported Enum Value"),
    SERVICE_UNAVAILABLE("Service Unavailable"),
    UNAUTHORIZED("Unauthorized"),
    INVALID_GIT_REPO_DETAILS("Invalid Git Repo Details"),
    INVALID_OPENTOFU_TOOL("Invalid OpenTofu Tool"),
    INVALID_OPENTOFU_SCRIPTS("Invalid OpenTofu Scripts"),
    INVALID_OPENTOFU_REQUEST("Invalid OpenTofu Request"),
    SEND_AMQP_MESSAGE_FAILED("Send Amqp Message Failed"),
    RESULT_ALREADY_RETURNED_OR_REQUEST_ID_INVALID("Result Already Returned or RequestId Invalid");

    private final String value;

    ResultType(String value) {
        this.value = value;
    }

    /** For ResultType deserialize. */
    @JsonValue
    public String toValue() {
        return this.value;
    }

    /** For ResultType serialize. */
    @JsonCreator
    public ResultType getByValue(String name) {
        for (ResultType resultType : values()) {
            if (resultType.value.equals(StringUtils.lowerCase(name))) {
                return resultType;
            }
        }
        return null;
    }
}
