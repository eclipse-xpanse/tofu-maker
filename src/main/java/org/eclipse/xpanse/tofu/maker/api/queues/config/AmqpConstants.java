/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.tofu.maker.api.queues.config;

/** Constants for AMQP queues, exchanges, and routing keys. */
public class AmqpConstants {

    /** Name of the queue for OpenTofu health check request. */
    public static final String QUEUE_NAME_FOR_TOFU_HEALTH_CHECK_REQUEST =
            "org.eclipse.tofu.maker.queue.request.health-check";

    /** Name of the queue for OpenTofu request with scripts in directory. */
    public static final String QUEUE_NAME_FOR_TOFU_REQUEST_WITH_DIRECTORY =
            "org.eclipse.tofu.maker.queue.request.directory";

    /** Name of the queue for OpenTofu request with scripts in git repo. */
    public static final String QUEUE_NAME_FOR_TOFU_REQUEST_WITH_GIT =
            "org.eclipse.tofu.maker.queue.request.git";

    /** Name of the queue for OpenTofu request with scripts map. */
    public static final String QUEUE_NAME_FOR_TOFU_REQUEST_WITH_SCRIPTS =
            "org.eclipse.tofu.maker.queue.request.scripts";

    /** Name of the queue for OpenTofu health check results. */
    public static final String QUEUE_NAME_FOR_TOFU_HEALTH_CHECK_RESULT =
            "org.eclipse.tofu.maker.queue.result.health-check";

    /** Name of the queue for OpenTofu health check results. */
    public static final String QUEUE_NAME_FOR_TOFU_PLAN_RESULT =
            "org.eclipse.tofu.maker.queue.result.plan";

    /** Name of the queue for OpenTofu health check results. */
    public static final String QUEUE_NAME_FOR_TOFU_VALIDATION_RESULT =
            "org.eclipse.tofu.maker.queue.result.validation";

    /** Name of the queue for OpenTofu results. */
    public static final String QUEUE_NAME_FOR_TOFU_DEPLOYMENT_RESULT =
            "org.eclipse.tofu.maker.queue.result.deployment";

    /** Exchange name for OpenTofu messages. */
    public static final String EXCHANGE_NAME_FOR_TOFU = "tofu.direct.exchange";

    /** Routing keys for OpenTofu health check request. */
    public static final String ROUTING_KEY_FOR_TOFU_HEALTH_CHECK_REQUEST = "request.health-check";

    /** Routing keys for OpenTofu request with directory. */
    public static final String ROUTING_KEY_FOR_TOFU_REQUEST_WITH_DIRECTORY = "request.directory";

    /** Routing keys for OpenTofu request with scripts. */
    public static final String ROUTING_KEY_FOR_TOFU_REQUEST_WITH_SCRIPTS = "request.scripts";

    /** Routing keys for OpenTofu request with git repo. */
    public static final String ROUTING_KEY_FOR_TOFU_REQUEST_WITH_GIT = "request.git";

    /** Routing keys for result of OpenTofu health check. */
    public static final String ROUTING_KEY_FOR_TOFU_HEALTH_CHECK_RESULT = "result.health-check";

    /** Routing keys for result of OpenTofu plan. */
    public static final String ROUTING_KEY_FOR_TOFU_PLAN_RESULT = "result.plan";

    /** Routing keys for result of OpenTofu validation. */
    public static final String ROUTING_KEY_FOR_TOFU_VALIDATION_RESULT = "result.validation";

    /** Routing keys for result of OpenTofu deployment. */
    public static final String ROUTING_KEY_FOR_TOFU_DEPLOYMENT_RESULT = "result.deployment";

    private AmqpConstants() {
        // Private constructor to prevent instantiation
    }
}
