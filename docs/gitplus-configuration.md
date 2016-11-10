##Remote configuration


| name                  | purpose                                                                              | default                   |
|-----------------------|--------------------------------------------------------------------------------------|---------------------------|
| active                | if false, actions remote configuration is not executed                               | true                      |
| confirmRemoteDelete   | a verification string to confirm remote repo delete, works with repoDeleteApprover   | see repoDeleteApprover    |
| create                | if true create a remote repo                                                         | false                     |
| repoName              | repo name for remote repo - **REQUIRED**                                             | local.projectName         |
| repoUser              | user name for remote repo - **REQUIRED**                                             | "not specified"           |
| providerBaseUrl       | the base url for you remote provider. https:// is assumed                            | "github.com"              |
| issueLabels           | labels to merge into new remote repo, used with mergeIssueLabels                     | defaultIssueLabels        |
| mergeIssueLabels      | if true, merge issueLabels into this repo                                            | false                     |
| projectDescription    | a project description, for remote repo                                               | empty string              |
| projectHomePage       | a project home page, for remote repo                                                 | empty string              |
| publicProject         | if true, remote repo is public                                                       | false                     |
| repoDeleteApprover    | approves / rejects calls to delete remote repo, used with confirmRemoteDelete        | DefaultRemoteRepoDeleteApprover |
| taggerEmail           | the user email to put on a tag when tag() called                                     | load from properties      |
| taggerName            | the user name to put on a tag when tag() called                                      | load from properties      |


## Local Configuration

| name                  | purpose                                                                              | default                   |
|-----------------------|--------------------------------------------------------------------------------------|---------------------------|
| cloneExistsResponse   | provides options when clone() called, and local copy already exists                  | EXCEPTION                 |
| clone                 | if true clone remote repo to projectDir   - mutually exclusive with *create*         | false                     |
| create                | if true create a local repo in projectDir - mutually exclusive with *clone*          | false                     |
| fileDeleteApprover    | approves / rejects calls to delete a file. A bit of protection from config errors    | delete always rejected    |
| projectDirParent      | the parent directory for projectDir                                                  | current dir               |
| projectName           | name of project if createProject true.    **REQUIRED**                               | remote.repoName           |
| taggerEmail           | the user email to put on a tag when tag() called                                     | load from properties      |
| taggerName            | the user name to put on a tag when tag() called                                      | load from properties      |


## Wiki Configuration

Same options as the Local Configuration