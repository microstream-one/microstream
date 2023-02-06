# MicroStream Spring Boot Integration

Allows integration of MicroStream with Spring Boot.

## How to use

The `src/it` folder contains examples of MicroStream integration with Spring Boot.

## General

MicroStream supports configurations using configuration files. These same configuration items are intended for use in
Spring Boot, but with the prefix `one.microstream`

#### Example

`storage-filesystem.sql.postgres.user=username`<br>
the spring configuration will look like this:<br>
`one.microstream.storage-filesystem.sql.postgres.user=username`

### Multiple StorageManagers

As of version 8.0, you can define multiple StorageManagers, and make use of the `@Storage` annotation and `StorageManagerInitializer` and `EmbeddedStorageFoundationCustomizer` concepts.

Since we cannot know which qualifier (label) you want to use for your different _StorageManager_s, the Spring Boot integration cannot create the beans without a little help/configuration from the developer.

You can define the different _StorageManager_ you need and assign through a following Configuration Bean.

```
@Configuration
public class DefineStorageManagers {

    private final StorageManagerProvider provider;

    public DefineStorageManagers(StorageManagerProvider provider) {
        this.provider = provider;
    }

    @Bean
    @Qualifier("green")
    public EmbeddedStorageManager getGreenManager() {
        return provider.get(DatabaseColor.GREEN.getName());
    }
    @Bean
    @Qualifier("red")
    public EmbeddedStorageManager getRedManager() {
        return provider.get(DatabaseColor.RED.getName());
    }
}
```
The `StorageManagerProvider` is a helper bean from the Spring Boot integration that can fully initialise the _StorageManager_ and the root by providing a qualifier label.

The qualifier label is used as prefix to look for the appropriate configuration values.

```
one.microstream.red.storage-directory=red-db
one.microstream.red.channel-count=2

one.microstream.green.storage-directory=green-db
one.microstream.green.channel-count=1
```

A `StorageManagerInitializer`and `EmbeddedStorageFoundationCustomizer` implementation can check which _instance_ it received by looking at the _database name_ property which reflects the Qualifier label that you used.

```
    @Override
    public void initialize(final StorageManager storageManager) {
        if (!"red".equals(storageManager.databaseName())) {
            // This customizer operates on the Red database only
            return;
        }
        /// Perform the required initialization.
    }
```

Instead of 2 _named_ `StorageManager`s through a Qualifier, you can also use one _default_ (since we define a `@Primary` annotated _StorageManager_ within the integration) and one that you define yourself as we have done above.

In that case, the configuration keys that you need to use are `one.microstream.` and `one.microstream.<name>` and the database name for the default one is `Primary`. 

### Important

This framework forwards all configuration keys to MicroStream. It is important to follow the format that the
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

Maven build, to build just run `mvn clean install`<br>
to run integration tests run `mvn -Prun-its clean install`. Integration tests require Docker to run.

