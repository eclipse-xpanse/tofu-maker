/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.tofu.maker.opentofu.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.tofu.maker.async.TaskConfiguration;
import org.eclipse.xpanse.tofu.maker.models.OpenTofuMakerSystemStatus;
import org.eclipse.xpanse.tofu.maker.models.enums.DestroyScenario;
import org.eclipse.xpanse.tofu.maker.models.enums.HealthStatus;
import org.eclipse.xpanse.tofu.maker.models.exceptions.OpenTofuExecutorException;
import org.eclipse.xpanse.tofu.maker.models.exceptions.OpenTofuHealthCheckException;
import org.eclipse.xpanse.tofu.maker.models.plan.OpenTofuPlan;
import org.eclipse.xpanse.tofu.maker.models.plan.OpenTofuPlanFromDirectoryRequest;
import org.eclipse.xpanse.tofu.maker.models.request.directory.OpenTofuAsyncDeployFromDirectoryRequest;
import org.eclipse.xpanse.tofu.maker.models.request.directory.OpenTofuAsyncDestroyFromDirectoryRequest;
import org.eclipse.xpanse.tofu.maker.models.request.directory.OpenTofuDeployFromDirectoryRequest;
import org.eclipse.xpanse.tofu.maker.models.request.directory.OpenTofuDestroyFromDirectoryRequest;
import org.eclipse.xpanse.tofu.maker.models.response.OpenTofuResult;
import org.eclipse.xpanse.tofu.maker.models.validation.OpenTofuValidationResult;
import org.eclipse.xpanse.tofu.maker.opentofu.OpenTofuExecutor;
import org.eclipse.xpanse.tofu.maker.opentofu.utils.SystemCmdResult;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * OpenTofu service classes are deployed form Directory.
 */
@Slf4j
@Service
public class OpenTofuDirectoryService {

    private static final String STATE_FILE_NAME = "terraform.tfstate";
    private static final String TEST_FILE_NAME = "hello-world.tf";
    private static final String HEALTH_CHECK_DIR = UUID.randomUUID().toString();
    private static final List<String> EXCLUDED_FILE_SUFFIX_LIST =
            Arrays.asList(".tf", ".tfstate", ".hcl");
    private static final String HELLO_WORLD_TEMPLATE = """
            output "hello_world" {
                value = "Hello, World!"
            }
            """;

    private final OpenTofuExecutor executor;
    private final RestTemplate restTemplate;

    @Value("${deployment.clean.workspace.enabled:true}")
    private Boolean cleanWorkspaceAfterDeployment;

    @Autowired
    public OpenTofuDirectoryService(OpenTofuExecutor executor, RestTemplate restTemplate) {
        this.executor = executor;
        this.restTemplate = restTemplate;
    }

