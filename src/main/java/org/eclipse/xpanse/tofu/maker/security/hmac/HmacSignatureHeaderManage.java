/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.tofu.maker.security.hmac;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.HmacAlgorithms;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Class to generate all required HMAC related HTTP headers. HMAC Signature must in the format -
 * x-signature: algorithm=HmacSHA256;headers=x-nonce-signature
 * x-timestamp-signature;signature=d4fb456bc7621ed1c8099e8a1136997d6ad9b8613fc79a49b39833d4cb36080a
 */
@Slf4j
@Component
@ConditionalOnProperty(
        name = "tofu.maker.webhook.hmac.request.signing.enabled",
        havingValue = "true",
        matchIfMissing = true)
public class HmacSignatureHeaderManage {

    private static final String NONCE_SIGNATURE_HEADER = "x-nonce-signature";
    private static final String TIMESTAMP_SIGNATURE_HEADER = "x-timestamp-signature";
    private static final String MAIN_SIGNATURE_HEADER = "x-signature";
    private final String hmacSecretKey;
    private final String hmacAlgorithm;

    /** Constructor for HmacSignatureHeaderManage. */
    @Autowired
    public HmacSignatureHeaderManage(
            @Value("${tofu.maker.webhook.hmac.request.signing.key}") String hmacSecretKey,
            @Value("${tofu.maker.webhook.hmac.request.signing.algorithm}") String hmacAlgorithm)
            throws InvalidAlgorithmParameterException {
        validateAlgorithmName(hmacAlgorithm);
        this.hmacSecretKey = hmacSecretKey;
        this.hmacAlgorithm = hmacAlgorithm;
        if (hmacSecretKey.isBlank()) {
            throw new IllegalArgumentException("Missing mandatory hmac secret key");
        }
    }

    /**
     * Generates the HMAC related HTTP headers map.
     *
     * @param webhookUrl URL to which the application wants to send the webhook request.
     * @param payload JSON payload
     * @return HMAC HTTP Headers.
     */
    public Map<String, String> createHmacSignatureHeader(String webhookUrl, String payload) {
        try {
            Mac mac = Mac.getInstance(hmacAlgorithm);
            SecretKeySpec secretKeySpec =
                    new SecretKeySpec(hmacSecretKey.getBytes(), hmacAlgorithm);
            mac.init(secretKeySpec);
            String nonce = getRandomNonce();
            String currentTimeStamp = Long.toString(System.currentTimeMillis());
            byte[] hmacBytes =
                    mac.doFinal(
                            getSignatureValue(nonce, currentTimeStamp, webhookUrl, payload)
                                    .getBytes());
            Map<String, String> headers = new HashMap<>();
            headers.put(NONCE_SIGNATURE_HEADER, nonce);
            headers.put(TIMESTAMP_SIGNATURE_HEADER, currentTimeStamp);
            headers.put(
                    MAIN_SIGNATURE_HEADER,
                    constructSignatureHeader(Hex.encodeHexString(hmacBytes)));
            return headers;
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("Failed generating key", e);
            throw new RuntimeException();
        }
    }

    private String getSignatureValue(
            String nonce, String currentTimeStamp, String webhookUrl, String payload) {
        return """
               %s
               %s
               %s
               %s"""
                .formatted(nonce, currentTimeStamp, webhookUrl, payload);
    }

    private String getRandomNonce() {
        byte[] nonce = new byte[16];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(nonce);
        return bytesToHex(nonce);
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    private String constructSignatureHeader(String generatedKey) {
        return String.format(
                "algorithm=%s;headers=%s %s;signature=%s",
                hmacAlgorithm, NONCE_SIGNATURE_HEADER, TIMESTAMP_SIGNATURE_HEADER, generatedKey);
    }

    private void validateAlgorithmName(String algorithmName)
            throws InvalidAlgorithmParameterException {
        for (HmacAlgorithms hmacAlgorithms : HmacAlgorithms.values()) {
            if (hmacAlgorithms.getName().equals(algorithmName)) {
                return;
            }
        }
        throw new InvalidAlgorithmParameterException("Invalid HMAC algorithm: " + algorithmName);
    }
}
