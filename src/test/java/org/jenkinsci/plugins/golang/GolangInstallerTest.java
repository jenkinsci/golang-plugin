package org.jenkinsci.plugins.golang;

import org.junit.Test;

import javax.annotation.Nullable;

import static org.jenkinsci.plugins.golang.GolangInstaller.GolangInstallable;
import static org.jenkinsci.plugins.golang.GolangInstaller.GolangRelease;
import static org.jenkinsci.plugins.golang.GolangInstaller.InstallationFailedException;
import static org.junit.Assert.assertEquals;

public class GolangInstallerTest {

    // These are definitions of available Go packages, as would be listed in the Jenkins installer JSON
    private static final GolangInstallable FREEBSD_32 = createPackage("freebsd", "386");
    private static final GolangInstallable FREEBSD_64 = createPackage("freebsd", "amd64");
    private static final GolangInstallable LINUX_32 = createPackage("linux", "386");
    private static final GolangInstallable LINUX_64 = createPackage("linux", "amd64");
    private static final GolangInstallable LINUX_ARM32 = createPackage("linux", "armv6l");
    private static final GolangInstallable LINUX_ARM64 = createPackage("linux", "arm64");
    private static final GolangInstallable OS_X_10_6_32 = createPackage("darwin", "386", "10.6");
    private static final GolangInstallable OS_X_10_6_64 = createPackage("darwin", "amd64", "10.6");
    private static final GolangInstallable OS_X_10_8_32 = createPackage("darwin", "386", "10.8");
    private static final GolangInstallable OS_X_10_8_64 = createPackage("darwin", "amd64", "10.8");
    // As of Go 1.5, there is only a single OS X build distributed, with no OS X version set
    private static final GolangInstallable OS_X_GO_1_5 = createPackage("darwin", "amd64");

    @Test(expected = InstallationFailedException.class)
    public void testUnsupportedOs() throws InstallationFailedException {
        // Given we have configured a release we want to install
        GolangRelease release = createReleaseInfo();

        // When we try to get the install package for an OS which is not supported
        GolangInstaller.getInstallCandidate(release, "Android", "armv7a", null);

        // Then an exception should be thrown
    }

    @Test
    public void testFreeBsdInstallation() throws InstallationFailedException {
        // Given we have configured a release we want to install
        GolangRelease release = createReleaseInfo();

        // When we try to get the install package for FreeBSD
        GolangInstallable pkg = GolangInstaller.getInstallCandidate(release, "FreeBSD", "amd64", "10.2-RELEASE");

        // Then we should get the correct FreeBSD package
        assertEquals("Got unexpected package", FREEBSD_64, pkg);
    }

    @Test
    public void testFreeBsd32BitInstallation() throws InstallationFailedException {
        // Given we have configured a release we want to install
        GolangRelease release = createReleaseInfo();

        // When we try to get the install package for FreeBSD
        GolangInstallable pkg = GolangInstaller.getInstallCandidate(release, "FreeBSD", "i386", "10.2-RELEASE");

        // Then we should get the correct FreeBSD package
        assertEquals("Got unexpected package", FREEBSD_32, pkg);
    }

    @Test(expected = InstallationFailedException.class)
    public void testUnsupportedFreeBsd32BitInstallation() throws InstallationFailedException {
        // Given we have configured a Go 1.5 release we want to install
        GolangRelease release = createReleaseInfo(OS_X_GO_1_5, FREEBSD_64, LINUX_32, LINUX_64, LINUX_ARM64);

        // When we try to get the install package for 32-bit FreeBSD
        GolangInstallable pkg = GolangInstaller.getInstallCandidate(release, "FreeBSD", "i386", "10.2-RELEASE");

        // Then an exception should be thrown, as there is no 32-bit package
    }

    @Test
    public void testLinuxArm32BitInstallation() throws InstallationFailedException {
        // Given we have configured a release we want to install
        GolangRelease release = createReleaseInfo();

        // When we try to get the install package for Linux ARM 32-bit (aka `arm`)
        GolangInstallable pkg = GolangInstaller.getInstallCandidate(release, "linux", "arm", "5.4.0");

        // Then we should get the correct Linux ARM 32-bit package
        assertEquals("Got unexpected package", LINUX_ARM32, pkg);
    }

    @Test
    public void testLinuxArm32BitInstallationAlias() throws InstallationFailedException {
        // Given we have configured a release we want to install
        GolangRelease release = createReleaseInfo();

        // When we try to get the install package for Linux ARM 32-bit, with the CPU value `aarch32`
        GolangInstallable pkg = GolangInstaller.getInstallCandidate(release, "linux", "aarch32", "5.4.0");

        // Then we should get the correct Linux ARM 32-bit package
        assertEquals("Got unexpected package", LINUX_ARM32, pkg);
    }

    @Test
    public void testLinuxAarch64BitInstallation() throws InstallationFailedException {
        // Given we have configured a release we want to install
        GolangRelease release = createReleaseInfo();

        // When we try to get the install package for Linux ARM 64-bit (aka `aarch64`)
        GolangInstallable pkg = GolangInstaller.getInstallCandidate(release, "linux", "aarch64", "5.4.0");

        // Then we should get the correct Linux ARM 64-bit package
        assertEquals("Got unexpected package", LINUX_ARM64, pkg);
    }

