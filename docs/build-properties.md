# Properties location
Properties are loaded by default from the gradle.properties file in the ./gradle folder of the user home directory.  This can be changed by configuring `FileBuildPropertiesLoader`, or providing a different implementation of `BuildPropertiesLoader`.

`GitPlus` requires the following properties:

>githubApiTokenRaiseIssue=an api token with rights to create an issue<br>
gitHubApiTokenCreateRepo=an api token with create repo rights<br>
gitHubApiTokenDeleteRepo=an api token with delete repo rights<br>
taggerName=That is me<br>
taggerEmail=me@example.com<br>