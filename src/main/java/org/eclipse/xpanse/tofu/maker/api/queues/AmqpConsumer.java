/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.tofu.maker.api.queues;

import java.util.UUID;
import org.eclipse.xpanse.tofu.maker.models.request.directory.OpenTofuRequestWithScriptsDirectory;
import org.eclipse.xpanse.tofu.maker.models.request.git.OpenTofuRequestWithScriptsGitRepo;
import org.eclipse.xpanse.tofu.maker.models.request.scripts.OpenTofuRequestWithScripts;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/** AMQP consumer. */
@Profile("amqp")
@Component
public interface AmqpConsumer {

    /**
     * Get openTofu health check request and process it.
     *
     * @param requestId request id
     */
    void processOpenTofuHealthCheckRequestFromQueue(@Payload UUID requestId);

    /**
     * Get openTofu request with scripts directory from queue and process it.
     *
     * @param request request
     */
    void processOpenTofuRequestWithDirectoryFromQueue(
            @Payload OpenTofuRequestWithScriptsDirectory request);

    /**
     * Get openTofu request with scripts directory from queue and process it.
     *
     * @param request request
     */
    void processOpenTofuRequestWithGitFromQueue(@Payload OpenTofuRequestWithScriptsGitRepo request);

    /**
     * Get openTofu request with scripts directory from queue and process it.
     *
     * @param request request
     */
    void processOpenTofuRequestWithScriptsFromQueue(@Payload OpenTofuRequestWithScripts request);
}
