package org.jenkinsci.plugins.golang;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.CopyOnWrite;
import hudson.EnvVars;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Computer;
import hudson.tasks.BuildWrapper;
import hudson.tasks.BuildWrapperDescriptor;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.util.Map;

public class GolangBuildWrapper extends BuildWrapper {

    private final String goVersion;

    @DataBoundConstructor
    public GolangBuildWrapper(String goVersion) {
        this.goVersion = goVersion;
    }

    @SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
    public Environment setUp(AbstractBuild build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException {
        GolangInstallation installation = getGoInstallation();
        if (installation != null) {
            EnvVars env = build.getEnvironment(listener);
            env.overrideAll(build.getBuildVariables());

            // Get the Go version for this node, installing it if necessary
            installation = installation.forNode(Computer.currentComputer().getNode(), listener).forEnvironment(env);
        }

        // Apply the GOROOT and go binaries to PATH
        final GolangInstallation install = installation;
        return new Environment() {
            @Override
            public void buildEnvVars(Map<String, String> env) {
                if (install != null) {
                    EnvVars envVars = new EnvVars();
                    install.buildEnvVars(envVars);
                    env.putAll(envVars);
                }
            }
        };
    }

    private GolangInstallation getGoInstallation() {
        for (GolangInstallation i : ((DescriptorImpl) getDescriptor()).getInstallations()) {
            if (i.getName().equals(goVersion)) {
                return i;
            }
        }
        return null;
    }

    @Extension
    public static class DescriptorImpl extends BuildWrapperDescriptor {

        @CopyOnWrite
        private volatile GolangInstallation[] installations = new GolangInstallation[0];

        public DescriptorImpl() {
            load();
        }

        @Override
        public String getDisplayName() {
            return Messages.SetUpGoTools();
        }

        @Override
        public boolean isApplicable(AbstractProject<?, ?> item) {
            return true;
        }

        @SuppressFBWarnings("EI_EXPOSE_REP")
        public GolangInstallation[] getInstallations() {
            return installations;
        }

        public void setInstallations(GolangInstallation... installations) {
            this.installations = installations;
            save();
        }

    }

}

