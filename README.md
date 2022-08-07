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
- Add the path `$GOROOT/bin` as a prefix of the `PATH`, so that the tools are available during the build

## Usage
Once this plugin is installed, you must first configure which Go version(s) you need for your Jenkins jobs, and then configure any jobs that need Go as appropriate.

### Global configuration
1. In the Global Tool Configuration (Manage Jenkins → Global Tool Configuration), find the "Go" section, click "Go Installations…" and "Add Go".
2. Enter a name, e.g. "1.19" — the name itself has no significance, but it's what you'll need to provide for a Pipeline, or will be displayed to users during Freestyle job configuration
3. Either select "Install automatically" and pick the desired Go version from the drop-down list, or specify another installation method

### Per-job configuration
#### Declarative Pipeline
You can use the [`tools` directive](https://www.jenkins.io/doc/book/pipeline/syntax/#tools) within any `pipeline` or `stage`. For example:

```groovy
pipeline {
  // Run on an agent where we want to use Go
  agent any

  // Ensure the desired Go version is installed for all stages,
  // using the name defined in the Global Tool Configuration
  tools { go '1.19' }

  stages {
    stage('Build') {
      steps {
        // Output will be something like "go version go1.19 darwin/arm64"
        sh 'go version'
      }
    }
  }
}
```

#### Scripted Pipeline
You will need to grab the installation directory from the [`tool` step](https://www.jenkins.io/doc/pipeline/steps/workflow-basic-steps/#tool-use-a-tool-from-a-predefined-tool-installation), and ensure that the correct `GOROOT` and `PATH` are set. For example:

```groovy
// Run on an agent where we want to use Go
node {
    // Ensure the desired Go version is installed on this agent,
    // using the name defined in the Global Tool Configuration
    def root = tool type: 'go', name: '1.19'

    // Export environment variables to pointing the Go installation;
    // the `PATH+X` syntax prepends an item to the existing `PATH`:
    // https://jenkins.io/doc/pipeline/steps/workflow-basic-steps/#withenv-set-environment-variables
    withEnv(["GOROOT=${root}", "PATH+GO=${root}/bin"]) {
        // Output will be something like "go version go1.19 darwin/arm64"
        sh 'go version'
    }
}
```

#### Freestyle
1. In a job's configuration, find the "Build environment" section
2. Select the "Set up Go programming language tools" checkbox
3. Select the name of a Go installation from the drop-down

## Changelog
See [CHANGELOG.md](https://github.com/jenkinsci/golang-plugin/blob/master/CHANGELOG.md).
