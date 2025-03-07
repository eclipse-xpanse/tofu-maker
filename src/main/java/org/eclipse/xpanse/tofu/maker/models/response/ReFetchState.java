/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.tofu.maker.models.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

/** ReFetch state for the REST API. */
public enum ReFetchState {
    OK("OK"),
    ORDER_IN_PROGRESS("OrderInProgress"),
    RESULT_NOT_FOUND("ResultNotFound"),
    RESULT_PARSE_FAILED("ResultParseFailed");
    private final String state;

    ReFetchState(String state) {
        this.state = state;
    }

    /** For ReFetchState deserialize. */
    @JsonValue
    public String toValue() {
        return this.state;
    }

    /** For ReFetchState serialize. */
    @JsonCreator
    public ReFetchState getByValue(String name) {
        for (ReFetchState state : values()) {
            if (StringUtils.equalsIgnoreCase(state.state, name)) {
                return state;
            }
        }
        return null;
    }
}
