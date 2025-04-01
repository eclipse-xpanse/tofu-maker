/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.tofu.maker.api.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.eclipse.xpanse.tofu.maker.models.request.OpenTofuRequest;
import org.eclipse.xpanse.tofu.maker.models.request.OpenTofuRequestDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

/** Configuration class for Jackson. */
@Configuration
public class JacksonConfig {

    /**
     * Create a ObjectMapper with the given module.
     *
     * @return objectMapper bean.
     */
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule tofuRequestModule = new SimpleModule();
        tofuRequestModule.addDeserializer(OpenTofuRequest.class, new OpenTofuRequestDeserializer());
        mapper.registerModule(tofuRequestModule);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper;
    }

    /**
     * Define MappingJackson2HttpMessageConverter with the given objectMapper.
     *
     * @param objectMapper objectMapper
     * @return mappingJackson2HttpMessageConverter bean
     */
    @Bean
    public MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter(
            ObjectMapper objectMapper) {
        return new MappingJackson2HttpMessageConverter(objectMapper);
    }
}
