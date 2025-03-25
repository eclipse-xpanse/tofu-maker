/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.tofu.maker.api.queues.rabbitmq;

import jakarta.annotation.Resource;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.RabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;

/** RabbitMQ configuration for AMQP. */
@Component
@Profile("amqp")
@ConditionalOnProperty(name = "spring.amqp.provider", havingValue = "rabbitmq")
@EnableRabbit
public class RabbitMqConfig {

    @Qualifier("customJsonMessageConverter")
    @Resource
    private Jackson2JsonMessageConverter jsonMessageConverter;

    @Value("${spring.rabbitmq.listener.simple.retry.max-attempts:3}")
    private int retryMaxAttempts;

    @Value("${spring.rabbitmq.listener.simple.retry.initial-interval:5000}")
    private int retryInitialInterval;

    @Value("${spring.rabbitmq.listener.simple.retry.max-interval:30000}")
    private int retryMaxInterval;

    /**
     * Create a RabbitTemplate bean with custom message converter and retry template.
     *
     * @param connectionFactory The connection factory used to create the RabbitTemplate.
     * @return RabbitTemplate bean.
     */
    @Bean("customRabbitTemplate")
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter);
        rabbitTemplate.setRetryTemplate(retryTemplate());
        return rabbitTemplate;
    }

    /**
     * Create a RabbitListenerContainerFactory bean with custom message converter and retry
     * template.
     *
     * @param connectionFactory The connection factory used to create the
     *     RabbitListenerContainerFactory.
     * @return rabbitListenerContainerFactory bean.
     */
    @Bean("customRabbitListenerContainerFactory")
    public RabbitListenerContainerFactory<SimpleMessageListenerContainer>
            rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setMessageConverter(jsonMessageConverter);
        factory.setConnectionFactory(connectionFactory);
        factory.setAcknowledgeMode(AcknowledgeMode.AUTO);
        factory.setDefaultRequeueRejected(true);
        factory.setFailedDeclarationRetryInterval(5000L);
        factory.setPrefetchCount(100);
        factory.setRetryTemplate(retryTemplate());

        factory.setBatchSize(10);
        factory.setConcurrentConsumers(5);
        factory.setMaxConcurrentConsumers(10);
        factory.setConsecutiveActiveTrigger(5);
        factory.setConsecutiveIdleTrigger(10);
        return factory;
    }

    private RetryTemplate retryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();
        retryTemplate.setRetryPolicy(new SimpleRetryPolicy(retryMaxAttempts));
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(retryInitialInterval);
        backOffPolicy.setMaxInterval(retryMaxInterval);
        retryTemplate.setBackOffPolicy(backOffPolicy);
        return retryTemplate;
    }
}
