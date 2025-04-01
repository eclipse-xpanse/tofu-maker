/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.tofu.maker.api.queues.rabbitmq;

import jakarta.annotation.Resource;
import java.util.Objects;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.tofu.maker.api.queues.AmqpConsumer;
import org.eclipse.xpanse.tofu.maker.api.queues.config.AmqpConstants;
import org.eclipse.xpanse.tofu.maker.models.enums.HealthStatus;
import org.eclipse.xpanse.tofu.maker.models.exceptions.UnsupportedEnumValueException;
import org.eclipse.xpanse.tofu.maker.models.request.OpenTofuRequest;
import org.eclipse.xpanse.tofu.maker.models.request.directory.OpenTofuRequestWithScriptsDirectory;
import org.eclipse.xpanse.tofu.maker.models.request.git.OpenTofuRequestWithScriptsGitRepo;
import org.eclipse.xpanse.tofu.maker.models.request.scripts.OpenTofuRequestWithScripts;
import org.eclipse.xpanse.tofu.maker.models.response.TofuMakerSystemStatus;
import org.eclipse.xpanse.tofu.maker.opentofu.service.OpenTofuRequestService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Profile;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/** Implementation of AmqpConsumer interface for RabbitMQ. */
@Slf4j
@Component
@Profile("amqp")
@ConditionalOnProperty(name = "spring.amqp.provider", havingValue = "rabbitmq")
public class RabbitMqConsumer implements AmqpConsumer {

    @Resource private RabbitMqProducer producer;

    @Lazy @Resource private OpenTofuRequestService requestService;

    @Value("${springwolf.docket.servers.amqp-server.protocol}")
    private String serverProtocol;

    @Value("${springwolf.docket.servers.amqp-server.host}")
    private String serverUrl;

    @RabbitListener(
            queues = AmqpConstants.QUEUE_NAME_FOR_TOFU_HEALTH_CHECK_REQUEST,
            containerFactory = "customRabbitListenerContainerFactory")
    @Override
    public void processOpenTofuHealthCheckRequestFromQueue(@Payload UUID requestId) {
        log.info("Processing received health check request with id {}", requestId);
        TofuMakerSystemStatus result = null;
        try {
            result = requestService.healthCheck(requestId);
        } catch (Exception e) {
            log.error("Failed to process health check request with id {}", requestId, e);
            result = new TofuMakerSystemStatus();
            result.setRequestId(requestId);
            result.setHealthStatus(HealthStatus.NOK);
            result.setErrorMessage(e.getMessage());
        }
        result.setServiceType(serverProtocol);
        result.setServiceUrl(serverUrl);
        producer.sendOpenTofuHealthCheckResult(result);
    }

    @RabbitListener(
            queues = AmqpConstants.QUEUE_NAME_FOR_TOFU_REQUEST_WITH_DIRECTORY,
            containerFactory = "customRabbitListenerContainerFactory")
    @Override
    public void processOpenTofuRequestWithDirectoryFromQueue(
            @Payload OpenTofuRequestWithScriptsDirectory request) {
        handleOpenTofuRequestAndSendResult(request);
    }

    @RabbitListener(
            queues = AmqpConstants.QUEUE_NAME_FOR_TOFU_REQUEST_WITH_GIT,
            containerFactory = "customRabbitListenerContainerFactory")
    @Override
    public void processOpenTofuRequestWithGitFromQueue(
            @Payload OpenTofuRequestWithScriptsGitRepo request) {
        handleOpenTofuRequestAndSendResult(request);
    }

    @RabbitListener(
            queues = AmqpConstants.QUEUE_NAME_FOR_TOFU_REQUEST_WITH_SCRIPTS,
            containerFactory = "customRabbitListenerContainerFactory")
    @Override
    public void processOpenTofuRequestWithScriptsFromQueue(
            @Payload OpenTofuRequestWithScripts request) {
        handleOpenTofuRequestAndSendResult(request);
    }

    private void handleOpenTofuRequestAndSendResult(OpenTofuRequest request) {
        try {
            processRequestByType(request);
        } catch (Exception e) {
            sendErrorResultToQueue(Objects.requireNonNull(request), e);
        }
    }

    private void processRequestByType(OpenTofuRequest request) {
        switch (request.getRequestType()) {
            case VALIDATE ->
                    producer.sendOpenTofuValidationResult(
                            requestService.handleOpenTofuValidateRequest(request));
            case PLAN ->
                    producer.sendOpenTofuPlanResult(
                            requestService.handleOpenTofuPlanRequest(request));
            case DEPLOY, MODIFY, DESTROY ->
                    producer.sendOpenTofuDeploymentResult(
                            requestService.handleOpenTofuDeploymentRequest(request));
            default ->
                    throw new UnsupportedEnumValueException(
                            String.format(
                                    "RequestType value %s is not supported.",
                                    request.getRequestType().toValue()));
        }
    }

    private void sendErrorResultToQueue(OpenTofuRequest request, Exception e) {
        log.error(
                "Failed to process request with id {} from amqp queues. {}",
                request.getRequestId(),
                e.getMessage(),
                e);
        try {
            processErrorByRequestType(request, e);
        } catch (Exception innerEx) {
            log.error("Error handling failed request: {}", innerEx.getMessage(), innerEx);
        }
    }

    private void processErrorByRequestType(OpenTofuRequest request, Exception e) {
        switch (request.getRequestType()) {
            case VALIDATE ->
                    producer.sendOpenTofuValidationResult(
                            requestService.getErrorValidateResult(request, e));
            case PLAN ->
                    producer.sendOpenTofuPlanResult(requestService.getErrorPlanResult(request, e));
            case DEPLOY, MODIFY, DESTROY ->
                    producer.sendOpenTofuDeploymentResult(
                            requestService.getErrorDeploymentResult(request, e));
            default ->
                    throw new UnsupportedEnumValueException(
                            String.format(
                                    "RequestType value %s is not supported.",
                                    request.getRequestType().toValue()));
        }
    }
}
