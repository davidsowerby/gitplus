# Using GitPlus

## Preparation

Include the [GitPlus](https://github.com/davidsowerby/gitplus) library in your build: *uk.q3c.build:gitplus:version*  

Make sure you have API key(s) for your remote service provider that you need.  See the [properties file](build-properties.md) section

## Sequence of use

Details of each of the following stages is given below

1. Instantiation
1. Configuration with either methods or properties
1. Invoke *gitPlus.execute()* - this validates the configuration, then does whatever it is has been configured to do.  Note that this method must ALWAYS be called before using a GitPlus instance, even if you are not creating anyhting

## Instantiation

###Guice

Add `GitPlusModule` to your injector, and inject `GitPlus` where needed 

### Other

A factory is available for use outside Guice.  Use with a try-resources block:

```
try (GitPlus gitPlus = GitPlusFactory.getInstance()) {
    final GitLocal gitLocal = gitPlus.getLocal();
    final GitLocalConfiguration localConfig = gitLocal.getConfiguration();
    // do stuff
}
catch (Exception e){

...

}
```

## Configuration

Configuration can be done directly with configuration properties, or using a number of convenience methods which just set up the configuration for you.
The latter is obviously simpler.

Some elements - API Keys for example - are expected to be in a [properties file](build-properties.md).

The configuration structure matches the structure of [GitPlus](https://github.com/davidsowerby/gitplus):

- local
- remote
- wikiLocal

Note that local and remote are *active* by default, and wikiLocal is *not active* by default.  These can be changed, for example:

```
gitPlus.remote.active(false)
```

The [detailed configuration](gitplus-configuration.md) lists each of the options.

## Typical Scenarios

In each of the scenarios listed below, there is a convenience method to set up the configuration, and then usually a suggestion of what other options are available.
If you want to set the configuration yourself, it is probably easiest to look at the code for the convenience methods as a start point

### Create a new local project, with matching repo on GitHub

Call:

```
gitPlus.createLocalAndRemote(cloneParentDir, remoteRepoUserName, projectName, includeWiki, projectCreator)

```

Note: projectCreator is optional. 

This provides a minimum configuration, and will create a local repo and push it to GitHub.  
if `projectCreator` is provided, it is invoked to build the project locally before pushing to remote.  The default simply adds a README.md to the project directory. A [related project](https://github.com/davidsowerby/projectadmin) provides at least one implementation

You may also want to use your own set of issue labels:

```
gitPlus.remote.mergeIssueLabels(true).issueLabels(myIssueMap)
```

An issue map is simply a `Map<String,String>` containing a key-value of issue label-issue colour

### Clone an existing project from GitHub

Call:

```
gitPlus.cloneFromRemote(parentDir, remoteRepoUserName, projectName, includeWiki, cloneExistsResponse) 
```



### Use just for remote
If you want to just manage issue labels, raise issues or other remote only tasks, simply configure `GitPlus` with:

```
gitPlus.remoteOnly()
```