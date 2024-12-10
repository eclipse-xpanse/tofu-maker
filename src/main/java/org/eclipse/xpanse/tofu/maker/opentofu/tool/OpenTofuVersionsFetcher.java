/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.tofu.maker.opentofu.tool;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.HashSet;
import java.util.Objects;
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
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.support.RetrySynchronizationManager;
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
    @Value("${opentofu.default.supported.versions}")
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
        int retryCount = Objects.isNull(RetrySynchronizationManager.getContext())
                ? 0 : RetrySynchronizationManager.getContext().getRetryCount();
        log.info("Start to fetch available versions from website for OpenTofu."
                + " Retry count: {}", retryCount);
        Set<String> allVersions = new HashSet<>();
        try {
            if (!isEndpointReachable(openTofuGithubApiEndpoint)) {
                String errorMsg = "OpenTofu website is not reachable.";
                log.error(errorMsg);
                throw new IOException(errorMsg);
            }
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
        } catch (Exception e) {
            log.error("Failed to fetch available versions from OpenTofu website. Retry count: {}",
                    retryCount, e);
            throw e;
        }
        log.info("Get available versions: {} from OpenTofu website. Retry count: {}", allVersions,
                retryCount);
        if (allVersions.isEmpty()) {
            String errorMsg = "No available versions found from OpenTofu website.";
            throw new InvalidOpenTofuToolException(errorMsg);
        }
        return allVersions;
    }

    private boolean isEndpointReachable(String endpoint) throws IOException {
        try {
            URL url = URI.create(endpoint).toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(10000);
            connection.setRequestMethod(HttpMethod.HEAD.name());
            return connection.getResponseCode() == HttpStatus.OK.value();
        } catch (IOException e) {
            throw new IOException("Failed to connect to the endpoint: " + endpoint);
        }
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
