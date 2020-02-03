# How to run

1. `./gradlew build`
1. `java -jar build/libs/chsystem-1.0-SNAPSHOT.jar`

or 

1. `./gradlew bootRun`


## DB Connection Setup IntellIJ

* Connection Type: Embedded
* Driver: H2
* Username, password and file path can be obtained from the `application.yml`
* The URL should look like this`jdbc:h2:file:~/dbs/chsystem.h2;AUTO_SERVER=TRUE`