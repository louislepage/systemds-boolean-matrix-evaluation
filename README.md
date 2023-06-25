# systemds-boolean-matrix-evaluation


## Use `systemds` jar
- the tool is dependent on all of systemds dependencies and systemds itself
- build `systemds` or aquire jar
- add it to local repo with [mvn install plugin](https://maven.apache.org/guides/mini/guide-3rd-party-jars-local.html)
```
mvn install:install-file -Dfile=./target/systemds-3.2.0-SNAPSHOT.jar
```
