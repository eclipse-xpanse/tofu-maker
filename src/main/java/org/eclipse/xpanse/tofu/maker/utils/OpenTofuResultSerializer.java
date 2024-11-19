/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.tofu.maker.utils;

import org.eclipse.serializer.Serializer;
import org.eclipse.serializer.SerializerFoundation;
import org.eclipse.xpanse.tofu.maker.models.response.OpenTofuResult;
import org.springframework.stereotype.Component;

/**
 * class to manage OpenTofuResult serialization and deserialization using eclipse-serializer.
 */
@Component
public class OpenTofuResultSerializer {

    /**
     * serialize OpenTofuResult object.
     *
     * @param result OpenTofuResult.
     * @return byte[].
     */
    public byte[] serialize(OpenTofuResult result) {
        final SerializerFoundation<?> foundation = SerializerFoundation
                .New().registerEntityTypes(OpenTofuResult.class);
        Serializer<byte[]> serializer = Serializer.Bytes(foundation);
        return serializer.serialize(result);
    }

    /**
     * deserialize OpenTofuResult object.
     *
     * @param data byte[].
     * @return OpenTofuResult.
     */
    public OpenTofuResult deserialize(byte[] data) {
        final SerializerFoundation<?> foundation = SerializerFoundation
                .New().registerEntityTypes(OpenTofuResult.class);
        Serializer<byte[]> serializer = Serializer.Bytes(foundation);
        return serializer.deserialize(data);
    }
}
