# Microstream-spring

Allows integration of Microstream with Spring boot.

## How to use

The `src/it` folder contains examples of Microstream integration with Spring boot.

## General:

Microstream supports configurations using configuration files. These same configuration items are intended for use in
spring boot, but with the prefix `one.microstream`

#### Example:

`storage-filesystem.sql.postgres.user=username`<br>
the spring configuration will look like this:<br>
`one.microstream.storage-filesystem.sql.postgres.user=username`

### Important:

This framework forwards all configuration keys to the Microstream. It is important to follow the format that needs
Nicrostream framework regardless of what the Spring configuration framework allows.

### Class Loader:

Spring boot class loader. If you use another class loader, such as hot replace, you may get an exception:
`one.microstream.exceptions.TypeCastException`<br>
In this case it is possible to force the use of the CurrentThread classloader for Microstream.<br>
`one.microstream.use-current-thread-class-loader=false` <br>
This value is not passed to the Microstream Framework but is set directly in this module.

## Debug

Microstream Spring module supports standard spring logging, so you can add into your config:<br>
`logging.level.one.microstream=debug`
for to obtain all microstream configuration keys:

```
2021-08-23 15:16:02.979 DEBUG 18469 --- [           main] o.m.spring.MicrostreamConfiguration      : Microstream configuration items:
2021-08-23 15:16:02.979 DEBUG 18469 --- [           main] o.m.spring.MicrostreamConfiguration      : storage-filesystem.sql.postgres.password : xxxxx
2021-08-23 15:16:02.994 DEBUG 18469 --- [           main] o.m.spring.MicrostreamConfiguration      : storage-filesystem.sql.postgres.data-source-provider : one.microstream.test.spring.MyDataSourceProvider
2021-08-23 15:16:02.994 DEBUG 18469 --- [           main] o.m.spring.MicrostreamConfiguration      : storage-directory : microstream_storage
2021-08-23 15:16:02.994 DEBUG 18469 --- [           main] o.m.spring.MicrostreamConfiguration      : storage-filesystem.sql.postgres.user : postgres
```

Key values containing "password" are replaced by "xxxxx".

## Build

Maven build, to build just run `mvn clean install`<br>
to run integration tests run `mvn -Prun-its clean install`. Integration tests require Docker to run.

