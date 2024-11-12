/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.tofu.maker.opentofu.service;

import static org.eclipse.xpanse.tofu.maker.opentofu.service.OpenTofuScriptsHelper.TF_SCRIPT_FILE_EXTENSION;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.xpanse.tofu.maker.models.exceptions.GitRepoCloneException;
import org.eclipse.xpanse.tofu.maker.models.exceptions.InvalidOpenTofuScriptsException;
import org.eclipse.xpanse.tofu.maker.models.request.git.OpenTofuScriptGitRepoDetails;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.support.RetrySynchronizationManager;
import org.springframework.stereotype.Component;

/**
 * Bean to manage GIT clone.
 */
@Slf4j
@Component
public class ScriptsGitRepoManage {

    /**
     * Method to check out scripts from a GIT repo.
     *
     * @param workspace   directory where the GIT clone must be executed.
     * @param scriptsRepo directory inside the GIT repo where scripts are expected to be present.
     */
    @Retryable(retryFor = GitRepoCloneException.class,
            maxAttemptsExpression = "${spring.retry.max-attempts}",
            backoff = @Backoff(delayExpression = "${spring.retry.delay-millions}"))
    public List<File> checkoutScripts(String workspace, OpenTofuScriptGitRepoDetails scriptsRepo) {
        log.info("Clone GIT repo to get the deployment scripts. Retry number: "
                + Objects.requireNonNull(RetrySynchronizationManager.getContext()).getRetryCount());
        File workspaceDirectory = new File(workspace);
        FileRepositoryBuilder repositoryBuilder = new FileRepositoryBuilder();
        repositoryBuilder.findGitDir(workspaceDirectory);
        if (Objects.isNull(repositoryBuilder.getGitDir())) {
            CloneCommand cloneCommand = new CloneCommand();
            cloneCommand.setURI(scriptsRepo.getRepoUrl());
            cloneCommand.setProgressMonitor(null);
            cloneCommand.setDirectory(workspaceDirectory);
            cloneCommand.setBranch(scriptsRepo.getBranch());
            cloneCommand.setTimeout(20);
            try (Git git = cloneCommand.call()) {
                git.checkout();
            } catch (GitAPIException e) {
                String errorMsg =
                        String.format("Clone scripts form GIT repo error:%s", e.getMessage());
                log.error(errorMsg);
                throw new GitRepoCloneException(errorMsg);
            }
        } else {
            log.info("Scripts repo is already cloned in the workspace.");
        }
        List<File> files = getSourceFiles(workspace, scriptsRepo);
        validateIfFolderContainsOpenTofuScripts(files, scriptsRepo);
        return files;
    }

    private List<File> getSourceFiles(String workspace, OpenTofuScriptGitRepoDetails scriptsRepo) {
        List<File> sourceFiles = new ArrayList<>();
        File directory = new File(workspace
                + (StringUtils.isNotBlank(scriptsRepo.getScriptPath())
                ? File.separator + scriptsRepo.getScriptPath()
                : ""));
        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (Objects.nonNull(files)) {
                Arrays.stream(files).forEach(file -> {
                    if (file.isFile()) {
                        sourceFiles.add(file);
                    }
                });
            }
        }
        return sourceFiles;
    }

    private void validateIfFolderContainsOpenTofuScripts(
            List<File> files, OpenTofuScriptGitRepoDetails scriptsRepo) {
        boolean isScriptsExisted = files.stream()
                .anyMatch(file -> file.getName().endsWith(TF_SCRIPT_FILE_EXTENSION));
        if (!isScriptsExisted) {
            throw new InvalidOpenTofuScriptsException(
                    "No OpenTofu scripts found in the "
                            + scriptsRepo.getRepoUrl()
                            + " repo's '"
                            + (StringUtils.isNotBlank(scriptsRepo.getScriptPath())
                            ? File.separator + scriptsRepo.getScriptPath() : "root")
                            + "' folder.");
        }
    }
}