package org.jenkinsci.plugins.golang;

import com.google.common.annotations.VisibleForTesting;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.Extension;
import hudson.FilePath;
import hudson.model.DownloadService;
import hudson.model.Node;
import hudson.model.TaskListener;
import hudson.tools.DownloadFromUrlInstaller;
import hudson.tools.ToolInstallation;
import hudson.tools.ToolInstallerDescriptor;
import hudson.util.VersionNumber;
import jenkins.security.MasterToSlaveCallable;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Installs the Go programming language tools by downloading the archive for the detected OS/architecture combo. */
public class GolangInstaller extends DownloadFromUrlInstaller {

    @DataBoundConstructor
    public GolangInstaller(String id) {
        super(id);
    }

    // This is essentially the parent implementation, but we override it so we can pass Node into getInstallable()
    public FilePath performInstallation(ToolInstallation tool, Node node, TaskListener log) throws IOException,
            InterruptedException {
        FilePath expectedPath = preferredLocation(tool, node);

        Installable installable;
        try {
            installable = getInstallable(node);
        } catch (InstallationFailedException e) {
            throw new InstallationFailedException(Messages.CouldNotInstallGo(e.getMessage()));
        }

        if (installable == null) {
            log.getLogger().println(Messages.UnrecognisedReleaseId(id));
            return expectedPath;
        }

        if (isUpToDate(expectedPath, installable)) {
            return expectedPath;
        }

        String message = Messages.InstallingGoOnNode(installable.url, expectedPath, node.getDisplayName());
        if (expectedPath.installIfNecessaryFrom(new URL(installable.url), log, message)) {
            expectedPath.child(".timestamp").delete(); // we don't use the timestamp
            FilePath base = findPullUpDirectory(expectedPath);
            if (base != null && base != expectedPath)
                base.moveAllChildrenTo(expectedPath);
            // leave a record for the next up-to-date check
            expectedPath.child(".installedFrom").write(installable.url, "UTF-8");
        }

        return expectedPath;
    }

    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    private Installable getInstallable(Node node) throws IOException, InterruptedException {
        // Get the Go release that we want to install
        GolangRelease release = getConfiguredRelease();
        if (release == null) {
            return null;
        }

        // Gather properties for the node to install on
        String[] properties = node.getChannel().call(new GetSystemProperties("os.name", "os.arch", "os.version"));

        // Get the best matching install candidate for this node
        return getInstallCandidate(release, properties[0], properties[1], properties[2]);
    }

    @VisibleForTesting
    static GolangInstallable getInstallCandidate(GolangRelease release, String osName, String osArch, String osVersion)
            throws InstallationFailedException {
        String platform = getPlatform(osName);
        String architecture = getArchitecture(osArch);

        // Sort and search for an appropriate variant
        List<GolangInstallable> variants = Arrays.asList(release.variants);
        Collections.sort(variants);
        for (GolangInstallable i : variants) {
            if (i.os.equals(platform) && i.arch.equals(architecture)) {
                if (i.osxversion == null) {
                    return i;
                }
                if (new VersionNumber(osVersion).compareTo(new VersionNumber(i.osxversion)) >= 0) {
                    return i;
                }
            }
        }

        String osWithVersion = osVersion == null ? osName : String.format("%s %s", osName, osVersion);
        throw new InstallationFailedException(Messages.NoInstallerForOs(release.name, osWithVersion, osArch));
    }

    private GolangRelease getConfiguredRelease() {
        List<GolangRelease> releases = ((DescriptorImpl) getDescriptor()).getInstallableReleases();
        if (releases == null) {
            return null;
        }

        for (GolangRelease r : releases) {
            if (r.id.equals(id)) {
                return r;
            }
        }
        return null;
    }

    @Extension
    public static final class DescriptorImpl extends ToolInstallerDescriptor<GolangInstaller> {
        public String getDisplayName() {
            return Messages.InstallFromWebsite();
        }

