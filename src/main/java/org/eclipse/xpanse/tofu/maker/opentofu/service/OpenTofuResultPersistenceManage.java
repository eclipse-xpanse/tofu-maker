/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.tofu.maker.opentofu.service;

import jakarta.annotation.Resource;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.xpanse.tofu.maker.models.response.OpenTofuResult;
import org.eclipse.xpanse.tofu.maker.models.response.ReFetchResult;
import org.eclipse.xpanse.tofu.maker.models.response.ReFetchState;
import org.eclipse.xpanse.tofu.maker.utils.OpenTofuResultSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** OpenTofu service classes are manage task result. */
@Slf4j
@Component
public class OpenTofuResultPersistenceManage {

    private static final String TF_RESULT_FILE_SUFFIX = ".dat";
    private static final String TF_LOCK_FILE_NAME = ".terraform.tfstate.lock.info";

    @Value("${failed.callback.response.store.location}")
    private String failedCallbackStoreLocation;

    @Resource private OpenTofuScriptsHelper scriptsHelper;
    @Resource private OpenTofuResultSerializer openTofuResultSerializer;

    /**
     * When the tofu-maker callback fails, store the OpenTofuResult in the local file system.
     *
     * @param result OpenTofuResult.
     */
    public void persistOpenTofuResult(OpenTofuResult result) {
        File filePath = getFilePath(result.getRequestId());
        if (!filePath.exists() && !filePath.mkdirs()) {
            log.error("Failed to create directory {}", filePath);
            return;
        }
        File file = new File(filePath, getFileName(result.getRequestId()));
        byte[] openTofuResultData = openTofuResultSerializer.serialize(result);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(openTofuResultData);
            log.info(
                    "openTofu result successfully stored to directoryName: {}",
                    result.getRequestId());
        } catch (IOException e) {
            String errorMsg =
                    String.format(
                            "storing openTofu result to " + "directoryName %s failed. %s",
                            result.getRequestId(), e);
            log.error(errorMsg);
        }
    }

    /**
     * Get the OpenTofuResult object stored when the tofu-maker callback fails by RequestId.
     *
     * @param requestId requestId.
     * @return OpenTofuResult.
     */
    public ReFetchResult retrieveOpenTofuResultByRequestId(UUID requestId) {
        File resultFile = new File(getFilePath(requestId), getFileName(requestId));
        if (!isValidResultFile(resultFile)) {
            String errorMsg = String.format("Not found result file for requestId %s.", requestId);
            if (isDeployingInProgress(requestId)) {
                errorMsg = errorMsg + " The order is still in progress.";
                return buildErrorResponse(requestId, errorMsg, ReFetchState.ORDER_IN_PROGRESS);
            }
            return buildErrorResponse(requestId, errorMsg, ReFetchState.RESULT_NOT_FOUND);
        }
        try (FileInputStream fis = new FileInputStream(resultFile)) {
            OpenTofuResult terraformResult =
                    openTofuResultSerializer.deserialize(fis.readAllBytes());
            deleteResultFileAndDirectory(resultFile);
            return ReFetchResult.builder()
                    .requestId(requestId)
                    .state(ReFetchState.OK)
                    .openTofuResult(terraformResult)
                    .build();
        } catch (Exception e) {
            String errorMsg =
                    String.format("Failed to parse result file for requestId %s", requestId);
            return buildErrorResponse(requestId, errorMsg, ReFetchState.RESULT_PARSE_FAILED);
        }
    }

    private ReFetchResult buildErrorResponse(
            UUID requestId, String errorMessage, ReFetchState state) {
        log.error(errorMessage);
        return ReFetchResult.builder()
                .requestId(requestId)
                .state(state)
                .errorMessage(errorMessage)
                .build();
    }

    private boolean isValidResultFile(File file) {
        return file.exists() && file.isFile();
    }

    private boolean isDeployingInProgress(UUID requestId) {
        File workspace = scriptsHelper.getTaskWorkspace(requestId.toString());
        File targetFile = new File(workspace, TF_LOCK_FILE_NAME);
        return targetFile.exists() && targetFile.isFile();
    }

    private void deleteResultFileAndDirectory(File resultFile) {
        try {
            deleteRecursively(resultFile);
            log.info("File folder deleted successfully: {}", resultFile.getAbsolutePath());
        } catch (Exception e) {
            log.error("An error occurred while deleting files: {}", e.getMessage());
        }
    }

    private void deleteRecursively(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File child : files) {
                    deleteRecursively(child);
                }
            }
        }
        if (file.delete()) {
            log.info("File deleted successfully: {}", file.getAbsolutePath());
        }
    }

    private File getFilePath(UUID requestId) {
        return new File(failedCallbackStoreLocation + File.separator + requestId);
    }

    private String getFileName(UUID requestId) {
        return requestId + TF_RESULT_FILE_SUFFIX;
    }
}
