# GitPlus

This module is being developed with the intent to use it with Gradle, to provide support for some common Git related tasks used during a Continuous Delivery process, including the production of a ChangeLog.

It is made up of 3 main parts:

## GitLocal

This is a fairly thin wrapper around [JGit](https://eclipse.org/jgit/), with some additional, build related, composite methods.
  
## GitRemote

This is a common interface for any hosted Git service, which is expected also to be where project issues are tracked.  At the moment there is only a [GitHub](https://github.com/) implementation for this interface.  This makes extensive use of this [GitHub API project](https://github.com/kohsuke/github-api)

It is hoped that there may be further implementations for BitBucket etc.

## ChangeLog

Using the two components above, ```ChangeLog``` generates a change log from Git comments, with a number of configurable options.  It also uses Velocity template so that the layout can be changed easily.


# Status
The core functionality is working and tested, but there are still some bugs to resolve.  In the hands of someone reasonably familiar with Git, it is usable. 

# Default Branch
This project is part of the work to move [Krail](https://github.com/davidsowerby/krail) to a Continuous Delivery build model.  

Continuous Delivery normally stipulates that every commit should be capable of being released, and made to *master*, and then the development team should treat any build failures as highest priority.

We feel that doing that on the *master* branch of a public repository leads to an unstable master branch, which may take time to fix.  

Our process is therefore to commit to the develop branch, and only merge ***releases*** into the master branch, thus keeping the *master* branch stable.  It also means that the default branch for the public repository has to be *develop*, so that PRs are raised against that, and not *master*.

# Contributions
Contributions would be extremely welcome, especially additional implementations of GitRemote.  PRs would be expected to provide adequate tests. 

# Build from Source

> ./gradlew build

# Acknowledgements

- [JGit](https://eclipse.org/jgit/)
- [GitHub](https://github.com/)
- [Spock](http://spockframework.github.io/spock/docs/1.0/index.html)
- [Change Log Generator](https://github.com/skywinder/github-changelog-generator) provided some excellent ideas, and would have been used rather than develop this ```ChangeLog```, except that we did not want to introduce a dependency on Ruby 


# Configuration

## GitPlus

tbd


## ChangeLog

tbd