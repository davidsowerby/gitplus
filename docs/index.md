# Introduction

Example configurations for Git related tasks and Change Log generation are given below.  

For the detail of each of the two main areas of configuration, please refer to [`GitPlusConfiguration`](https://github.com/davidsowerby/gitplus/wiki/GitPlusConfiguration) for Git related tasks, and [`ChangeLogConfiguration`](https://github.com/davidsowerby/gitplus/wiki/ChangeLogConfiguration) for the change log generation.

# GitPlusConfiguration Examples

## Create a completely new project

This configuration would generate a project in a local Git repo with master and develop branches, create a new remote repository with defined issues labels, and push the local repo to remote.

```
GitPlusConfiguration gitPlusConfiguration = new GitPlusConfiguration()
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
GitPlusConfiguration gitPlusConfiguration = new GitPlusConfiguration()
                .remoteRepoFullName('davidsowerby/dummy')
                .cloneRemote(true)
                .cloneExistsResponse(PULL)
                .projectDirParent(myGitDir)
```

## Clone a remote repository - delete clone if clone already exists

There is a protection mechanism to try and avoid deleting something that should not be deleted, so before an existing clone is deleted it is checked against your implementation of `FileDeleteApprover`:

```
GitPlusConfiguration gitPlusConfiguration = new GitPlusConfiguration()
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
