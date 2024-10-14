package org.eclipse.xpanse.tofu.maker.opentofu.tool;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jakarta.annotation.Resource;

import java.io.File;

import org.eclipse.xpanse.tofu.maker.models.exceptions.InvalidOpenTofuToolException;
import org.eclipse.xpanse.tofu.maker.opentofu.utils.SystemCmd;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = {OpenTofuInstaller.class,
        OpenTofuVersionsHelper.class,
        OpenTofuVersionsCache.class,
        OpenTofuVersionsFetcher.class,
        SystemCmd.class})
class OpenTofuInstallerTest {

    @Resource
    private OpenTofuInstaller installer;
    @Resource
    private OpenTofuVersionsHelper versionHelper;

    @Test
    void testGetExecutableOpenTofuByVersion() {

        String requiredVersion = "";
        String openTofuPath = installer.getExecutorPathThatMatchesRequiredVersion(requiredVersion);
        assertEquals("tofu", openTofuPath);

        String requiredVersion1 = ">= 1.7.0";
        String[] operatorAndNumber1 = versionHelper.getOperatorAndNumberFromRequiredVersion(
                requiredVersion1);
        String openTofuPath1 =
                installer.getExecutorPathThatMatchesRequiredVersion(requiredVersion1);
        assertTrue(versionHelper.checkIfExecutorVersionIsValid(new File(openTofuPath1),
                operatorAndNumber1[0], operatorAndNumber1[1]));

        String requiredVersion2 = "= 1.6.0";
        String[] operatorAndNumber2 = versionHelper.getOperatorAndNumberFromRequiredVersion(
                requiredVersion2);
        String openTofuPath2 =
                installer.getExecutorPathThatMatchesRequiredVersion(requiredVersion2);
        assertTrue(versionHelper.checkIfExecutorVersionIsValid(new File(openTofuPath2),
                operatorAndNumber2[0], operatorAndNumber2[1]));

        String requiredVersion3 = ">= v1.8.0";
        String[] operatorAndNumber3 = versionHelper.getOperatorAndNumberFromRequiredVersion(
                requiredVersion3);
        String openTofuPath3 =
                installer.getExecutorPathThatMatchesRequiredVersion(requiredVersion3);
        assertTrue(versionHelper.checkIfExecutorVersionIsValid(new File(openTofuPath3),
                operatorAndNumber3[0], operatorAndNumber3[1]));

        String requiredVersion4 = ">= 100.0.0";
        assertThrows(InvalidOpenTofuToolException.class, () ->
                installer.getExecutorPathThatMatchesRequiredVersion(requiredVersion4));

        String requiredVersion5 = "< v1.6.0";
        assertThrows(InvalidOpenTofuToolException.class, () ->
                installer.getExecutorPathThatMatchesRequiredVersion(requiredVersion5));
    }
}
