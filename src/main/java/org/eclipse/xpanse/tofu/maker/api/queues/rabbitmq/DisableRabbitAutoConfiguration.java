/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.tofu.maker.api.queues.rabbitmq;

import org.springframework.boot.actuate.autoconfigure.amqp.RabbitHealthContributorAutoConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/** Disable AmqpAutoConfigurations when profile amqp is disabled. */
@Profile("!amqp")
@Configuration
@EnableAutoConfiguration(
        exclude = {RabbitAutoConfiguration.class, RabbitHealthContributorAutoConfiguration.class})
public class DisableRabbitAutoConfiguration {}