        // Used by config.groovy to show a human-readable list of releases
        @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
        public List<GolangRelease> getInstallableReleases()  {
            return GolangReleaseList.all().get(GolangReleaseList.class).toList();
        }

        @Override
        public boolean isApplicable(Class<? extends ToolInstallation> toolType) {
            return toolType == GolangInstallation.class;
        }
    }

    @Extension
    public static final class GolangReleaseList extends DownloadService.Downloadable {

        // Public for JSON deserialisation
        public List<GolangRelease> releases;

        public GolangReleaseList() {
            super(GolangInstaller.class);
        }

        /** @return A list of available Go releases, obtained by parsing the JSON received from the update centre. */
        public List<GolangRelease> toList() {
            JSONObject root;
            try {
                root = getData();
                if (root == null) {
                    // JSON file has not yet been downloaded by Jenkins
                    return null;
                }
            } catch (IOException e) {
                // JSON parsing exception occurred
                return null;
            }

            Map<String, Class> classMap = new HashMap<String, Class>();
            classMap.put("releases", GolangRelease.class);
            classMap.put("variants", GolangInstallable.class);
            return ((GolangReleaseList) JSONObject.toBean(root, GolangReleaseList.class, classMap)).releases;
        }
    }

    // Needs to be public for JSON deserialisation
    public static class GolangRelease {
        public String id;
        public String name;
        public GolangInstallable[] variants;
    }

    // Needs to be public for JSON deserialisation
    public static class GolangInstallable extends Installable implements Comparable<GolangInstallable> {
        public String os;
        public String osxversion;
        public String arch;

        @SuppressFBWarnings("EQ_COMPARETO_USE_OBJECT_EQUALS")
        public int compareTo(GolangInstallable o) {
            // Sort by OS X version, descending
            if (osxversion != null && o.osxversion != null) {
                return new VersionNumber(o.osxversion).compareTo(new VersionNumber(osxversion));
            }
            // Otherwise we don't really care; sort by OS name
            return os.compareTo(o.os);
        }

        @Override
        public String toString() {
            return String.format("GolangInstallable[os=%s, arch=%s, version=%s]", os, arch, osxversion);
        }
    }

    /** @return The OS value used in a Go archive filename, for the given {@code os.name} value. */
    private static String getPlatform(String os) throws InstallationFailedException {
        String value = os.toLowerCase(Locale.ENGLISH);
        if (value.contains("freebsd")) {
            return "freebsd";
        }
        if (value.contains("linux")) {
            return "linux";
        }
        if (value.contains("os x")) {
            return "darwin";
        }
        if (value.contains("windows")) {
            return "windows";
        }
        throw new InstallationFailedException(Messages.UnsupportedOs(os));
    }

    /** @return The CPU architecture value used in a Go archive filename, for the given {@code os.arch} value. */
    private static String getArchitecture(String arch) throws InstallationFailedException {
        String value = arch.toLowerCase(Locale.ENGLISH);
        if (value.contains("amd64") || value.contains("86_64")) {
            return "amd64";
        }
        if (value.contains("86")) {
            return "386";
        }
        if (value.contains("s390x")) {
            return "s390x";
        }
        throw new InstallationFailedException(Messages.UnsupportedCpuArch(arch));
    }

    /** Returns the values of the given Java system properties. */
    private static class GetSystemProperties extends MasterToSlaveCallable<String[], InterruptedException> {
        private static final long serialVersionUID = 1L;

        private final String[] properties;

        GetSystemProperties(String... properties) {
            this.properties = properties;
        }

        public String[] call() {
            String[] values = new String[properties.length];
            for (int i = 0; i < properties.length; i++) {
                values[i] = System.getProperty(properties[i]);
            }
            return values;
        }
    }

    // Extend IOException so we can throw and stop the build if installation fails
    static class InstallationFailedException extends IOException {
        InstallationFailedException(String message) {
            super(message);
        }
    }

}
