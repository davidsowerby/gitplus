# GitPlus

This module is being developed for use with Gradle, to provide support for some common Git and Change Log generation tasks used during a Continuous Delivery process.  It could, however, be verily easily used without Gradle.

It treats a local Git clone and its related remote repository as a pair, represented by `GitLocal` and `GitRemote` respectively.
 
# Features

This module is highly configurable, but with depening on how it is configured, a single command could invoke:

- Create a local repo
- Create a project within that local repo
- Create a remote repo, with wiki enabled
- Create the wiki repo locally
- Push the local project to the remote
- Merge your standard issue labels into the new remote repository
 
This would give you a skeleton project with local and remote repos already set up, and issue labels set up to your standard configuration.

Once you start development a change log can be generated automatically.

# Components

GitPlus comprises:

## GitLocal

This is a fairly thin wrapper around [JGit](https://eclipse.org/jgit/), with some additional, build related, composite methods.  This used for both the code and wiki repos.
  
## GitRemote

This is a common interface for any hosted Git service, which is expected also to be where project issues are tracked.  At the moment there is only a [GitHub](https://github.com/) implementation for this interface.  This makes extensive use of this [GitHub API project](https://github.com/jcabi/jcabi-github)

It is hoped that there may be further implementations for BitBucket etc.

## ChangeLog

Using the two components above, ```ChangeLog``` generates a change log from Git comments.  Many elements are configurable, and a Velocity template is used to enable the user to change the layout.


# Status
The core functionality is working and tested, but not yet exercised in production.
 
# Limitations
When you create a new GitHub repository manually, and select the wiki tab, you may notice that there is no clone url displayed - until your create the first page.  This is also true when you create a repository through the API, but unfortunately there is no way to create that first wiki page via the API.  (This has been confirmed by GitHub Support)

This means that the GitPlus configuration is set to create both local and remote repositories, it will do that, but cannot "activate" the wiki - you will need to do that manually by creating a page online.

# Default Branch
This project is part of the work to move [Krail](https://github.com/davidsowerby/krail) to a Continuous Delivery build model.  

Continuous Delivery normally stipulates that every commit should be capable of being released, and committed to *master*, and then the development team should treat any build failures as highest priority.

We feel that doing that on the *master* branch of a public repository leads to an unstable master branch, which may take time to fix.  

Our process is therefore to ***commit to the develop branch***, and only merge ***releases*** into the master branch, thus keeping the *master* branch stable.  It also means that the default branch for the public repository has to be *develop*, so that PRs are raised against that, and not *master*.

# Contributions
Contributions would be extremely welcome, especially additional implementations of GitRemote (BitBucket etc).  Pull requests would be expected to provide adequate tests. 

# Build from Source

> ./gradlew build

# Acknowledgements

- [JGit](https://eclipse.org/jgit/)
- [GitHub API Library](https://github.com/jcabi/jcabi-github)
- [Spock](http://spockframework.github.io/spock/docs/1.0/index.html)
- [Change Log Generator](https://github.com/skywinder/github-changelog-generator) provided some excellent ideas, and would have been used rather than develop this ```ChangeLog```, except that we did not want to introduce a dependency on Ruby 


# Configuration

See the [wiki](https://github.com/davidsowerby/gitplus/wiki)