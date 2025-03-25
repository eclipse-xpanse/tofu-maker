/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.tofu.maker.models.exceptions;

/** Defines possible exceptions returned by OpenTofu request invalid. */
public class InvalidOpenTofuRequestException extends RuntimeException {

    public InvalidOpenTofuRequestException(String message) {
        super(message);
    }
}
