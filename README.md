# systemds-boolean-matrix-evaluation
This is a small java program that uses systemds as a jar to evaluta the benefits of true boolean matrix operators for [SYSTEMDS-3544](https://issues.apache.org/jira/browse/SYSTEMDS-3544).
The PR regarding this feature can be found here: https://github.com/apache/systemds/pull/1849

## Use `systemds` jar
- the tool is dependent on all of systemds dependencies and systemds itself
- build `systemds` or aquire jar
- add it to local repo with [mvn install plugin](https://maven.apache.org/guides/mini/guide-3rd-party-jars-local.html)
```
mvn install:install-file -Dfile=./target/systemds-3.2.0-SNAPSHOT.jar
```
