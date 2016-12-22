# Using GitPlus

## Preparation

Include the [GitPlus](https://github.com/davidsowerby/gitplus) library in your build: *uk.q3c.build:gitplus:version*  

Make sure you have API key(s) for your remote service provider.  See the 'Properties File' section

## Instantiation

###Guice

Add `GitPlusModule` to your injector, and inject `GitPlus` where needed 

### Other

A factory available.  Use a try-resources block:

```
try (GitPlus gitPlus = GitPlusFactory.getInstance()) {
    final GitLocal gitLocal = gitPlus.getLocal();
    final GitLocalConfiguration localConfig = gitLocal.getLocalConfiguration();
    // do stuff
}
catch (Exception e){

...

}
```

## Configuration

Most of the configuration would be done directly in code, but there are some elements for which that would not be a good idea - API Keys for example - and these are expected to be in a [properties file](build-properties.md).

The rest configuration structure matches the structure of [GitPlus](https://github.com/davidsowerby/gitplus):

- local
- remote
- wikiLocal

Note that local and remote are *active* by default, and wikiLocal is *not active* by default.  These can be changed, for example:

```
gitPlus.remote.active(false)
```

A typical scenario is given below. The [detailed configuration](gitplus-configuration.md) lists each of the options.

### Scenario - create a new project locally, with matching repo on GitHub

```
gitPlus.local.createProject(true).projectDirParent(yourDir).projectName("myProject")
gitPlus.remote.createProject(true).repoUser("yourName")
```

This provides a minimum configuration, and will create a local repo and push it to GitHub.  
Note that the remote repo name defaults to the project name, but can be configured directly

You may also want to use your own set of issue labels:

```
gitPlus.remote.createProject(true).mergeIssueLabels(true).issueLabels(myIssueMap)
```

An issue map is simply a `Map<String,String>` containing a key-value of issue label-issue colour


You may also want provide a callback to enable the creation of default project directories and files

```
gitPlus.local.projectCreator({ProjectCreator instance}) 
```

Once the configuration is set as required, simply execute it (**Note::** execute() should always be called even if `GitPlus` is only being used to read information fro local or remote Git repositories.

```
gitPlus.execute()
```
