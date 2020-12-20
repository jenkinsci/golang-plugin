# Go programming language plugin for Jenkins

[![Jenkins plugin](https://img.shields.io/jenkins/plugin/v/golang.svg)](https://plugins.jenkins.io/golang)
[![Jenkins plugin installs](https://img.shields.io/jenkins/plugin/i/golang?color=blue)](https://plugins.jenkins.io/golang)
[![Build status](https://ci.jenkins.io/job/Plugins/job/golang-plugin/job/master/badge/icon)](https://ci.jenkins.io/job/Plugins/job/golang-plugin/job/master/)

Automatically installs and sets up the [Go programming language](http://golang.org/) (golang) tools on a Jenkins agent during a build.

## Functionality
During a build, this plugin can:
- Install a particular version of Go on the agent that the build is running on
  - The correct package for the machine's operating system and CPU architecture will be automatically downloaded and installed, if not already present
- Export the `GOROOT` environment variable, pointing to the installed Go tools
- Add the path `$GOROOT/bin` to the `PATH`, so that the tools are available during the build

## Usage
### Global configuration
1. In the Jenkins global tool configuration settings (Manage Jenkins → Global Tool Configuration), find the "Go" section, click "Go Installations…" and "Add Go".
2. Enter a name, e.g. "Go 1.15" — the name itself has no significance, but will be displayed to users during Freestyle job configuration, or is what you need to enter as the `name` in a Pipeline
3. Either select "Install automatically" and select the desired Go version from the drop-down list or specify the installation directory manually

### Per-job configuration
#### Freestyle
1. In a job's configuration, find the "Build environment" section
2. Select the "Set up Go programming language tools" checkbox
3. Select the name of a Go installation from the drop-down

#### Pipeline
As with any other type of Tool Installer, you can use the `tool` step, in this case with the `go` tool type.

For example, with a Scripted Pipeline:

```groovy
// Run on an agent where we want to use Go
node {
    // Ensure the desired Go version is installed
    def root = tool type: 'go', name: 'Go 1.15'

    // Export environment variables pointing to the directory where Go was installed
    withEnv(["GOROOT=${root}", "PATH+GO=${root}/bin"]) {
        sh 'go version'
    }
}
```

## Changelog
See [CHANGELOG.md](https://github.com/jenkinsci/golang-plugin/blob/master/CHANGELOG.md).
