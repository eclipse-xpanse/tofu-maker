/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.tofu.maker.config;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.tofu.maker.logging.RestTemplateLoggingInterceptor;
import org.eclipse.xpanse.tofu.maker.security.hmac.HmacSignatureHeaderInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/** Configuration class for RestTemplate. */
@Configuration
@Slf4j
public class RestTemplateConfig {

    private final RestTemplateLoggingInterceptor restTemplateLoggingInterceptor;
    private final HmacSignatureHeaderInterceptor hmacSignatureHeaderInterceptor;
    private final boolean isHmacRequestSigningEnabled;

    /** constructor for RestTemplateConfig. */
    public RestTemplateConfig(
            @Autowired RestTemplateLoggingInterceptor restTemplateLoggingInterceptor,
            @Autowired(required = false)
                    HmacSignatureHeaderInterceptor hmacSignatureHeaderInterceptor,
            @Value("${tofu.maker.webhook.hmac.request.signing.enabled}")
                    boolean isHmacRequestSigningEnabled) {
        this.restTemplateLoggingInterceptor = restTemplateLoggingInterceptor;
        this.hmacSignatureHeaderInterceptor = hmacSignatureHeaderInterceptor;
        this.isHmacRequestSigningEnabled = isHmacRequestSigningEnabled;
        if (!isHmacRequestSigningEnabled) {
            log.warn("HMAC based request signing disabled for webhook requests");
        } else {
            log.info("HMAC based request signing enabled for webhook requests");
        }
    }

    /** Create RestTemplate to IOC. */
    @Bean
    public RestTemplate restTemplate(ClientHttpRequestFactory factory) {
        RestTemplate restTemplate = new RestTemplate(factory);
        restTemplate.getInterceptors().add(restTemplateLoggingInterceptor);
        if (isHmacRequestSigningEnabled) {
            restTemplate.getInterceptors().add(hmacSignatureHeaderInterceptor);
        }
        return restTemplate;
    }

    /** Create ClientHttpRequestFactory to IOC. */
    @Bean
    public ClientHttpRequestFactory simpleClientHttpRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(15000);
        factory.setReadTimeout(5000);
        return factory;
    }
}
