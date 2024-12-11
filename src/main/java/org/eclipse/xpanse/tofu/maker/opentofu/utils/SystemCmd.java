/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.tofu.maker.opentofu.utils;

import static java.util.concurrent.Executors.newSingleThreadExecutor;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.tofu.maker.models.exceptions.OpenTofuExecutorException;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

/** Executes operating system commands. */
@Slf4j
@Component
public class SystemCmd {

    public SystemCmdResult execute(
            String cmd,
            String workspace,
            boolean isCommandOutputToBeLogged,
            Map<String, String> envVariables) {
        return execute(cmd, 0, workspace, isCommandOutputToBeLogged, envVariables);
    }

    /**
     * Executes operating system command.
     *
     * @param cmd command to be executed.
     * @param waitSecond time to wait for the command to be completed.
     * @return returns SystemCmdResult object which has all the execution details.
     */
    public SystemCmdResult execute(
            String cmd,
            int waitSecond,
            String workspace,
            boolean isCommandOutputToBeLogged,
            Map<String, String> envVariables) {
        SystemCmdResult systemCmdResult = new SystemCmdResult();
        systemCmdResult.setCommandExecuted(cmd);
        log.info("SystemCmd executing cmd: " + String.join(" ", cmd));
        try {
            String[] safeCmd = cmd.split(" +");
            ProcessBuilder processBuilder = new ProcessBuilder(safeCmd);
            if (Objects.nonNull(envVariables) && !envVariables.isEmpty()) {
                processBuilder.environment().putAll(envVariables);
            }
            processBuilder.directory(new File(workspace));
            Process process = processBuilder.start();
            readProcessOutput(process, systemCmdResult, isCommandOutputToBeLogged);

            if (waitSecond <= 0) {
                process.waitFor();
            } else {
                if (!process.waitFor(waitSecond, TimeUnit.SECONDS)) {
                    log.error("SystemCmd wait process failed");
                    systemCmdResult.setCommandSuccessful(false);
                }
            }
            if (process.exitValue() != 0) {
                log.error("SystemCmd process finished with abnormal value.");
                systemCmdResult.setCommandSuccessful(false);
            } else {
                systemCmdResult.setCommandSuccessful(true);
            }
            log.debug("stdout of the command: " + systemCmdResult.getCommandStdOutput());
            log.debug("stderr of the command: " + systemCmdResult.getCommandStdError());
        } catch (final IOException ex) {
            systemCmdResult.setCommandSuccessful(false);
            systemCmdResult.setCommandStdError(ex.getMessage());
        } catch (final InterruptedException ex) {
            log.error("SystemCmd process be interrupted.");
            Thread.currentThread().interrupt();
            systemCmdResult.setCommandSuccessful(false);
            systemCmdResult.setCommandStdError(ex.getMessage());
        } catch (ExecutionException e) {
            systemCmdResult.setCommandSuccessful(false);
            systemCmdResult.setCommandStdError(e.getMessage());
            throw new OpenTofuExecutorException(e.getMessage());
        }
        return systemCmdResult;
    }

    private String readStream(
            BufferedReader bufferedReader,
            Map<String, String> contextMap,
            boolean isCommandOutputToBeLogged) {
        // copying MDC context of the main deployment thread to the stream reader thread.
        MDC.setContextMap(contextMap);
        StringBuilder stringBuilder = new StringBuilder();
        bufferedReader
                .lines()
                .forEach(
                        line -> {
                            if (isCommandOutputToBeLogged) {
                                log.info(line);
                            }
                            // skip adding new line for the first line.
                            if (!stringBuilder.isEmpty()) {
                                stringBuilder.append(System.lineSeparator());
                            }
                            stringBuilder.append(line);
                        });
        return stringBuilder.toString();
    }

    private void readProcessOutput(
            Process process, SystemCmdResult systemCmdResult, boolean isCommandOutputToBeLogged)
            throws IOException, ExecutionException, InterruptedException {
        if (Objects.isNull(process)) {
            return;
        }
        final Map<String, String> contextMap =
                new HashMap<>(
                        Objects.nonNull(MDC.getCopyOfContextMap())
                                ? MDC.getCopyOfContextMap()
                                : new HashMap<>());

        // Starting threads in parallel to read stdout and stderr. This is needed because in
        // some cases reading stdout first works and in some cases reading stderr first works.
        // we now let both stdout and stderr streams to be fully read in parallel and then read
        // the output after the buffers are fully read.
        BufferedReader stdoutReader =
                new BufferedReader(new InputStreamReader(process.getInputStream()));

        try (ExecutorService threadToReadStdout = newSingleThreadExecutor()) {
            Future<String> stdOutFuture =
                    threadToReadStdout.submit(
                            () -> readStream(stdoutReader, contextMap, isCommandOutputToBeLogged));

            BufferedReader stdErrorReader =
                    new BufferedReader(new InputStreamReader(process.getErrorStream()));
            Future<String> stdErrFuture;
            try (ExecutorService threadToReadStdErr = newSingleThreadExecutor()) {
                stdErrFuture =
                        threadToReadStdErr.submit(
                                () ->
                                        readStream(
                                                stdErrorReader,
                                                contextMap,
                                                isCommandOutputToBeLogged));

                int count = 0;
                while (!stdOutFuture.isDone() || !stdErrFuture.isDone()) {
                    if (count++ < 10 || count % 100000 == 0) {
                        log.debug("Command output and error streams are still being read.");
                    }
                }

                systemCmdResult.setCommandStdError(stdErrFuture.get());
                systemCmdResult.setCommandStdOutput(stdOutFuture.get());
                threadToReadStdout.shutdown();
                threadToReadStdErr.shutdown();
            }
        }
    }
}
