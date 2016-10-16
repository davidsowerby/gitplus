# Introduction

Example configurations for Git related tasks and Change Log generation are given below.  

For the detail of each of the two main areas of configuration, please refer to [`GitPlusConfiguration`](gitplus-configuration.md) for Git related tasks, and [`ChangeLogConfiguration`](change-log-configuration.md) for the change log generation.

# Using GitPlus

A typical usage may look like that below, where GitPlus is constructed and then getConfiguration() called to access the configuration.  See the examples below for ways of configuring `DefaultGitPlus`

```
GitPlus gitPlus = new GitPlus()
gitPlus.getConfiguration()
    .remoteRepoFullName('davidsowerby/dummy')
    .useWiki(true)
```
When the repositories have been created / configured, the change log can be produced, using default settings or by passing configuration settings.  There are several methods for generating the changelog, this just uses defaults:


```
gitPlus.generateChangeLog()
```

While this allows a completely new configuration to be set up:

```
gitPlus.generateChangeLog(changeLogConfiguration)
```



# GitPlusConfiguration Examples

## Create a completely new project

This configuration would generate a project in a local Git repo with master and develop branches, create a new remote repository with defined issues labels, and push the local repo to remote.

```
GitPlus gitPlus = new GitPlus()
gitPlus.getConfiguration()
                .remoteRepoFullName('davidsowerby/dummy')
                .createLocalRepo(true)
                .createProject(true)
                .projectCreator(new MyProjectCreator())
                .createRemoteRepo(true)
                .publicProject(true)
                .projectDirParent(myGitDir)
                .useWiki(true)
                .mergeIssueLabels(true)
                .issueLabels(myIssueLabelsMap)

```

### Explanation

- `remoteRepoFullName` also sets the project name
- `MyProjectCreator` is your own implementation of `ProjectCreator` - contributions of standard project structures would be welcome
- `useWiki` seems obvious, but there is a [limitation](https://github.com/davidsowerby/gitplus#limitations)
- `mergeIssueLabels(true)` causes your defined set of issue labels to be applied to the new remote repository
- `projectDirParent` is the parent directory for the local project

## Clone a remote repository - pull if clone already exists

```
GitPlus gitPlus = new GitPlus()
gitPlus.getConfiguration()
        .remoteRepoFullName('davidsowerby/dummy')
        .cloneRemote(true)
        .cloneExistsResponse(PULL)
        .projectDirParent(myGitDir)
```

## Clone a remote repository - delete clone if clone already exists

There is a protection mechanism to try and avoid deleting something that should not be deleted, so before an existing clone is deleted it is checked against your implementation of `FileDeleteApprover`:

```
GitPlus gitPlus = new GitPlus()
gitPlus.getConfiguration()
                .remoteRepoFullName('davidsowerby/dummy')
                .cloneRemote(true)
                .cloneExistsResponse(DELETE)
                .fileDeleteApprover(new MyFileDeleteApprover())
                .projectDirParent(myGitDir)

```

# ChangeLogConfiguration Examples

## Change versions generated and output target
```
ChangeLogConfiguration changeLogConfiguration= new ChangeLogConfiguration()
        .fromVersion("1.3.9")
        .numberOfVersions(3)
        .outputDirectory(PROJECT_BUILD_ROOT)
        .labelGroups(myLabelGroups)

```
### Explanation

The defaults for `ChangeLogConfiguration` will work as they are, but this example:

- starts the change log at version "1.3.9" and outputs no more than 3 versions
- outputs to the project build directory (the default is the wiki root directory)
- defines your own grouping of issue labels to headings within the change log


# Project Change Log
 
The change log for this project is on the [wiki](https://github.com/davidsowerby/gitplus/wiki/changelog)