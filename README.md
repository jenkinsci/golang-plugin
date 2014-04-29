# Go programming language plugin for Jenkins

Installs the Go programming language tools on a Jenkins build machine during a build.

For the selected Go version, the correct package for the build machine's operating system, version and CPU architecture is automatically selected.

Once installed, the `GOROOT` and `PATH` are set appropriately, so that `go` and the other tools are available during a build.

https://wiki.jenkins-ci.org/display/JENKINS/Go+Plugin
