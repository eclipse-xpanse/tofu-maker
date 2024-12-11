/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.tofu.maker.opentofu;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.tofu.maker.models.exceptions.OpenTofuExecutorException;
import org.eclipse.xpanse.tofu.maker.opentofu.utils.SystemCmd;
import org.eclipse.xpanse.tofu.maker.opentofu.utils.SystemCmdResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** An executor for openTofu. */
@Slf4j
@Component
public class OpenTofuExecutor {

    private static final String TF_VARS_FILE_NAME = "variables.tfvars.json";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static {
        OBJECT_MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    private final SystemCmd systemCmd;

    private final boolean isStdoutStdErrLoggingEnabled;

    private final String customOpenTofuBinary;

    private final String openTofuLogLevel;

    /**
     * Constructor for the OpenTofuExecutor bean.
     *
     * @param systemCmd SystemCmd bean
     * @param isStdoutStdErrLoggingEnabled value of `log.openTofu.stdout.stderr` property
     * @param customOpenTofuBinary value of `openTofu.binary.location` property
     * @param openTofuLogLevel value of `openTofu.log.level` property
     */
    @Autowired
    public OpenTofuExecutor(
            SystemCmd systemCmd,
            @Value("${log.opentofu.stdout.stderr:true}") boolean isStdoutStdErrLoggingEnabled,
            @Value("${opentofu.binary.location}") String customOpenTofuBinary,
            @Value("${opentofu.log.level}") String openTofuLogLevel) {
        this.systemCmd = systemCmd;
        this.customOpenTofuBinary = customOpenTofuBinary;
        this.isStdoutStdErrLoggingEnabled = isStdoutStdErrLoggingEnabled;
        this.openTofuLogLevel = openTofuLogLevel;
    }

    /** OpenTofu executes init, plan and destroy commands. */
    public SystemCmdResult tfDestroy(
            String executorPath,
            Map<String, Object> variables,
            Map<String, String> envVariables,
            String taskWorkspace) {
        tfPlan(executorPath, variables, envVariables, taskWorkspace);
        SystemCmdResult applyResult =
                tfDestroyCommand(executorPath, variables, envVariables, taskWorkspace);
        if (!applyResult.isCommandSuccessful()) {
            log.error("TFExecutor.tfDestroy failed.");
            throw new OpenTofuExecutorException(
                    "TFExecutor.tfDestroy failed.", applyResult.getCommandStdError());
        }
        return applyResult;
    }

    /** OpenTofu executes init, plan and apply commands. */
    public SystemCmdResult tfApply(
            String executorPath,
            Map<String, Object> variables,
            Map<String, String> envVariables,
            String taskWorkspace) {
        tfPlan(executorPath, variables, envVariables, taskWorkspace);
        SystemCmdResult applyResult =
                tfApplyCommand(executorPath, variables, envVariables, taskWorkspace);
        if (!applyResult.isCommandSuccessful()) {
            log.error("TFExecutor.tfApply failed.");
            throw new OpenTofuExecutorException(
                    "TFExecutor.tfApply failed.", applyResult.getCommandStdError());
        }
        return applyResult;
    }

    /** OpenTofu executes init and plan commands. */
    public SystemCmdResult tfPlan(
            String executorPath,
            Map<String, Object> variables,
            Map<String, String> envVariables,
            String taskWorkspace) {
        tfInit(executorPath, taskWorkspace);
        SystemCmdResult planResult =
                tfPlanCommand(executorPath, variables, envVariables, taskWorkspace);
        if (!planResult.isCommandSuccessful()) {
            log.error("TFExecutor.tfPlan failed.");
            throw new OpenTofuExecutorException(
                    "TFExecutor.tfPlan failed.", planResult.getCommandStdError());
        }
        return planResult;
    }

    /** Method to execute openTofu plan and get the plan as a json string. */
    public String getOpenTofuPlanAsJson(
            String executorPath,
            Map<String, Object> variables,
            Map<String, String> envVariables,
            String taskWorkspace) {
        tfInit(executorPath, taskWorkspace);
        SystemCmdResult tfPlanResult =
                executeWithVariables(
                        new StringBuilder(
                                getOpenTofuCommand(
                                        executorPath,
                                        "plan -input=false -no-color --out tfplan.binary ")),
                        variables,
                        envVariables,
                        taskWorkspace);
        if (!tfPlanResult.isCommandSuccessful()) {
            log.error("TFExecutor.tfPlan failed.");
            throw new OpenTofuExecutorException(
                    "TFExecutor.tfPlan failed.", tfPlanResult.getCommandStdError());
        }
        SystemCmdResult planJsonResult =
                execute(
                        getOpenTofuCommand(executorPath, "show -json tfplan.binary"),
                        taskWorkspace,
                        envVariables);
        if (!planJsonResult.isCommandSuccessful()) {
            log.error("Reading OpenTofu plan as JSON failed.");
            throw new OpenTofuExecutorException(
                    "Reading OpenTofu plan as JSON failed.", planJsonResult.getCommandStdError());
        }
        return planJsonResult.getCommandStdOutput();
    }

    /** OpenTofu executes the init command. */
    public SystemCmdResult tfValidate(String executorPath, String taskWorkspace) {
        tfInit(executorPath, taskWorkspace);
        return tfValidateCommand(executorPath, taskWorkspace);
    }

    /** OpenTofu executes the init command. */
    public void tfInit(String executorPath, String taskWorkspace) {
        SystemCmdResult initResult = tfInitCommand(executorPath, taskWorkspace);
        if (!initResult.isCommandSuccessful()) {
            log.error("TFExecutor.tfInit failed.");
            throw new OpenTofuExecutorException(
                    "TFExecutor.tfInit failed.", initResult.getCommandStdError());
        }
    }

    /**
     * Executes openTofu init command.
     *
     * @return Returns result of SystemCmd executed.
     */
    private SystemCmdResult tfInitCommand(String executorPath, String taskWorkspace) {
        return execute(
                getOpenTofuCommand(executorPath, "init -no-color"), taskWorkspace, new HashMap<>());
    }

    /**
     * Executes openTofu validate command.
     *
     * @return Returns result of SystemCmd executed.
     */
    private SystemCmdResult tfValidateCommand(String executorPath, String taskWorkspace) {
        return execute(
                getOpenTofuCommand(executorPath, "validate -json -no-color"),
                taskWorkspace,
                new HashMap<>());
    }

    /**
     * Executes openTofu plan command.
     *
     * @return Returns result of SystemCmd executed.
     */
    private SystemCmdResult tfPlanCommand(
            String executorPath,
            Map<String, Object> variables,
            Map<String, String> envVariables,
            String taskWorkspace) {
        return executeWithVariables(
                new StringBuilder(getOpenTofuCommand(executorPath, "plan -input=false -no-color ")),
                variables,
                envVariables,
                taskWorkspace);
    }

    /**
     * Executes openTofu apply command.
     *
     * @return Returns result of SystemCmd executed.
     */
    private SystemCmdResult tfApplyCommand(
            String executorPath,
            Map<String, Object> variables,
            Map<String, String> envVariables,
            String taskWorkspace) {
        return executeWithVariables(
                new StringBuilder(
                        getOpenTofuCommand(
                                executorPath, "apply -auto-approve -input=false -no-color ")),
                variables,
                envVariables,
                taskWorkspace);
    }

    /**
     * Executes openTofu destroy command.
     *
     * @return Returns result of SystemCmd executed.
     */
    private SystemCmdResult tfDestroyCommand(
            String executorPath,
            Map<String, Object> variables,
            Map<String, String> envVariables,
            String taskWorkspace) {
        return executeWithVariables(
                new StringBuilder(executorPath + " destroy -auto-approve -input=false -no-color "),
                variables,
                envVariables,
                taskWorkspace);
    }

    /**
     * Executes openTofu commands with parameters.
     *
     * @return Returns result of SystemCmd executed.
     */
    private SystemCmdResult executeWithVariables(
            StringBuilder command,
            Map<String, Object> variables,
            Map<String, String> envVariables,
            String taskWorkspace) {
        createVariablesFile(variables, taskWorkspace);
        command.append(" -var-file=");
        command.append(TF_VARS_FILE_NAME);
        SystemCmdResult systemCmdResult = execute(command.toString(), taskWorkspace, envVariables);
        cleanUpVariablesFile(taskWorkspace);
        return systemCmdResult;
    }

    /**
     * Executes openTofu commands.
     *
     * @return SystemCmdResult
     */
    private SystemCmdResult execute(
            String cmd, String taskWorkspace, @NonNull Map<String, String> envVariables) {
        envVariables.putAll(getOpenTofuLogConfig());
        return this.systemCmd.execute(
                cmd, taskWorkspace, this.isStdoutStdErrLoggingEnabled, envVariables);
    }

    private String getOpenTofuCommand(String executorPath, String openTofuArguments) {
        if (Objects.isNull(this.customOpenTofuBinary) || this.customOpenTofuBinary.isBlank()) {
            return executorPath + " " + openTofuArguments;
        }
        return this.customOpenTofuBinary + " " + openTofuArguments;
    }

    private Map<String, String> getOpenTofuLogConfig() {
        return Collections.singletonMap("TF_LOG", this.openTofuLogLevel);
    }

    private void createVariablesFile(Map<String, Object> variables, String taskWorkspace) {
        try {
            log.info("creating variables file");
            File varFile = new File(taskWorkspace, TF_VARS_FILE_NAME);
            OBJECT_MAPPER.writeValue(varFile, variables);
        } catch (IOException ioException) {
            throw new OpenTofuExecutorException(
                    "Creating variables file failed", ioException.getMessage());
        }
    }

    private void cleanUpVariablesFile(String taskWorkspace) {
        File file = new File(taskWorkspace, TF_VARS_FILE_NAME);
        try {
            log.info("cleaning up variables file");
            Files.deleteIfExists(file.toPath());
        } catch (IOException ioException) {
            log.error("Cleanup of variables file failed", ioException);
        }
    }
}
