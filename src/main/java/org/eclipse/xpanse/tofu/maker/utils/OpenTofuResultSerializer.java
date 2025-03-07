/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.tofu.maker.utils;

import org.eclipse.serializer.Serializer;
import org.eclipse.serializer.SerializerFoundation;
import org.eclipse.xpanse.tofu.maker.models.response.OpenTofuResult;
import org.springframework.stereotype.Component;

/** class to manage OpenTofuResult serialization and deserialization using eclipse-serializer. */
@Component
public class OpenTofuResultSerializer {

    private final Serializer<byte[]> serializer;

    /** Constructor to initialize the Serializer with OpenTofuResult class registered. */
    public OpenTofuResultSerializer() {
        final SerializerFoundation<?> foundation =
                SerializerFoundation.New().registerEntityTypes(OpenTofuResult.class);
        this.serializer = Serializer.Bytes(foundation);
    }

    /**
     * Serialize OpenTofuResult object.
     *
     * @param result OpenTofuResult.
     * @return byte[].
     */
    public byte[] serialize(OpenTofuResult result) {
        return serializer.serialize(result);
    }

    /**
     * Deserialize OpenTofuResult object.
     *
     * @param data byte[].
     * @return OpenTofuResult.
     */
    public OpenTofuResult deserialize(byte[] data) {
        return serializer.deserialize(data);
    }
}
