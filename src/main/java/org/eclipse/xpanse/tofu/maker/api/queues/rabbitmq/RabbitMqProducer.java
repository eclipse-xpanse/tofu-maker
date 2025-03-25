/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.tofu.maker.api.queues.rabbitmq;

import io.github.springwolf.bindings.amqp.annotations.AmqpAsyncOperationBinding;
import io.github.springwolf.core.asyncapi.annotations.AsyncOperation;
import io.github.springwolf.core.asyncapi.annotations.AsyncPublisher;
import jakarta.annotation.Resource;
import java.util.Objects;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.tofu.maker.api.queues.AmqpProducer;
import org.eclipse.xpanse.tofu.maker.api.queues.config.AmqpConstants;
import org.eclipse.xpanse.tofu.maker.models.request.directory.OpenTofuRequestWithScriptsDirectory;
import org.eclipse.xpanse.tofu.maker.models.request.git.OpenTofuRequestWithScriptsGitRepo;
import org.eclipse.xpanse.tofu.maker.models.request.scripts.OpenTofuRequestWithScripts;
import org.eclipse.xpanse.tofu.maker.models.response.OpenTofuPlan;
import org.eclipse.xpanse.tofu.maker.models.response.OpenTofuResult;
import org.eclipse.xpanse.tofu.maker.models.response.TofuMakerSystemStatus;
import org.eclipse.xpanse.tofu.maker.models.response.validation.OpenTofuValidationResult;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/** implementation of AmqpProducer interface for RabbitMQ. */
@Slf4j
@Component
@Profile("amqp")
@ConditionalOnProperty(name = "spring.amqp.provider", havingValue = "rabbitmq")
public class RabbitMqProducer implements AmqpProducer {

    @Qualifier("customRabbitTemplate")
    @Resource
    private RabbitTemplate rabbitTemplate;

    @AsyncPublisher(
            operation =
                    @AsyncOperation(
                            channelName = AmqpConstants.QUEUE_NAME_FOR_TOFU_HEALTH_CHECK_REQUEST,
                            description = "Send openTofu health check request to rabbitmq queue."))
    @AmqpAsyncOperationBinding
    @Override
    public void sendOpenTofuHealthCheckRequest(@Payload UUID requestId) {
        log.info("Received openTofu health check request with id {}", requestId);
        sendMessageViaExchange(
                AmqpConstants.ROUTING_KEY_FOR_TOFU_HEALTH_CHECK_REQUEST,
                requestId,
                "Health check request " + requestId);
    }

    @AsyncPublisher(
            operation =
                    @AsyncOperation(
                            channelName = AmqpConstants.QUEUE_NAME_FOR_TOFU_REQUEST_WITH_DIRECTORY,
                            description = "Send openTofu request with scripts to rabbitmq queue."))
    @AmqpAsyncOperationBinding
    @Override
    public void sendOpenTofuRequestWithDirectory(
            @Payload OpenTofuRequestWithScriptsDirectory request) {
        log.info(
                "Received openTofu directory request with type {} and id {}",
                request.getRequestType(),
                request.getRequestId());
        sendMessageViaExchange(
                AmqpConstants.ROUTING_KEY_FOR_TOFU_REQUEST_WITH_DIRECTORY,
                request,
                "OpenTofu directory request " + request.getRequestId());
    }

    @AsyncPublisher(
            operation =
                    @AsyncOperation(
                            channelName = AmqpConstants.QUEUE_NAME_FOR_TOFU_REQUEST_WITH_SCRIPTS,
                            description = "Send openTofu request with scripts to rabbitmq queue."))
    @AmqpAsyncOperationBinding
    @Override
    public void sendOpenTofuRequestWithScripts(@Payload OpenTofuRequestWithScripts request) {
        log.info(
                "Received openTofu scripts request with type {} and id {}",
                request.getRequestType(),
                request.getRequestId());
        sendMessageViaExchange(
                AmqpConstants.ROUTING_KEY_FOR_TOFU_REQUEST_WITH_SCRIPTS,
                request,
                "OpenTofu scripts request " + request.getRequestId());
    }

    @AsyncPublisher(
            operation =
                    @AsyncOperation(
                            channelName = AmqpConstants.QUEUE_NAME_FOR_TOFU_REQUEST_WITH_GIT,
                            description =
                                    "Send openTofu request with scripts git repo to rabbitmq"
                                            + " queue."))
    @AmqpAsyncOperationBinding
    @Override
    public void sendOpenTofuRequestWithScriptsGitRepo(
            @Payload OpenTofuRequestWithScriptsGitRepo request) {
        log.info(
                "Received openTofu git request with type {} and id {}",
                request.getRequestType(),
                request.getRequestId());
        sendMessageViaExchange(
                AmqpConstants.ROUTING_KEY_FOR_TOFU_REQUEST_WITH_GIT,
                request,
                "OpenTofu git request " + request.getRequestId());
    }

    @Override
    public void sendOpenTofuHealthCheckResult(@Payload TofuMakerSystemStatus result) {
        sendMessageViaExchange(
                AmqpConstants.ROUTING_KEY_FOR_TOFU_HEALTH_CHECK_RESULT,
                result,
                "OpenTofu health check result " + result.getRequestId());
    }

    @Override
    public void sendOpenTofuPlanResult(@Payload OpenTofuPlan result) {
        sendMessageViaExchange(
                AmqpConstants.ROUTING_KEY_FOR_TOFU_PLAN_RESULT,
                result,
                "OpenTofu plan result " + result.getRequestId());
    }

    @Override
    public void sendOpenTofuValidationResult(@Payload OpenTofuValidationResult result) {
        sendMessageViaExchange(
                AmqpConstants.ROUTING_KEY_FOR_TOFU_VALIDATION_RESULT,
                result,
                "OpenTofu validation result " + result.getRequestId());
    }

    @Override
    public void sendOpenTofuDeploymentResult(@Payload OpenTofuResult result) {
        sendMessageViaExchange(
                AmqpConstants.ROUTING_KEY_FOR_TOFU_DEPLOYMENT_RESULT,
                result,
                "OpenTofu deployment result " + result.getRequestId());
    }

    private <T> void sendMessageViaExchange(String routingKey, T message, String logPrefix) {
        try {
            if (Objects.isNull(message)) {
                throw new IllegalArgumentException("Message cannot be null");
            }
            if (StringUtils.isBlank(routingKey)) {
                throw new IllegalArgumentException("Routing key cannot be empty");
            }
            rabbitTemplate.convertAndSend(
                    AmqpConstants.EXCHANGE_NAME_FOR_TOFU, routingKey, message);
            log.debug(
                    "{} sent via exchange {} with key {} completed",
                    logPrefix,
                    AmqpConstants.EXCHANGE_NAME_FOR_TOFU,
                    routingKey);
        } catch (AmqpException e) {
            String errorMsg =
                    String.format(
                            "Failed to send message to exchange: %s (key: %s). Error: %s",
                            AmqpConstants.EXCHANGE_NAME_FOR_TOFU, routingKey, e.getMessage());
            log.error("{}", errorMsg, e);
            throw new AmqpException(errorMsg, e);
        }
    }
}
