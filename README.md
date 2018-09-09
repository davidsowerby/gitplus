# GitPlus

**This project has moved to [GitLab](https://gitlab.com/dsowerby/gitplus/blob/master/README.md)**

![License](http://img.shields.io/:license-apache-blue.svg)
[ ![Download](https://api.bintray.com/packages/dsowerby/maven/gitplus/images/download.svg) ](https://bintray.com/dsowerby/maven/gitplus/_latestVersion)


Brings together [JGit](https://eclipse.org/jgit/) and remote Git providers (for example GitHub and GitLab) and treats them as a "pair" - a local and a remote instance.

Concentrates on the more common aspects of Git, and tries to simplify the API for those actions, for example:

- create a new repo, both local and remote
- clone an existing repo
- set standard issue labels for a remote repo

# Configuration

GitPlus is extensively configurable - see the [documentation](http://gitplus.readthedocs.io/en/master/) for detail

# Acknowledgements

[jcabi-github](https://github.com/jcabi/jcabi-github) for the GitHub implementation
[JGit](https://eclipse.org/jgit/) for enabling the local Git implementation
