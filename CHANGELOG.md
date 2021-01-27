# Version history

## Version 1.4
January 27, 2021

- Added support for installing Go on machines with 32-bit ARM CPU architecture

## Version 1.3
December 20, 2020

- Added support for installing Go on machines with 64-bit ARM (AArch64) CPU architecture ([#3](https://github.com/jenkinsci/golang-plugin/pull/3); thanks to [Sergei](https://github.com/serges147))
- Increased required Jenkins version to 2.190.3

## Version 1.2
February 25, 2017

- Stopped breaking tool configuration page if the version metadata had not been fetched (see [JENKINS-27499](https://issues.jenkins-ci.org/browse/JENKINS-27499))
- Added `go` symbol, for use with the Pipeline `tool` step (see [JENKINS-34345](https://issues.jenkins-ci.org/browse/JENKINS-34345))
- Increased required Jenkins version to 1.642.3

## Version 1.1
June 21, 2014

- Worked around bug causing newer versions of Go to not install (see [JENKINS-23509](https://issues.jenkins-ci.org/browse/JENKINS-23509))
- Fixed bug where wrong package for OS X could be installed (see [JENKINS-23505](https://issues.jenkins-ci.org/browse/JENKINS-23505))
- Ensured "Install automatically" is checked by default when adding a Go installation

## Version 1.0
June 18, 2014

-   Initial release
