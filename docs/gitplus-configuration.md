| name                  | purpose                                                                              | default                   |
|-----------------------|--------------------------------------------------------------------------------------|---------------------------|
| cloneExistsResponse   | provides options when clone() called, and local copy already exists                  | EXCEPTION                 |
| cloneRemoteRepo       | if true clone remote repo to projectDir                                              | false                     |
| cloneUrl              | URL to clone from. Set automatically from remoteRepoHtmlUrl                          | null                      |
| confirmRemoteDelete   | a verification string to confirm remote repo delete, works with repoDeleteApprover   | see repoDeleteApprover    |
| createLocalRepo       | if true create a local repo                                                          | false                     |
| createProject         | true to generate a project. Requires projectCreator                                  | false                     |
| createRemoteRepo      | if true create a remote repo                                                         | false                     |
| fileDeleteApprover    | approves / rejects calls to delete a file. A bit of protection from config errors    | null                      |
| gitRemoteFactory      | creates instances of GitRemote for remoteServiceProvider                             | DefaultGitRemoteFactory   |
| issueLabels           | labels to merge into new remote repo, used with mergeIssueLabels, createRemoteRepo   | defaultIssueLabels        |
| mergeIssueLabels      | if true, merge issueLabels into this repo                                            | false                     |
| projectCreator        | creates a development project if createProject true                                  | null                      |
| projectDescription    | a project description, for remote repo                                               | empty string              |
| projectDir            | The root directory for GitLocal. Uses projectDirParent+projectName if not set        | null                      |
| projectDirParent      | the parent directory for projectDir                                                  | current dir               |
| projectHomePage       | a project home page, for remote repo                                                 | null                      |
| projectName           | name of project if createProject true. Uses remoeRepoName if not defined             | null                      |
| propertiesLoader      | Loads build properties such as API tokens                                            | FileBuildPropertiesLoader |
| publicProject         | if true, remote repo is public                                                       | false                     |
| remoteRepoHtmlUrl     | htttp url for remote repo                                                            | null                      |
| remoteRepoName        | repo name for remote repo                                                            | null                      |
| remoteRepoUser        | user name for remote repo                                                            | null                      |
| remoteServiceProvider | provider of remote repo (and issues)                                                 | GITHUB                    |
| repoDeleteApprover    | approves / rejects calls to delete remote repo, used with confirmRemoteDelete        |                           |
| taggerEmail           | the user email to put on a tag when tag() called                                     | load from properties      |
| taggerName            | the user name to put on a tag when tag() called                                      | load from properties      |
| useWiki               | passed as part of creating remote repo. May not be supported by all remote providers | true                      |
|                       |                                                                                      |                           |
|                       |                                                                                      |                           |