/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.tofu.maker.opentofu.service;

import java.io.File;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.xpanse.tofu.maker.models.exceptions.GitRepoCloneException;
import org.eclipse.xpanse.tofu.maker.models.request.git.OpenTofuScriptGitRepoDetails;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
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
    public void checkoutScripts(String workspace, OpenTofuScriptGitRepoDetails scriptsRepo) {
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
    }


    /**
     * Recover method for checkoutScripts.
     *
     * @param e GitRepoCloneException
     */
    @Recover
    public void recoverCheckoutScripts(GitRepoCloneException e) {
        log.error("Retry exhausted. Throwing exception: " + e.getMessage());
        throw e;
    }
}
