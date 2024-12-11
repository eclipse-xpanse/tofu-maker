/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.tofu.maker.models.exceptions;

/** Defines possible exceptions returned by OpenTofu version invalid. */
public class InvalidOpenTofuToolException extends RuntimeException {

    public InvalidOpenTofuToolException(String message) {
        super(message);
    }
}
