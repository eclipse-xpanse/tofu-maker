/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.tofu.maker.models.request;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.tofu.maker.models.request.directory.OpenTofuAsyncRequestWithScriptsDirectory;
import org.eclipse.xpanse.tofu.maker.models.request.directory.OpenTofuRequestWithScriptsDirectory;
import org.eclipse.xpanse.tofu.maker.models.request.git.OpenTofuAsyncRequestWithScriptsGitRepo;
import org.eclipse.xpanse.tofu.maker.models.request.git.OpenTofuRequestWithScriptsGitRepo;
import org.eclipse.xpanse.tofu.maker.models.request.scripts.OpenTofuAsyncRequestWithScripts;

/** OpenTofuRequestDeserializer is used to deserialize OpenTofuRequest. */
@Slf4j
public class OpenTofuRequestDeserializer extends StdDeserializer<OpenTofuRequest> {

    /** Constructs a new OpenTofuRequestDeserializer. */
    public OpenTofuRequestDeserializer() {
        super(OpenTofuRequest.class);
    }

    @Override
    public OpenTofuRequest deserialize(JsonParser p, DeserializationContext ctxt)
            throws IOException {
        JsonNode node = p.getCodec().readTree(p);
        if (node.has("scriptFiles")) {
            if (node.has("webhookConfig")) {
                return p.getCodec().treeToValue(node, OpenTofuAsyncRequestWithScripts.class);
            }
            return p.getCodec().treeToValue(node, OpenTofuAsyncRequestWithScripts.class);
        } else if (node.has("gitRepoDetails")) {
            if (node.has("webhookConfig")) {
                return p.getCodec().treeToValue(node, OpenTofuAsyncRequestWithScriptsGitRepo.class);
            }
            return p.getCodec().treeToValue(node, OpenTofuRequestWithScriptsGitRepo.class);
        } else if (node.has("scriptsDirectory")) {
            if (node.has("webhookConfig")) {
                return p.getCodec()
                        .treeToValue(node, OpenTofuAsyncRequestWithScriptsDirectory.class);
            }
            return p.getCodec().treeToValue(node, OpenTofuRequestWithScriptsDirectory.class);
        }
        return p.getCodec().treeToValue(node, OpenTofuRequest.class);
    }
}
