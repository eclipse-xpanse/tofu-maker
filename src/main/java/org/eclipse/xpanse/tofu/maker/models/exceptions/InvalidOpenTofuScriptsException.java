/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.tofu.maker.models.exceptions;

/** Defines possible exceptions returned by OpenTofu scripts invalid. */
public class InvalidOpenTofuScriptsException extends RuntimeException {

    public InvalidOpenTofuScriptsException(String message) {
        super(message);
    }
}
