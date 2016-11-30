# GitPlus

This module is being developed for use with Gradle, to provide support for some common Git and Change Log generation tasks used during a Continuous Delivery process.  It could, however, be verily easily used without Gradle.

It treats a local Git clone and its related remote repository as a pair, represented by `GitLocal` and `GitRemote` respectively.  It also provides a `WikiLocal` instance, for remote providers (like GitHub) who provide a parallel wiki repository. 
 
# Features

This module is highly configurable, but depending on how it is configured, a single command could invoke:

- Create a local repo
- Invoke a project creator to populate the local repo
- Create a remote repo, with wiki enabled
- Create the wiki repo locally
- Push the local project to the remote
- Merge your standard issue labels into the new remote repository

 
This would give you a skeleton project with local and remote repos already set up, and issue labels set up to your standard configuration.


# Components

GitPlus comprises:

## GitLocal

This is a fairly thin wrapper around [JGit](https://eclipse.org/jgit/), with some additional, build related, composite methods.  This used for both the code and wiki repos.
  
## GitRemote

This is a common interface for any hosted Git service, which is uk.q3c.build.gitplus.expected also to be where project issues are tracked.  At the moment there is only a [GitHub](https://github.com/) implementation for this interface.  This makes extensive use of this [GitHub API project](https://github.com/jcabi/jcabi-github)

It is hoped that there may be further implementations for BitBucket, GitLab etc.

## WikiLocal

For GitHub at least, there is a separate repo for the wiki assoicated with the code project.  `WikiLocal` provides a representation of that


# Status
The core functionality is working and tested, but not yet exercised in production.
 
# Limitations
## GitHub wiki
When you create a new GitHub repository manually, and select the wiki tab, you may notice that there is no clone url displayed - until your create the first page.  This is also true when you create a repository through the API, but unfortunately there is no way to create that first wiki page via the API.  (This has been confirmed by GitHub Support)

This means that the GitPlus configuration is set to create both local and remote repositories, it will do that, but cannot "activate" the wiki - you will need to do that manually by creating a page online.


# Default Branch
This project is part of the work to move [Krail](https://github.com/davidsowerby/krail) to a Continuous Delivery build model.  

Continuous Delivery normally stipulates that every commit should be capable of being released, and committed to *master*, and then the development team should treat any build failures as highest priority.

We feel that doing that on the *master* branch of a public repository leads to an unstable master branch, which may take time to fix.  

Our process is therefore to ***commit to the develop branch***, and only merge ***releases*** into the master branch, thus keeping the *master* branch stable - effectively the master branch is then 'in production'.  It also means that the default branch for the public repository has to be *develop*, so that PRs are raised against that, and not *master*.

# Contributions
Contributions would be extremely welcome, especially additional implementations of GitRemote (BitBucket etc).  Pull requests would be expected to provide adequate tests. 

# Build from Source

> ./gradlew build

# Acknowledgements

- [JGit](https://eclipse.org/jgit/)
- [GitHub API Library](https://github.com/jcabi/jcabi-github)
- [Spock](http://spockframework.github.io/spock/docs/1.0/index.html)


# Configuration

See the [docs](http://gitplus.readthedocs.org/en/latest/)