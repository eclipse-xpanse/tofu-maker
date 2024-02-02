/*
 * SPDX-License-Identifier: Apache-2.0
 * SPDX-FileCopyrightText: Huawei Inc.
 */

package org.eclipse.xpanse.tofu.maker.opentofu.service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.xpanse.tofu.maker.models.exceptions.OpenTofuExecutorException;
import org.eclipse.xpanse.tofu.maker.opentofu.OpenTofuExecutor;
import org.springframework.stereotype.Component;

/**
 * bean to host all generic methods shared from different types of OpenTofu deployers.
 */
@Slf4j
@Component
public class OpenTofuScriptsHelper {

    private static final String STATE_FILE_NAME = "terraform.tfstate";

    private final OpenTofuExecutor openTofuExecutor;

    public OpenTofuScriptsHelper(OpenTofuExecutor openTofuExecutor) {
        this.openTofuExecutor = openTofuExecutor;
    }

    /**
     * Creates the tfstate file in the directory of the OpenTofu module.
     *
     * @param tfState        state file contents as string.
     * @param moduleLocation module location where the file must be created.
     */
    public void createTfStateFile(String tfState, String moduleLocation) {
        if (StringUtils.isBlank(tfState)) {
            throw new OpenTofuExecutorException("tfState file create error");
        }
        String fileName =
                openTofuExecutor.getModuleFullPath(moduleLocation)
                        + File.separator
                        + STATE_FILE_NAME;
        boolean overwrite = new File(fileName).exists();
        try (FileWriter scriptWriter = new FileWriter(fileName, overwrite)) {
            scriptWriter.write(tfState);
            log.info("tfState file create success, fileName: {}", fileName);
        } catch (IOException ex) {
            log.error("tfState file create failed.", ex);
            throw new OpenTofuExecutorException("tfState file create failed.", ex);
        }
    }
}