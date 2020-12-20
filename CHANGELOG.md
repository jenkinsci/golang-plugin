# Version history

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
