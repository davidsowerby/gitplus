# GitPlus

The main purpose of this library was to support the creation of new projects both locally and at a remote service provider (for example GitHub), though no doubt it could be used for other Git related tasks.

GitPlus itself provides a local-remote pair, where local is assumed to be on your local drive and remote is, for example, GitHub. An optional local wiki instance is also supported.
 
Much of GitPlus is just wrappers and configuration around existing libraries, but aims to make the simple tasks simple, while allowing access to the deeper, more obscure features of Git if required.

# Features

- Managed creation and deletion of repositories as a pair
- Manage issue labels

## Status

- Not yet released to JCenter or Maven Central, so only available as source (release expected by Nov 16th 2016)
- The 'active' property, which should control whether a local, remote or wikiLocal instance is active is only partially implemented
- Not really usable yet, although close to it if the code is of use to you now.
- Currently only supports GitHub as the remote service.  Others, for example BitBucket, could be provided by implementing a single interface
- Passes all current tests, but integration testing could do with greater coverage

## Usage and Documentation

Guides are on [ReadTheDocs](http://gitplus.readthedocs.io/en/develop/).
  
## Languages
  
Source - mostly [Kotlin](https://kotlinlang.org/), small amount of Java
Test - Groovy (Spock)

## Contributions

Contributions via PR would be very welcome.  They can be written in Java or Kotlin, but must provide adequate tests (Spock tests are preferred, but other frameworks acceptable)
An implementation of the `GitRemote` interface for BitBucket would be particularly welcome 

## Build from source

```
./gradlew build
```

## Change log

The change log for this project is on the [wiki](https://github.com/davidsowerby/gitplus/wiki/changelog)

## Acknowledgements

[JGit](https://eclipse.org/jgit/)
[jcabi GitHub library](https://github.com/jcabi/jcabi-github)
[Guice](https://github.com/google/guice)
[Spock](http://spockframework.org/)

