/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.tofu.maker.opentofu.tool;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.tofu.maker.models.exceptions.InvalidOpenTofuToolException;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHTag;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.kohsuke.github.GitHubRateLimitHandler;
import org.kohsuke.github.PagedIterable;
import org.kohsuke.github.connector.GitHubConnectorResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

/**
 * Class to get available versions of OpenTofu.
 */
@Slf4j
@Component
public class OpenTofuVersionsFetcher {

    private static final Pattern OFFICIAL_VERSION_PATTERN =
            Pattern.compile("^v(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})$");
    @Value("${opentofu.github.api.endpoint:https://api.github.com}")
    private String openTofuGithubApiEndpoint;
    @Value("${opentofu.github.repository:opentofu/opentofu}")
    private String openTofuGithubRepository;
    @Value("${opentofu.versions}")
    private String defaultVersionsString;

    /**
     * Fetch all available versions from OpenTofu website.
     *
     * @return all available versions from OpenTofu website.
     */
    @Retryable(retryFor = Exception.class,
            maxAttemptsExpression = "${spring.retry.max-attempts}",
            backoff = @Backoff(delayExpression = "${spring.retry.delay-millions}"))
    public Set<String> fetchAvailableVersionsFromOpenTofuWebsite() throws Exception {
        Set<String> allVersions = new HashSet<>();
        GitHub gitHub = new GitHubBuilder()
                .withEndpoint(openTofuGithubApiEndpoint)
                .withRateLimitHandler(getGithubRateLimitHandler())
                .build();
        GHRepository repository = gitHub.getRepository(openTofuGithubRepository);
        PagedIterable<GHTag> tags = repository.listTags();
        tags.forEach(tag -> {
            String version = tag.getName();
            if (OFFICIAL_VERSION_PATTERN.matcher(version).matches()) {
                // remove the prefix 'v'
                allVersions.add(version.substring(1));
            }
        });
        log.info("Get available versions: {} from OpenTofu website.", allVersions);
        if (allVersions.isEmpty()) {
            String errorMsg = "No available versions found from OpenTofu website.";
            throw new InvalidOpenTofuToolException(errorMsg);
        }
        return allVersions;
    }


    /**
     * Get default versions from config.
     *
     * @return default versions.
     */
    public Set<String> getDefaultVersionsFromConfig() {
        Set<String> defaultVersions =
                Set.of(defaultVersionsString.replaceAll("//s+", "").split(","));
        log.info("Get default versions: {} from OpenTofu versions config value: {}",
                defaultVersions, defaultVersionsString);
        return defaultVersions;
    }

    private GitHubRateLimitHandler getGithubRateLimitHandler() {
        return new GitHubRateLimitHandler() {
            @Override
            public void onError(@Nonnull GitHubConnectorResponse response) throws IOException {
                String limit = response.header("X-RateLimit-Limit");
                String remaining = response.header("X-RateLimit-Remaining");
                String reset = response.header("X-RateLimit-Reset");
                String errorMsg = String.format("GitHub API rate limit exceeded. "
                        + "Rate limit: %s, remaining: %s, reset time: %s", limit, remaining, reset);
                throw new IOException(errorMsg);
            }
        };
    }

}
