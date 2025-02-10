/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.tofu.maker.security.hmac;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

/** Interceptor to automatically add HMAC signature headers. */
@Component
@ConditionalOnProperty(
        name = "tofu.maker.webhook.hmac.request.signing.enabled",
        havingValue = "true",
        matchIfMissing = true)
public class HmacSignatureHeaderInterceptor implements ClientHttpRequestInterceptor {

    private final HmacSignatureHeaderManage hmacSignatureHeaderManage;

    @Autowired
    public HmacSignatureHeaderInterceptor(HmacSignatureHeaderManage hmacSignatureHeaderManage) {
        this.hmacSignatureHeaderManage = hmacSignatureHeaderManage;
    }

    @Override
    @NonNull
    public ClientHttpResponse intercept(
            @NonNull HttpRequest request,
            @NonNull byte[] body,
            @NonNull ClientHttpRequestExecution execution)
            throws IOException {
        Map<String, String> signatureHeaders =
                hmacSignatureHeaderManage.createHmacSignatureHeader(
                        request.getURI().toURL().toString(),
                        new String(body, StandardCharsets.UTF_8));
        signatureHeaders.forEach(
                (entryKey, entryValue) -> {
                    request.getHeaders().put(entryKey, List.of(entryValue));
                });
        return execution.execute(request, body);
    }
}
