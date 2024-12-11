/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.tofu.maker.opentofu.tool;

import jakarta.annotation.Resource;
import java.io.File;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.tofu.maker.models.exceptions.InvalidOpenTofuToolException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

/** Bean help to install opentofu with specific version. */
@Slf4j
@Component
public class OpenTofuInstaller {

    @Value("${opentofu.download.base.url:https://releases.hashicorp.com/opentofu}")
    private String openTofuDownloadBaseUrl;

    @Value("${opentofu.install.dir:/opt/opentofu}")
    private String openTofuInstallDir;

    @Resource private OpenTofuVersionsCache versionsCache;
    @Resource private OpenTofuVersionsHelper versionHelper;

    /**
     * Find the executable binary path of the OpenTofu tool that matches the required version. If no
     * matching executable binary is found, install the OpenTofu tool with the required version and
     * then return the path.
     *
     * @param requiredVersion The required version of OpenTofu tool.
     * @return The path of the executable binary.
     */
    @Retryable(
            retryFor = InvalidOpenTofuToolException.class,
            maxAttemptsExpression = "${spring.retry.max-attempts}",
            backoff = @Backoff(delayExpression = "${spring.retry.delay-millions}"))
    public String getExecutorPathThatMatchesRequiredVersion(String requiredVersion) {
        if (StringUtils.isBlank(requiredVersion)) {
            log.info("No required version of OpenTofu is specified, use the default OpenTofu.");
            return "tofu";
        }
        String[] operatorAndNumber =
                this.versionHelper.getOperatorAndNumberFromRequiredVersion(requiredVersion);
        String requiredOperator = operatorAndNumber[0];
        String requiredNumber = operatorAndNumber[1];
        // Find executor in the installation path that matches the required version.
        String matchedVersionExecutorPath =
                this.versionHelper.getExecutorPathMatchedRequiredVersion(
                        this.openTofuInstallDir, requiredOperator, requiredNumber);
        if (StringUtils.isBlank(matchedVersionExecutorPath)) {
            log.info(
                    "Not found any OpenTofu executor matched the required version {} from the "
                            + "OpenTofu installation dir {}, start to download and install one.",
                    requiredVersion,
                    this.openTofuInstallDir);
            return installOpenTofuByRequiredVersion(requiredOperator, requiredNumber);
        }
        return matchedVersionExecutorPath;
    }

    private String installOpenTofuByRequiredVersion(
            String requiredOperator, String requiredNumber) {
        String bestVersionNumber =
                getBestAvailableVersionMatchingRequiredVersion(requiredOperator, requiredNumber);
        File installedExecutorFile =
                this.versionHelper.installOpenTofuWithVersion(
                        bestVersionNumber, this.openTofuDownloadBaseUrl, this.openTofuInstallDir);
        if (this.versionHelper.checkIfExecutorCanBeExecuted(installedExecutorFile)) {
            log.info("OpenTofu with version {} installed successfully.", installedExecutorFile);
            return installedExecutorFile.getAbsolutePath();
        }
        String errorMsg =
                String.format(
                        "Installing OpenTofu with version %s into the dir %s " + "failed. ",
                        bestVersionNumber, this.openTofuInstallDir);
        log.error(errorMsg);
        throw new InvalidOpenTofuToolException(errorMsg);
    }

    /**
     * Get the best available version in download url.
     *
     * @param requiredOperator operator in required version
     * @param requiredNumber number in required version
     * @return the best available version existed in download url.
     */
    private String getBestAvailableVersionMatchingRequiredVersion(
            String requiredOperator, String requiredNumber) {
        Set<String> availableVersions = this.versionsCache.getAvailableVersions();
        String bestAvailableVersion =
                this.versionHelper.findBestVersionFromAllAvailableVersions(
                        availableVersions, requiredOperator, requiredNumber);
        if (StringUtils.isNotBlank(bestAvailableVersion)) {
            log.info(
                    "Found the best available version {} for OpenTofu by the required version "
                            + "{}.",
                    bestAvailableVersion,
                    requiredOperator + requiredNumber);
            return bestAvailableVersion;
        }
        String errorMsg =
                String.format(
                        "Failed to find available versions for OpenTofu by the "
                                + "required version %s.",
                        requiredOperator + requiredNumber);
        log.error(errorMsg);
        throw new InvalidOpenTofuToolException(errorMsg);
    }
}
