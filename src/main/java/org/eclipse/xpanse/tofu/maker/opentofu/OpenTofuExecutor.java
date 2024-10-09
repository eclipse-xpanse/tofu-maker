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

/**
 * An executor for opentofu.
 */
@Slf4j
@Component
public class OpenTofuExecutor {

    private static final String VARS_FILE_NAME = "variables.tfvars.json";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static {
        OBJECT_MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    private final String moduleParentDirectoryPath;

    private final SystemCmd systemCmd;

    private final boolean isStdoutStdErrLoggingEnabled;

    private final String customOpenTofuBinary;

    private final String openTofuLogLevel;

    /**
     * Constructor for the OpenTofuExecutor bean.
     *
     * @param systemCmd                    SystemCmd bean
     * @param moduleParentDirectoryPath    value of `opentofu.root.module.directory` property
     * @param isStdoutStdErrLoggingEnabled value of `log.opentofu.stdout.stderr` property
     * @param customOpenTofuBinary         value of `opentofu.binary.location` property
     * @param openTofuLogLevel             value of `opentofu.log.level` property
     */
    @Autowired
    public OpenTofuExecutor(SystemCmd systemCmd,
                            @Value("${opentofu.root.module.directory}")
                            String moduleParentDirectoryPath,
                            @Value("${log.opentofu.stdout.stderr:true}")
                            boolean isStdoutStdErrLoggingEnabled,
                            @Value("${opentofu.binary.location}")
                            String customOpenTofuBinary,
                            @Value("${opentofu.log.level}")
                            String openTofuLogLevel
    ) {
        if (moduleParentDirectoryPath.isBlank() || moduleParentDirectoryPath.isEmpty()) {
            this.moduleParentDirectoryPath =
                    System.getProperty("java.io.tmpdir");
        } else {
            this.moduleParentDirectoryPath = moduleParentDirectoryPath;
        }
        this.systemCmd = systemCmd;
        this.customOpenTofuBinary = customOpenTofuBinary;
        this.isStdoutStdErrLoggingEnabled = isStdoutStdErrLoggingEnabled;
        this.openTofuLogLevel = openTofuLogLevel;
    }

    /**
     * OpenTofu executes init, plan and destroy commands.
     */
    public SystemCmdResult tfDestroy(String executorPath, Map<String, Object> variables,
                                     Map<String, String> envVariables, String moduleDirectory) {
        tfPlan(executorPath, variables, envVariables, moduleDirectory);
        SystemCmdResult applyResult = tfDestroyCommand(
                executorPath, variables, envVariables, getModuleFullPath(moduleDirectory));
        if (!applyResult.isCommandSuccessful()) {
            log.error("OpenTofuExecutor.tfDestroy failed.");
            throw new OpenTofuExecutorException("OpenTofuExecutor.tfDestroy failed.",
                    applyResult.getCommandStdError());
        }
        return applyResult;
    }

    /**
     * OpenTofu executes init, plan and apply commands.
     */
    public SystemCmdResult tfApply(String executorPath, Map<String, Object> variables,
                                   Map<String, String> envVariables,
                                   String moduleDirectory) {
        tfPlan(executorPath, variables, envVariables, moduleDirectory);
        SystemCmdResult applyResult =
                tfApplyCommand(executorPath, variables, envVariables,
                        getModuleFullPath(moduleDirectory));
        if (!applyResult.isCommandSuccessful()) {
            log.error("OpenTofuExecutor.tfApply failed.");
            throw new OpenTofuExecutorException("OpenTofuExecutor.tfApply failed.",
                    applyResult.getCommandStdError());
        }
        return applyResult;
    }

    /**
     * OpenTofu executes init and plan commands.
     */
    public SystemCmdResult tfPlan(String executorPath, Map<String, Object> variables,
                                  Map<String, String> envVariables,
                                  String moduleDirectory) {
        tfInit(executorPath, moduleDirectory);
        SystemCmdResult planResult =
                tfPlanCommand(executorPath, variables, envVariables,
                        getModuleFullPath(moduleDirectory));
        if (!planResult.isCommandSuccessful()) {
            log.error("OpenTofuExecutor.tfPlan failed.");
            throw new OpenTofuExecutorException("OpenTofuExecutor.tfPlan failed.",
                    planResult.getCommandStdError());
        }
        return planResult;
    }

    /**
     * Method to execute open tofu plan and get the plan as a json string.
     */
    public String getOpenTofuPlanAsJson(String executorPath, Map<String, Object> variables,
                                        Map<String, String> envVariables,
                                        String moduleDirectory) {
        tfInit(executorPath, moduleDirectory);
        SystemCmdResult tfPlanResult = executeWithVariables(
                new StringBuilder(
                        getOpenTofuCommand(executorPath,
                                "plan -input=false -no-color --out tfplan.binary ")),
                variables, envVariables, getModuleFullPath(moduleDirectory));
        if (!tfPlanResult.isCommandSuccessful()) {
            log.error("OpenTofuExecutor.tfPlan failed.");
            throw new OpenTofuExecutorException("OpenTofuExecutor.tfPlan failed.",
                    tfPlanResult.getCommandStdError());
        }
        SystemCmdResult planJsonResult =
                execute(getOpenTofuCommand(executorPath, "show -json tfplan.binary"),
                        getModuleFullPath(moduleDirectory), envVariables);
        if (!planJsonResult.isCommandSuccessful()) {
            log.error("Reading OpenTofu plan as JSON failed.");
            throw new OpenTofuExecutorException("Reading OpenTofu plan as JSON failed.",
                    planJsonResult.getCommandStdError());
        }
        return planJsonResult.getCommandStdOutput();
    }

    /**
     * OpenTofu executes the init command.
     */
    public SystemCmdResult tfValidate(String executorPath, String moduleDirectory) {
        tfInit(executorPath, moduleDirectory);
        return tfValidateCommand(executorPath, getModuleFullPath(moduleDirectory));
    }

    /**
     * OpenTofu executes the init command.
     */
    public void tfInit(String executorPath, String moduleDirectory) {
        SystemCmdResult initResult =
                tfInitCommand(executorPath, getModuleFullPath(moduleDirectory));
        if (!initResult.isCommandSuccessful()) {
            log.error("OpenTofuExecutor.tfInit failed.");
            throw new OpenTofuExecutorException("OpenTofuExecutor.tfInit failed.",
                    initResult.getCommandStdError());
        }
    }

    /**
     * Get the full path of Module.
     */
    public String getModuleFullPath(String moduleDirectory) {
        return this.moduleParentDirectoryPath + File.separator + moduleDirectory;
    }


    /**
     * Executes open tofu init command.
     *
     * @return Returns result of SystemCmd executed.
     */
    private SystemCmdResult tfInitCommand(String executorPath, String workspace) {
        return execute(getOpenTofuCommand(executorPath, "init -no-color"),
                workspace, new HashMap<>());
    }

    /**
     * Executes open tofu validate command.
     *
     * @return Returns result of SystemCmd executed.
     */
    private SystemCmdResult tfValidateCommand(String executorPath, String workspace) {
        return execute(getOpenTofuCommand(executorPath, "validate -json -no-color"),
                workspace, new HashMap<>());
    }

    /**
     * Executes open tofu plan command.
     *
     * @return Returns result of SystemCmd executed.
     */
    private SystemCmdResult tfPlanCommand(String executorPath, Map<String, Object> variables,
                                          Map<String, String> envVariables, String workspace) {
        return executeWithVariables(
                new StringBuilder(getOpenTofuCommand(executorPath, "plan -input=false -no-color ")),
                variables, envVariables, workspace);
    }

    /**
     * Executes open tofu apply command.
     *
     * @return Returns result of SystemCmd executed.
     */
    private SystemCmdResult tfApplyCommand(String executorPath, Map<String, Object> variables,
                                           Map<String, String> envVariables, String workspace) {
        return executeWithVariables(
                new StringBuilder(
                        getOpenTofuCommand(executorPath,
                                "apply -auto-approve -input=false -no-color ")),
                variables, envVariables, workspace);
    }

    /**
     * Executes open tofu destroy command.
     *
     * @return Returns result of SystemCmd executed.
     */
    private SystemCmdResult tfDestroyCommand(String executorPath, Map<String, Object> variables,
                                             Map<String, String> envVariables, String workspace) {
        return executeWithVariables(
                new StringBuilder(executorPath + " destroy -auto-approve -input=false -no-color "),
                variables, envVariables, workspace);
    }

    /**
     * Executes open tofu commands with parameters.
     *
     * @return Returns result of SystemCmd executed.
     */
    private SystemCmdResult executeWithVariables(StringBuilder command,
                                                 Map<String, Object> variables,
                                                 Map<String, String> envVariables,
                                                 String workspace) {
        createVariablesFile(variables, workspace);
        command.append(" -var-file=");
        command.append(VARS_FILE_NAME);
        SystemCmdResult systemCmdResult = execute(command.toString(), workspace, envVariables);
        cleanUpVariablesFile(workspace);
        return systemCmdResult;
    }

    /**
     * Executes open tofu commands.
     *
     * @return SystemCmdResult
     */
    private SystemCmdResult execute(String cmd, String workspace,
                                    @NonNull Map<String, String> envVariables) {
        envVariables.putAll(getOpenTofuLogConfig());
        return this.systemCmd.execute(cmd, workspace, this.isStdoutStdErrLoggingEnabled,
                envVariables);
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

    private void createVariablesFile(Map<String, Object> variables, String workspace) {
        try {
            log.info("creating variables file");
            OBJECT_MAPPER.writeValue(new File(getVariablesFilePath(workspace)), variables);
        } catch (IOException ioException) {
            throw new OpenTofuExecutorException("Creating variables file failed",
                    ioException.getMessage());
        }
    }

    private void cleanUpVariablesFile(String workspace) {
        File file = new File(getVariablesFilePath(workspace));
        try {
            log.info("cleaning up variables file");
            Files.deleteIfExists(file.toPath());
        } catch (IOException ioException) {
            log.error("Cleanup of variables file failed", ioException);
        }
    }

    private String getVariablesFilePath(String workspace) {
        return workspace + File.separator + VARS_FILE_NAME;
    }
}
