package org.eclipse.xpanse.tofu.maker.opentofu.tool;

import java.util.Map;
import org.assertj.core.api.Assertions;
import org.eclipse.xpanse.tofu.maker.security.hmac.HmacSignatureHeaderManage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@TestPropertySource(
        properties = {
            "tofu.maker.webhook.hmac.request.signing.key=1c30e4b1fad574f88572e25d0da03f34365f4ae92eda22bfd3a8c53cb5102f27",
            "tofu.maker.webhook.hmac.request.signing.algorithm=HmacSHA256",
        })
@ContextConfiguration(classes = {HmacSignatureHeaderManage.class})
public class HmacSignatureHeaderTest {

    @Autowired HmacSignatureHeaderManage hmacSignatureHeaderManage;

    @Test
    public void testSignature() {
        Map<String, String> signatureHeaders =
                this.hmacSignatureHeaderManage.createHmacSignatureHeader(
                        "http://localhost/orderId", "");
        Assertions.assertThat(signatureHeaders).isNotEmpty();
        Assertions.assertThat(signatureHeaders)
                .containsKeys("x-signature", "x-nonce-signature", "x-timestamp-signature");
    }
}
