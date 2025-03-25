package org.eclipse.xpanse.tofu.maker.opentofu.tool;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import jakarta.annotation.Resource;
import java.io.File;
import java.util.Set;
import org.eclipse.xpanse.tofu.maker.models.exceptions.InvalidOpenTofuToolException;
import org.eclipse.xpanse.tofu.maker.opentofu.utils.SystemCmd;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
        classes = {
            OpenTofuInstaller.class,
            OpenTofuVersionsHelper.class,
            OpenTofuVersionsCache.class,
            OpenTofuVersionsFetcher.class,
            SystemCmd.class
        },
        properties = {"support.default.opentofu.versions.only=false"})
class OpenTofuInstallerTest {
    private static WireMockServer wireMockServer;

    @Value("${opentofu.default.supported.versions:1.6.0,1.7.0,1.8.0}")
    private String opentofuVersions;

    @Resource private OpenTofuInstaller installer;
    @Resource private OpenTofuVersionsHelper versionHelper;
    @Resource private OpenTofuVersionsCache versionsCache;

    @Disabled
    @Test
    void testGetExecutableOpenTofuByVersion() {
        Set<String> defaultVersions = Set.of(opentofuVersions.split(","));
        Set<String> cachedVersions = versionsCache.getAvailableVersions();
        assertTrue(cachedVersions.containsAll(defaultVersions));
        assertTrue(cachedVersions.size() >= defaultVersions.size());

        String requiredVersion = "";
        String openTofuPath = installer.getExecutorPathThatMatchesRequiredVersion(requiredVersion);
        assertEquals("tofu", openTofuPath);

        String requiredVersion1 = ">= 1.7.0";
        String[] operatorAndNumber1 =
                versionHelper.getOperatorAndNumberFromRequiredVersion(requiredVersion1);
        String openTofuPath1 =
                installer.getExecutorPathThatMatchesRequiredVersion(requiredVersion1);
        assertTrue(
                versionHelper.checkIfExecutorIsMatchedRequiredVersion(
                        new File(openTofuPath1), operatorAndNumber1[0], operatorAndNumber1[1]));

        String requiredVersion2 = "= 1.6.0";
        String[] operatorAndNumber2 =
                versionHelper.getOperatorAndNumberFromRequiredVersion(requiredVersion2);
        String openTofuPath2 =
                installer.getExecutorPathThatMatchesRequiredVersion(requiredVersion2);
        assertTrue(
                versionHelper.checkIfExecutorIsMatchedRequiredVersion(
                        new File(openTofuPath2), operatorAndNumber2[0], operatorAndNumber2[1]));

        String requiredVersion3 = ">= v1.8.0";
        String[] operatorAndNumber3 =
                versionHelper.getOperatorAndNumberFromRequiredVersion(requiredVersion3);
        String openTofuPath3 =
                installer.getExecutorPathThatMatchesRequiredVersion(requiredVersion3);
        assertTrue(
                versionHelper.checkIfExecutorIsMatchedRequiredVersion(
                        new File(openTofuPath3), operatorAndNumber3[0], operatorAndNumber3[1]));

        String requiredVersion4 = ">= 100.0.0";
        assertThrows(
                InvalidOpenTofuToolException.class,
                () -> installer.getExecutorPathThatMatchesRequiredVersion(requiredVersion4));

        String requiredVersion5 = "< v1.6.0";
        assertThrows(
                InvalidOpenTofuToolException.class,
                () -> installer.getExecutorPathThatMatchesRequiredVersion(requiredVersion5));
    }

    @BeforeAll
    static void setupWireMock() {
        wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().port(9090));
        wireMockServer.start();

        wireMockServer.stubFor(
                get(urlEqualTo("/api/versions"))
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader("Content-Type", "application/json")
                                        .withBody(
                                                "{\"versions\": [\"1.6.0\", \"1.7.0\", \"1.8.0\","
                                                        + " \"1.9.0\"]}")));
    }

    @AfterAll
    static void stopWireMock() {
        wireMockServer.stop();
    }

    @Test
    void testGetExecutableOpenTofuByVersionWithWiremock() {
        Set<String> defaultVersions = Set.of(opentofuVersions.split(","));
        Set<String> cachedVersions = versionsCache.getAvailableVersions();
        assertTrue(cachedVersions.containsAll(defaultVersions));
        assertTrue(cachedVersions.size() >= defaultVersions.size());

        String requiredVersion = "";
        String openTofuPath = installer.getExecutorPathThatMatchesRequiredVersion(requiredVersion);
        assertEquals("tofu", openTofuPath);

        String requiredVersion1 = ">= 1.7.0";
        String[] operatorAndNumber1 =
                versionHelper.getOperatorAndNumberFromRequiredVersion(requiredVersion1);
        String openTofuPath1 =
                installer.getExecutorPathThatMatchesRequiredVersion(requiredVersion1);
        assertTrue(
                versionHelper.checkIfExecutorIsMatchedRequiredVersion(
                        new File(openTofuPath1), operatorAndNumber1[0], operatorAndNumber1[1]));

        String requiredVersion2 = "= 1.6.0";
        String[] operatorAndNumber2 =
                versionHelper.getOperatorAndNumberFromRequiredVersion(requiredVersion2);
        String openTofuPath2 =
                installer.getExecutorPathThatMatchesRequiredVersion(requiredVersion2);
        assertTrue(
                versionHelper.checkIfExecutorIsMatchedRequiredVersion(
                        new File(openTofuPath2), operatorAndNumber2[0], operatorAndNumber2[1]));

        String requiredVersion3 = ">= v1.8.0";
        String[] operatorAndNumber3 =
                versionHelper.getOperatorAndNumberFromRequiredVersion(requiredVersion3);
        String openTofuPath3 =
                installer.getExecutorPathThatMatchesRequiredVersion(requiredVersion3);
        assertTrue(
                versionHelper.checkIfExecutorIsMatchedRequiredVersion(
                        new File(openTofuPath3), operatorAndNumber3[0], operatorAndNumber3[1]));

        String requiredVersion4 = ">= 100.0.0";
        assertThrows(
                InvalidOpenTofuToolException.class,
                () -> installer.getExecutorPathThatMatchesRequiredVersion(requiredVersion4));

        String requiredVersion5 = "< v1.6.0";
        assertThrows(
                InvalidOpenTofuToolException.class,
                () -> installer.getExecutorPathThatMatchesRequiredVersion(requiredVersion5));
    }
}
