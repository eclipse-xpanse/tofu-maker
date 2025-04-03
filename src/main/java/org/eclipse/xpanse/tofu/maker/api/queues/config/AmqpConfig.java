/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.tofu.maker.api.queues.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/** Configuration for Spring AMQP (Advanced Message Queuing Protocol). */
@Slf4j
@Profile("amqp")
@Component
public class AmqpConfig {

    @Resource private ObjectMapper objectMapper;

    /** Create JSON message converter for Spring AMQP. */
    @Bean("customJsonMessageConverter")
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        ObjectMapper localObjectMapper = objectMapper.copy();
        localObjectMapper
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .deactivateDefaultTyping()
                .setSerializationInclusion(JsonInclude.Include.NON_NULL);
        Jackson2JsonMessageConverter converter =
                new Jackson2JsonMessageConverter(localObjectMapper);
        converter.setAlwaysConvertToInferredType(true);
        return converter;
    }

    private Queue createDurableQueue(String queueName) {
        return QueueBuilder.durable(queueName).build();
    }

    /** Create durable queue for OpenTofu health check request. */
    @Bean
    public Queue queueForOpenTofuHealthCheckRequest() {
        return createDurableQueue(AmqpConstants.QUEUE_NAME_FOR_TOFU_HEALTH_CHECK_REQUEST);
    }

    /** Create durable queue for OpenTofu request with directory. */
    @Bean
    public Queue queueForOpenTofuRequestWithDirectory() {
        return createDurableQueue(AmqpConstants.QUEUE_NAME_FOR_TOFU_REQUEST_WITH_DIRECTORY);
    }

    /** Create durable queue for OpenTofu request with scripts. */
    @Bean
    public Queue queueForOpenTofuRequestWithScripts() {
        return createDurableQueue(AmqpConstants.QUEUE_NAME_FOR_TOFU_REQUEST_WITH_SCRIPTS);
    }

    /** Create durable queue for OpenTofu request with git repo. */
    @Bean
    public Queue queueForOpenTofuRequestWithScriptsGitRepo() {
        return createDurableQueue(AmqpConstants.QUEUE_NAME_FOR_TOFU_REQUEST_WITH_GIT);
    }

    /** Create durable queue for OpenTofu health check result. */
    @Bean
    public Queue queueForOpenTofuHealthCheckResult() {
        return createDurableQueue(AmqpConstants.QUEUE_NAME_FOR_TOFU_HEALTH_CHECK_RESULT);
    }

    /** Create durable queue for OpenTofu plan result. */
    @Bean
    public Queue queueForOpenTofuPlanResult() {
        return createDurableQueue(AmqpConstants.QUEUE_NAME_FOR_TOFU_PLAN_RESULT);
    }

    /** Create durable queue for OpenTofu validation result. */
    @Bean
    public Queue queueForOpenTofuValidationResult() {
        return createDurableQueue(AmqpConstants.QUEUE_NAME_FOR_TOFU_VALIDATION_RESULT);
    }

    /** Create durable queue for OpenTofu deployment result. */
    @Bean
    public Queue queueForOpenTofuDeploymentResult() {
        return createDurableQueue(AmqpConstants.QUEUE_NAME_FOR_TOFU_DEPLOYMENT_RESULT);
    }

    /** Create direct exchange for OpenTofu message. */
    @Bean
    public DirectExchange openTofuDirectExchange() {
        return new DirectExchange(AmqpConstants.EXCHANGE_NAME_FOR_TOFU, true, false);
    }

    /** Bind OpenTofu health check request queue to OpenTofu direct exchange. */
    @Bean
    public Binding bindHealthCheckRequest(
            DirectExchange openTofuDirectExchange, Queue queueForOpenTofuHealthCheckRequest) {
        return BindingBuilder.bind(queueForOpenTofuHealthCheckRequest)
                .to(openTofuDirectExchange)
                .with(AmqpConstants.ROUTING_KEY_FOR_TOFU_HEALTH_CHECK_REQUEST);
    }

    /** Bind OpenTofu request with directory queue to OpenTofu direct exchange. */
    @Bean
    public Binding bindRequestWithDirectory(
            DirectExchange openTofuDirectExchange, Queue queueForOpenTofuRequestWithDirectory) {
        return BindingBuilder.bind(queueForOpenTofuRequestWithDirectory)
                .to(openTofuDirectExchange)
                .with(AmqpConstants.ROUTING_KEY_FOR_TOFU_REQUEST_WITH_DIRECTORY);
    }

    /** Bind OpenTofu request with scripts queue to OpenTofu direct exchange. */
    @Bean
    public Binding bindRequestWithScripts(
            DirectExchange openTofuDirectExchange, Queue queueForOpenTofuRequestWithScripts) {
        return BindingBuilder.bind(queueForOpenTofuRequestWithScripts)
                .to(openTofuDirectExchange)
                .with(AmqpConstants.ROUTING_KEY_FOR_TOFU_REQUEST_WITH_SCRIPTS);
    }

    /** Bind OpenTofu request with git repo queue to OpenTofu direct exchange. */
    @Bean
    public Binding bindRequestWithGitRepo(
            DirectExchange openTofuDirectExchange,
            Queue queueForOpenTofuRequestWithScriptsGitRepo) {
        return BindingBuilder.bind(queueForOpenTofuRequestWithScriptsGitRepo)
                .to(openTofuDirectExchange)
                .with(AmqpConstants.ROUTING_KEY_FOR_TOFU_REQUEST_WITH_GIT);
    }

    /** Bind OpenTofu health check result queue to OpenTofu direct exchange. */
    @Bean
    public Binding bindHealthCheckResult(
            DirectExchange openTofuDirectExchange, Queue queueForOpenTofuHealthCheckResult) {
        return BindingBuilder.bind(queueForOpenTofuHealthCheckResult)
                .to(openTofuDirectExchange)
                .with(AmqpConstants.ROUTING_KEY_FOR_TOFU_HEALTH_CHECK_RESULT);
    }

    /** Bind OpenTofu plan result queue to OpenTofu direct exchange. */
    @Bean
    public Binding bindPlanResult(
            DirectExchange openTofuDirectExchange, Queue queueForOpenTofuPlanResult) {
        return BindingBuilder.bind(queueForOpenTofuPlanResult)
                .to(openTofuDirectExchange)
                .with(AmqpConstants.ROUTING_KEY_FOR_TOFU_PLAN_RESULT);
    }

    /** Bind OpenTofu validation result queue to OpenTofu direct exchange. */
    @Bean
    public Binding bindValidationResult(
            DirectExchange openTofuDirectExchange, Queue queueForOpenTofuValidationResult) {
        return BindingBuilder.bind(queueForOpenTofuValidationResult)
                .to(openTofuDirectExchange)
                .with(AmqpConstants.ROUTING_KEY_FOR_TOFU_VALIDATION_RESULT);
    }

    /** Bind OpenTofu deployment result queue to OpenTofu direct exchange. */
    @Bean
    public Binding bindDeploymentResult(
            DirectExchange openTofuDirectExchange, Queue queueForOpenTofuDeploymentResult) {
        return BindingBuilder.bind(queueForOpenTofuDeploymentResult)
                .to(openTofuDirectExchange)
                .with(AmqpConstants.ROUTING_KEY_FOR_TOFU_DEPLOYMENT_RESULT);
    }
}
