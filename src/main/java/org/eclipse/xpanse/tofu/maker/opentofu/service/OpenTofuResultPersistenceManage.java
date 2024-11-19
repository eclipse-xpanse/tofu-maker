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
import org.eclipse.xpanse.tofu.maker.models.exceptions.ResultAlreadyReturnedOrRequestIdInvalidException;
import org.eclipse.xpanse.tofu.maker.models.response.OpenTofuResult;
import org.eclipse.xpanse.tofu.maker.utils.OpenTofuResultSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * OpenTofu service classes are manage task result.
 */
@Slf4j
@Component
public class OpenTofuResultPersistenceManage {

    private static final String TF_RESULT_FILE_SUFFIX = ".dat";

    @Value("${failed.callback.response.store.location}")
    private String failedCallbackStoreLocation;

    @Resource
    private OpenTofuResultSerializer openTofuResultSerializer;

    /**
     * When the tofu-maker callback fails, store the OpenTofuResult in the local file system.
     *
     * @param result OpenTofuResult.
     */
    public void persistOpenTofuResult(OpenTofuResult result) {
        String filePath = getFilePath(result.getRequestId());
        File file = new File(filePath);
        if (!file.exists() && !file.mkdirs()) {
            log.error("Failed to create directory {}", filePath);
            return;
        }
        byte[] openTofuResultData = openTofuResultSerializer.serialize(result);
        try (FileOutputStream fos = new FileOutputStream(file.getPath() + File.separator
                + result.getRequestId() + TF_RESULT_FILE_SUFFIX)) {
            fos.write(openTofuResultData);
            log.info("openTofu result successfully stored to directoryName: {}",
                    result.getRequestId());
        } catch (IOException e) {
            String errorMsg = String.format("storing openTofu result to "
                    + "directoryName %s failed. %s", result.getRequestId(), e);
            log.error(errorMsg);
        }
    }

    /**
     * Get the OpenTofuResult object stored when the tofu-maker callback fails by RequestId.
     *
     * @param requestId requestId.
     * @return OpenTofuResult.
     */
    public OpenTofuResult retrieveOpenTofuResultByRequestId(String requestId) {
        String filePath = getFilePath(UUID.fromString(requestId));
        File resultFile = new File(filePath + File.separator + requestId + TF_RESULT_FILE_SUFFIX);
        if (!resultFile.exists() && !resultFile.isFile()) {
            throw new ResultAlreadyReturnedOrRequestIdInvalidException(
                    "Result file does not exist: " + resultFile.getAbsolutePath());
        }
        try (FileInputStream fis = new FileInputStream(resultFile)) {
            byte[] openTofuResultData = fis.readAllBytes();
            OpenTofuResult openTofuResult = openTofuResultSerializer
                    .deserialize(openTofuResultData);
            fis.close();
            deleteResultFileAndDirectory(new File(filePath));
            return openTofuResult;
        } catch (IOException e) {
            log.error("Failed to retrieve OpenTofuResult for requestId: {}", requestId, e);
            throw new ResultAlreadyReturnedOrRequestIdInvalidException(
                    "Failed to retrieve OpenTofuResult for requestId: " + requestId);
        }
    }

    private void deleteResultFileAndDirectory(File resultFile) {
        try {
            deleteRecursively(resultFile);
            log.info("File folder deleted successfully: " + resultFile.getAbsolutePath());
        } catch (Exception e) {
            log.error("An error occurred while deleting files: " + e.getMessage());
            e.printStackTrace();
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
        file.delete();
    }

    private String getFilePath(UUID requestId) {
        return failedCallbackStoreLocation + File.separator + requestId.toString();
    }

}