    @Test
    public void testLinuxArm64BitInstallationAlias() throws InstallationFailedException {
        // Given we have configured a release we want to install
        GolangRelease release = createReleaseInfo();

        // When we try to get the install package for Linux ARM 64-bit, with the CPU value `arm64`
        GolangInstallable pkg = GolangInstaller.getInstallCandidate(release, "linux", "arm64", "5.4.0");

        // Then we should get the correct Linux ARM 64-bit package
        assertEquals("Got unexpected package", LINUX_ARM64, pkg);
    }

    @Test
    public void testLatestGo15PackageForOsXVersionReturned() throws InstallationFailedException {
        // Given we have configured a Go 1.5 release we want to install
        GolangRelease release = createReleaseInfo(LINUX_32, LINUX_64, LINUX_ARM64, OS_X_GO_1_5);

        // When we try to get the install package for an OS X version
        GolangInstallable pkg = GolangInstaller.getInstallCandidate(release, "Mac OS X", "x86_64", "10.11.12");

        // Then we should get the sole OS X package
        assertEquals("Got unexpected package", OS_X_GO_1_5, pkg);
    }

    @Test
    public void testLatestPackageForOsXVersionReturned() throws InstallationFailedException {
        // Given we have configured a release we want to install
        GolangRelease release = createReleaseInfo();

        // When we try to get the install package for a much newer OS X version
        GolangInstallable pkg = GolangInstaller.getInstallCandidate(release, "Mac OS X", "x86_64", "10.11.12");

        // Then we should get the newest package which is older than our version
        assertEquals("Got unexpected package", OS_X_10_8_64, pkg);
    }

    @Test
    public void testLatestPackageFor32BitOsXVersionReturned() throws InstallationFailedException {
        // Given we have configured a release we want to install
        GolangRelease release = createReleaseInfo();

        // When we try to get the install package for a much newer 32-bit OS X version
        GolangInstallable pkg = GolangInstaller.getInstallCandidate(release, "Mac OS X", "i386", "10.11.12");

        // Then we should get the newest package which is older than our version
        assertEquals("Got unexpected package", OS_X_10_8_32, pkg);
    }

    @Test
    public void testEarlierPackageForOsXVersionReturned() throws InstallationFailedException {
        // Given we have configured a release we want to install
        GolangRelease release = createReleaseInfo();

        // When we try to get the install package for an older, but supported OS X version
        GolangInstallable pkg = GolangInstaller.getInstallCandidate(release, "Mac OS X", "x86_64", "10.7");

        // Then we should get the newest package which is older than our version
        assertEquals("Got unexpected package", OS_X_10_6_64, pkg);
    }

    @Test
    public void testEarlierPackageFor32BitOsXVersionReturned() throws InstallationFailedException {
        // Given we have configured a release we want to install
        GolangRelease release = createReleaseInfo();

        // When we try to get the install package for an older, but supported 32-bit OS X version
        GolangInstallable pkg = GolangInstaller.getInstallCandidate(release, "Mac OS X", "i386", "10.7");

        // Then we should get the newest 32-bit package which is older than our version
        assertEquals("Got unexpected package", OS_X_10_6_32, pkg);
    }

    @Test
    public void testExactMatchPackageForOsXVersionReturned() throws InstallationFailedException {
        // Given we have configured a release we want to install
        GolangRelease release = createReleaseInfo();

        // When we try to get a install package which has an exact match on OS X version
        GolangInstallable pkg = GolangInstaller.getInstallCandidate(release, "Mac OS X", "x86_64", "10.6");

        // Then we should get the package which matches the given version exactly
        assertEquals("Got unexpected package", OS_X_10_6_64, pkg);
    }

    @Test(expected = InstallationFailedException.class)
    public void testUnsupportedOsXVersion() throws InstallationFailedException {
        // Given we have configured a release we want to install
        GolangRelease release = createReleaseInfo();

        // When we try to get a install package which has an exact match on OS X version
        GolangInstallable pkg = GolangInstaller.getInstallCandidate(release, "Mac OS X", "x86_64", "10.5");

        // Then an exception should be thrown
    }

    private static GolangRelease createReleaseInfo() {
        return createReleaseInfo(FREEBSD_32, FREEBSD_64, LINUX_32, LINUX_64, LINUX_ARM32, LINUX_ARM64,
                OS_X_10_6_32, OS_X_10_6_64, OS_X_10_8_32, OS_X_10_8_64);
    }

    private static GolangRelease createReleaseInfo(GolangInstallable... releases) {
        GolangRelease release = new GolangRelease();
        release.variants = releases;
        return release;
    }

    /**
     * @param os The OS value used in a Go archive filename.
     * @param arch The CPU architecture value used in a Go archive filename.
     */
    private static GolangInstallable createPackage(String os, String arch) {
        return createPackage(os, arch, null);
    }

    /**
     * @param os The OS value used in a Go archive filename.
     * @param arch The CPU architecture value used in a Go archive filename.
     * @param osxVersion Mac OS X version name.
     */
    private static GolangInstallable createPackage(String os, String arch, @Nullable String osxVersion) {
        GolangInstallable pkg = new GolangInstallable();
        pkg.os = os;
        pkg.arch = arch;
        pkg.osxversion = osxVersion;
        return pkg;
    }

}
