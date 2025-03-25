/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.tofu.maker.api.queues;

import java.util.UUID;
import org.eclipse.xpanse.tofu.maker.models.request.directory.OpenTofuRequestWithScriptsDirectory;
import org.eclipse.xpanse.tofu.maker.models.request.git.OpenTofuRequestWithScriptsGitRepo;
import org.eclipse.xpanse.tofu.maker.models.request.scripts.OpenTofuRequestWithScripts;
import org.eclipse.xpanse.tofu.maker.models.response.OpenTofuPlan;
import org.eclipse.xpanse.tofu.maker.models.response.OpenTofuResult;
import org.eclipse.xpanse.tofu.maker.models.response.TofuMakerSystemStatus;
import org.eclipse.xpanse.tofu.maker.models.response.validation.OpenTofuValidationResult;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/** AMQP producer. */
@Profile("amqp")
@Component
public interface AmqpProducer {

    /** Send tofu health check request to amqp queue. */
    void sendOpenTofuHealthCheckRequest(@Payload UUID requestId);

    /**
     * Send tofu request with scripts to amqp queue.
     *
     * @param request request to send.
     */
    void sendOpenTofuRequestWithDirectory(@Payload OpenTofuRequestWithScriptsDirectory request);

    /**
     * Send tofu request with scripts git repo to amqp queue.
     *
     * @param request request to send.
     */
    void sendOpenTofuRequestWithScriptsGitRepo(@Payload OpenTofuRequestWithScriptsGitRepo request);

    /**
     * Send tofu request with scripts to amqp queue.
     *
     * @param request request to send.
     */
    void sendOpenTofuRequestWithScripts(@Payload OpenTofuRequestWithScripts request);

    /**
     * Send tofu health check result to amqp queue.
     *
     * @param result result to send.
     */
    void sendOpenTofuHealthCheckResult(@Payload TofuMakerSystemStatus result);

    /**
     * Send tofu plan result to amqp queue.
     *
     * @param result result to send.
     */
    void sendOpenTofuPlanResult(@Payload OpenTofuPlan result);

    /**
     * Send tofu validation result to amqp queue.
     *
     * @param result result to send.
     */
    void sendOpenTofuValidationResult(@Payload OpenTofuValidationResult result);

    /**
     * Send tofu deployment result to amqp queue.
     *
     * @param result result to send.
     */
    void sendOpenTofuDeploymentResult(@Payload OpenTofuResult result);
}
