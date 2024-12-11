/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.tofu.maker.models.exceptions;

/** Used to indicate OpenTofu health check anomalies. */
public class OpenTofuHealthCheckException extends RuntimeException {

    public OpenTofuHealthCheckException(String message) {
        super(message);
    }
}
