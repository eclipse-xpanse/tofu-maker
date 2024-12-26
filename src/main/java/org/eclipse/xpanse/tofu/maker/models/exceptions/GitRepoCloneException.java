/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.tofu.maker.models.exceptions;

/** Exception thrown when GIT clone command fails. */
public class GitRepoCloneException extends RuntimeException {

    public GitRepoCloneException(String message) {
        super(message);
    }
}
