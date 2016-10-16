# Properties location
Properties are loaded by default from the gradle.properties file in the ./gradle folder of the user home directory.  This can be changed by configuring `FileBuildPropertiesLoader`, or providing a different implementation of `BuildPropertiesLoader`.

`DefaultGitPlus` requires the following properties:

>githubApiTokenRestricted=an api token without create repo or delete repo rights<br>
gitHubApiTokenCreateRepo=an api token with create repo rights<br>
gitHubApiTokenDeleteRepo=an api token with ONLY delete repo rights<br>
taggerName=That is me<br>
taggerEmail=me@example.com<br>