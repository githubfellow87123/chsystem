# How to run

1. `./gradlew -Pprofile=dev build`
1. `java -jar -Dspring.profiles.active=dev build/libs/chsystem-1.0-SNAPSHOT.jar`

or

1. `./gradlew -Pprofile=dev bootRun`

# Docker Image

## Build

1. `./gradlew -Pprofile=prod build`
1. `docker build . -t chsystem:1.0`

## Run

`docker run -p 8080:8080 chsystem:1.0`
With prod db as bind
mount: `docker run -p 8080:8080 -v /Users/b/dbs/chsystem_prod:/dbs chsystem:1.0 --spring.profiles.active=docker`

## Start Frontend and Backend with docker-compose

Both docker images have to be build upfront. Then start both with `docker-compose up`. If you want to build the
containers upfront call `docker compose up --build`.

### Database 

#### Clear Database

```shell
docker compose down -v
```

#### Backup/Restore

##### Backup

```shell
docker exec $(docker compose ps -q postgres) pg_dump -U chsystem -d chsystem > chsystem_backup.sql
cp chsystem_backup.sql "$(date "+%Y-%m-%d")-chsystem_backup.sql"
```

##### Restore

```shell
docker exec -i $(docker compose ps -q postgres) psql -U chsystem -d chsystem < chsystem_backup.sql
```

To restore to a fresh database, first clear it:

```shell
docker compose down -v
docker compose up -d postgres
docker exec -i $(docker compose ps -q postgres) psql -U chsystem -d chsystem < chsystem_backup.sql
```


# DB Connection Setup IntellIJ

## Postgres

* Driver: PostgreSQL
* User, Password, Database: chsystem

## H2

* Connection Type: Embedded
* Driver: H2
* Username, password and file path can be obtained from the `application.yml`
* The URL should look like this`jdbc:h2:file:~/dbs/chsystem.h2;AUTO_SERVER=TRUE`
