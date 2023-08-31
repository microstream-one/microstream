# MicroStream Spring Boot Integration

Allows integration of MicroStream with Spring Boot 3.x.

## How to use

The `src/it` folder contains examples of MicroStream integration with Spring Boot 3.

## General

MicroStream supports configurations using configuration files. These same configuration items are intended for use in
Spring Boot, but with the prefix `one.microstream`

#### Example

`storage-filesystem.sql.postgres.user=username`<br>
the spring configuration will look like this:<br>
`one.microstream.storage-filesystem.sql.postgres.user=username`

### Important

This framework forwards all configuration keys to the MicroStream. It is important to follow the format that the
MicroStream framework needs regardless of what the Spring configuration framework allows.

## Debug

MicroStream Spring module supports standard Spring logging, so you can add into your config:<br>
`logging.level.one.microstream=debug`
in order to obtain all MicroStream configuration keys:

```
2021-08-23 15:16:02.979 DEBUG 18469 --- [           main] o.m.spring.MicrostreamConfiguration      : Microstream configuration items:
2021-08-23 15:16:02.979 DEBUG 18469 --- [           main] o.m.spring.MicrostreamConfiguration      : storage-filesystem.sql.postgres.password : xxxxx
2021-08-23 15:16:02.994 DEBUG 18469 --- [           main] o.m.spring.MicrostreamConfiguration      : storage-filesystem.sql.postgres.data-source-provider : one.microstream.test.spring.MyDataSourceProvider
2021-08-23 15:16:02.994 DEBUG 18469 --- [           main] o.m.spring.MicrostreamConfiguration      : storage-directory : microstream_storage
2021-08-23 15:16:02.994 DEBUG 18469 --- [           main] o.m.spring.MicrostreamConfiguration      : storage-filesystem.sql.postgres.user : postgres
```

Key values containing "password" are replaced by "xxxxx".

## Build

Maven build, to build just run `mvn clean install` (requires JDK 17!)<br>
to run integration tests run `mvn -Prun-its clean install`. Integration tests require Docker to run.

