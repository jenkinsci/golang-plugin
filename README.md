# Go (Golang) Plugin for Jenkins

[![Jenkins plugin](https://img.shields.io/jenkins/plugin/v/golang.svg)](https://plugins.jenkins.io/golang)
[![Jenkins plugin installs](https://img.shields.io/jenkins/plugin/i/golang?color=blue)](https://plugins.jenkins.io/golang)
[![Build Status](https://github.com/jenkinsci/golang-plugin/actions/workflows/ci.yml/badge.svg)](https://github.com/jenkinsci/golang-plugin/actions/workflows/ci.yml)

Automatically downloads, installs, and sets up the [Go programming language (Golang)](https://go.dev/) tools on Jenkins agents during a build.

## Functionality

During a build, this plugin can:
- Automatically download and install a specific version of Go on the agent that the build runs on.
  - The correct package for the machine's operating system and CPU architecture will be automatically detected, downloaded, and installed if not already present.
- Export the `GOROOT` environment variable pointing to the installed Go tools.
- Add `$GOROOT/bin` to the `PATH` for the duration of the build.

### Supported Platforms & Architectures

The plugin detects agent properties and installs matching official Go distributions for:
- **Operating Systems**: Linux, macOS (Darwin), Windows, FreeBSD, OpenBSD
- **CPU Architectures**:
  - `amd64` / `x86_64` (64-bit x86)
  - `arm64` / `aarch64` (64-bit ARM, including Apple Silicon & AWS Graviton)
  - `armv6l` / `arm` (32-bit ARM)
  - `386` (32-bit x86)
  - `s390x` (IBM Z)
  - `ppc64le` (PowerPC 64-bit Little Endian)
  - `riscv64` (RISC-V 64-bit)
  - `loong64` (LoongArch 64-bit)

---

## Usage

### 1. Global Tool Configuration
Before using Go in jobs, configure one or more Go versions:
1. Navigate to **Manage Jenkins** → **Tools** (in older Jenkins versions: **Global Tool Configuration**).
2. Locate the **Go** section and click **Go Installations…** → **Add Go**.
3. Enter a recognizable name (e.g., `1.23` or `go-latest`).
4. Keep **Install automatically** checked and select the desired version from the official distribution list, or specify an alternative installer / manual directory.

### 2. Job Configuration

#### Declarative Pipeline
Use the [`tools` directive](https://www.jenkins.io/doc/book/pipeline/syntax/#tools) inside your `pipeline` or stage:

```groovy
pipeline {
    agent any

    tools {
        go '1.23'
    }

    stages {
        stage('Build') {
            steps {
                sh 'go version'
                sh 'go test ./...'
                sh 'go build -v ./...'
            }
        }
    }
}
```

#### Scripted Pipeline
Use the [`tool` step](https://www.jenkins.io/doc/pipeline/steps/workflow-basic-steps/#tool-use-a-tool-from-a-predefined-tool-installation) to resolve the installation path and set `GOROOT` and `PATH`:

```groovy
node {
    stage('Build') {
        def goRoot = tool name: '1.23', type: 'go'
        withEnv(["GOROOT=${goRoot}", "PATH+GO=${goRoot}/bin"]) {
            sh 'go version'
            sh 'go test ./...'
            sh 'go build -v ./...'
        }
    }
}
```

#### Freestyle Jobs
1. In the job configuration, navigate to **Build Environment**.
2. Select the checkbox **Set up Go programming language tools**.
3. Select your configured Go installation from the dropdown menu.

---

## Contributing & Maintenance

Contributions and improvements are welcome! Please follow the [Jenkins Contribution Guidelines](https://www.jenkins.io/participate/code/).

To build and test locally:
```bash
mvn clean verify
```

To run an interactive development Jenkins instance with the plugin installed:
```bash
mvn hpi:run
```

## Changelog
See [CHANGELOG.md](CHANGELOG.md) or GitHub Releases.