    /**
     * Perform OpenTofu health checks by creating a OpenTofu test configuration file.
     *
     * @return OpenTofuBootSystemStatus.
     */
    public OpenTofuMakerSystemStatus tfHealthCheck() {
        String filePath = executor.getModuleFullPath(HEALTH_CHECK_DIR) + File.separator
                + TEST_FILE_NAME;
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            FileWriter writer = new FileWriter(filePath);
            writer.write(HELLO_WORLD_TEMPLATE);
            writer.close();
        } catch (IOException e) {
            throw new OpenTofuHealthCheckException(
                    "Error creating or writing to file '" + filePath + "': " + e.getMessage());
        }
        OpenTofuValidationResult openTofuValidationResult =
                tfValidateFromDirectory(HEALTH_CHECK_DIR);
        OpenTofuMakerSystemStatus systemStatus = new OpenTofuMakerSystemStatus();
        if (openTofuValidationResult.isValid()) {
            systemStatus.setHealthStatus(HealthStatus.OK);
            return systemStatus;
        }
        systemStatus.setHealthStatus(HealthStatus.NOK);
        return systemStatus;
    }

    /**
     * Executes openTofu validate command.
     *
     * @return TfValidationResult.
     */
    public OpenTofuValidationResult tfValidateFromDirectory(String moduleDirectory) {
        try {
            SystemCmdResult systemCmdResult = executor.tfValidate(moduleDirectory);
            return new ObjectMapper().readValue(systemCmdResult.getCommandStdOutput(),
                    OpenTofuValidationResult.class);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Serialising string to object failed.", ex);
        }
    }

    /**
     * Deploy a source by openTofu.
     */
    public OpenTofuResult deployFromDirectory(OpenTofuDeployFromDirectoryRequest request,
                                              String moduleDirectory) {
        SystemCmdResult result;
        try {
            if (Boolean.TRUE.equals(request.getIsPlanOnly())) {
                result = executor.tfPlan(request.getVariables(), request.getEnvVariables(),
                        moduleDirectory);
            } else {
                result = executor.tfApply(request.getVariables(), request.getEnvVariables(),
                        moduleDirectory);
            }
        } catch (OpenTofuExecutorException tfEx) {
            log.error("OpenTofu deploy service failed. error:{}", tfEx.getMessage());
            result = new SystemCmdResult();
            result.setCommandSuccessful(false);
            result.setCommandStdError(tfEx.getMessage());
        }
        String workspace = executor.getModuleFullPath(moduleDirectory);
        OpenTofuResult openTofuResult =
                transSystemCmdResultToOpenTofuResult(result, workspace, null);
        if (cleanWorkspaceAfterDeployment) {
            deleteWorkspace(workspace);
        }
        return openTofuResult;
    }

    /**
     * Destroy resource of the service.
     */
    public OpenTofuResult destroyFromDirectory(OpenTofuDestroyFromDirectoryRequest request,
                                               String moduleDirectory) {
        SystemCmdResult result;
        try {
            result = executor.tfDestroy(request.getVariables(),
                    request.getEnvVariables(), moduleDirectory);
        } catch (OpenTofuExecutorException tfEx) {
            log.error("OpenTofu destroy service failed. error:{}", tfEx.getMessage());
            result = new SystemCmdResult();
            result.setCommandSuccessful(false);
            result.setCommandStdError(tfEx.getMessage());
        }
        String workspace = executor.getModuleFullPath(moduleDirectory);
        OpenTofuResult openTofuResult = transSystemCmdResultToOpenTofuResult(
                result, workspace, request.getDestroyScenario());
        deleteWorkspace(workspace);
        return openTofuResult;
    }

    /**
     * Executes openTofu plan command on a directory and returns the plan as a JSON string.
     */
    public OpenTofuPlan getOpenTofuPlanFromDirectory(OpenTofuPlanFromDirectoryRequest request,
                                                     String moduleDirectory) {
        String result = executor.getOpenTofuPlanAsJson(request.getVariables(),
                request.getEnvVariables(), moduleDirectory);
        deleteWorkspace(executor.getModuleFullPath(moduleDirectory));
        return OpenTofuPlan.builder().plan(result).build();
    }

    /**
     * Async deploy a source by openTofu.
     */
    @Async(TaskConfiguration.TASK_EXECUTOR_NAME)
    public void asyncDeployWithScripts(
            OpenTofuAsyncDeployFromDirectoryRequest asyncDeployRequest, String moduleDirectory) {
        OpenTofuResult result;
        try {
            result = deployFromDirectory(asyncDeployRequest, moduleDirectory);
        } catch (RuntimeException e) {
            result = OpenTofuResult.builder()
                    .commandStdOutput(null)
                    .commandStdError(e.getMessage())
                    .isCommandSuccessful(false)
                    .terraformState(null)
                    .importantFileContentMap(new HashMap<>())
                    .build();
        }
        String url = asyncDeployRequest.getWebhookConfig().getUrl();
        log.info("Deployment service complete, callback POST url:{}, requestBody:{}", url, result);
        restTemplate.postForLocation(url, result);
    }

    /**
     * Async destroy resource of the service.
     */
    @Async(TaskConfiguration.TASK_EXECUTOR_NAME)
    public void asyncDestroyWithScripts(OpenTofuAsyncDestroyFromDirectoryRequest request,
                                        String moduleDirectory) {
        OpenTofuResult result;
        try {
            result = destroyFromDirectory(request, moduleDirectory);
        } catch (RuntimeException e) {
            result = OpenTofuResult.builder()
                    .commandStdOutput(null)
                    .commandStdError(e.getMessage())
                    .isCommandSuccessful(false)
                    .terraformState(null)
                    .importantFileContentMap(new HashMap<>())
                    .build();
        }

        String url = request.getWebhookConfig().getUrl();
        log.info("Destroy service complete, callback POST url:{}, requestBody:{}", url, result);
        restTemplate.postForLocation(url, result);
    }

    private OpenTofuResult transSystemCmdResultToOpenTofuResult(SystemCmdResult result,
                                                                String workspace,
                                                                DestroyScenario destroyScenario) {
        OpenTofuResult openTofuResult = OpenTofuResult.builder().build();
        BeanUtils.copyProperties(result, openTofuResult);
        openTofuResult.setDestroyScenario(destroyScenario);
        openTofuResult.setTerraformState(getTerraformState(workspace));
        openTofuResult.setImportantFileContentMap(getImportantFilesContent(workspace));
        return openTofuResult;
    }

    /**
     * Get the content of the tfState file.
     */
    private String getTerraformState(String workspace) {
        String state = null;
        try {
            File tfState = new File(workspace + File.separator + STATE_FILE_NAME);
            if (tfState.exists()) {
                state = Files.readString(tfState.toPath());
            }
        } catch (IOException ex) {
            log.error("Read state file failed.", ex);
        }
        return state;
    }

    /**
     * get file content.
     */
    private Map<String, String> getImportantFilesContent(String workspace) {
        Map<String, String> fileContentMap = new HashMap<>();
        File workPath = new File(workspace);
        if (workPath.isDirectory() && workPath.exists()) {
            File[] files = workPath.listFiles();
            if (Objects.nonNull(files)) {
                List<File> importantFiles = Arrays.stream(files)
                        .filter(file -> file.isFile() && !isExcludedFile(file.getName())).toList();
                for (File importantFile : importantFiles) {
                    try {
                        String content = Files.readString(importantFile.toPath());
                        fileContentMap.put(importantFile.getName(), content);
                    } catch (IOException e) {
                        log.error("Read content of file with name:{} error.",
                                importantFile.getName(), e);
                    }
                }
            }
        }
        return fileContentMap;
    }

    private void deleteWorkspace(String workspace) {
        Path path = Paths.get(workspace);
        try {
            Files.walk(path).sorted(Comparator.reverseOrder()).map(Path::toFile)
                    .forEach(File::delete);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    private boolean isExcludedFile(String fileName) {
        String fileSuffix = fileName.substring(fileName.lastIndexOf("."));
        return EXCLUDED_FILE_SUFFIX_LIST.contains(fileSuffix);
    }
}
