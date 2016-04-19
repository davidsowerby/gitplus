| name                 | purpose                                                                                                       | default                     |
|----------------------|---------------------------------------------------------------------------------------------------------------|-----------------------------|
| correctTypos         | if true use the typo map to correct commit comments before extracting issue references                        | true                        |
| exclusionTagClose    | opening marker for exclusion tag                                                                              | "}}"                        |
| exclusionTagOpen     | closing marker for exclusion tag                                                                              | "{{"                        |
| exclusionTags        | when enclosed by exclusionTagOpen & Close cause git comit comment to be ignored                               | ImmutableSet.of("javadoc")  |
| fromVersion          | generate change log from LATEST_COMMIT, LATEST_VERSION or specific version                                    | LATEST_COMMIT               |
| int numberOfVersions | when >0, the number of versions to be generated, otherwise ignored                                            | 0                           |
| labelGroups          | groups issue labels for the change log                                                                        | defaultLabelGroups          |
| outputDirectory      | where the output file should go, the filename defined by outputFilename                                       | OutputTarget.WIKI_ROOT      |
| outputFile           | Full file spec for the output file.  Used only if outputDirectory is USE_FILE_SPEC, then this must be defined | null                        |
| outputFilename       | name of the output file, ignored if outputDirectory is USE_FILE_SPEC                                          | "changelog.md"              |
| pullRequestTitle     | The title in the changelog for the Pull Requests section                                                      | DEFAULT_PULL_REQUESTS_TITLE |
| separatePullRequests | if true, list the pull requests as a separate section, otherwise grouped by label along with other issues     | true                        |
| templateName         | the name of the Velocity template to use for ChangeLog layout                                                 | DEFAULT_TEMPLATE            |
| toVersion            | the last version to output.  Leave as null to output all                                                      | null                        |
| typoMap              | a simple typo replacement map                                                                                 | defaultTypoMap              |
|                      |                                                                                                               |                             |