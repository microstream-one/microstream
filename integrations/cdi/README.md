# MicroStream CDI extension

The MicroStream CDI extension is an open-source project to integrate the Jakarta EE/MicroProfile world with the MicroStream persistence solution.

This project has two minimum requirements:

* A [CDI](https://jakarta.ee/specifications/cdi/) 2.0 implementation or higher
* An [Eclipse MicroProfile Config](https://github.com/eclipse/microprofile-config) 2.0 implementation or higher

## Dependency

To use in your project you can put it on your maven project:

```xml
<dependency>
    <groupId>one.microstream</groupId>
    <artifactId>microstream-integrations-cdi</artifactId>
    <version>${microstream.version}</version>
</dependency>
```

## Features

### StorageManager

You're enabled to inject the ```StorageManager``` easily using MicroProfile Config to read the properties.

```java
@Inject
private StorageManager manager;
```

The CDI will create an instance application-scoped, and it will close automatically.

It read all configuration keys starting with the prefix `one.microstream` and converts them to configuration keys of MicroStream (for the list, see [User guide](https://docs.microstream.one/manual/storage/configuration/properties.html)). The `-` character can be replaced by a `.` within the configuration key when it is not supported for that source.

Not only the filesystem is supported as a target but also the other targets like databases and only the dependency needs to be added to your project.



### Storage

The `@Storage` annotation defines the root object for the _StorageManager_.
Each application must have a unique class with this annotation and it is converted into a CDI bean within the Application Scope. Also (field) injection is supported within this class.

Note: To increase performance use immutable sub-graphs as often as possible.

```java
@Storage
public class NameStorage {
    //...
}
```

```java
@Inject
private NameStorage names;
```

### Store

This annotation indicates that instances that are marked as dirty will be stored by the _StorageManager_ at the end of the method (it is a CDI interceptor)

Since the rule is: "The Object that has been modified has to be stored!" the CDI integration makes it easy to indicate the modified object using a fluent API.

```java

@Inject
private DirtyMarker dirtyMarker;

@Store
public Item save(Item item) {
        dirtyMarker.mark(this.items).add(item);
        return item;
        }
```

The `mark()` method returns the instance itself but adds it to the set of changed instances. All items within this set are stored at the end of the method.

By default, this is done in an asynchronous way to speed up the user response. Also, if the marked object is a `Lazy` reference, it is cleared after it is stored.

You can alter this behaviour by using the member values of the annotation.

```java
@Store(asynchronous = false, clearLazy = false)
```

Instead of using the `@Store` annotation, you can always tune and optimize the persistence process to your needs by using the _StorageManager_ manually.
```StorageManager#store(Object)```.
[To get more information](https://docs.microstream.one/manual/storage/storing-data/index.html).

### Cache

You can use MicroStream as a cache in CDI as well, thanks to the ```@StorageCache``` annotation.

```java
@Inject
@StorageCache
private Cache<String, Integer> counter;
```

You have the option to declare more than one cache from the same configuration from the name.

```java
@Inject
@StorageCache("jcache2")
private Cache<String, Integer> counter;
```

You also have the option to inject both ```CachingProvider``` and ```CacheManager``` using CDI.

```java
@Inject
@StorageCache
private CachingProvider provider;

@Inject
@StorageCache
private CacheManager cacheManager;
```

## Eclipse MicroProfile Configuration

The integration allows receiving all information from the Eclipse MicroProfile Config instead of either a programmatic
configuration or a single file.
Thus, you can overwrite any properties following the good practices in the Market, such as [the Twelve-Factor App](https://12factor.net/).

By default, Eclipse Microprofile will read all the properties and do a parser to MicroStream, with the properties parses below. Furthermore,  you can read the properties directly in the MicroStream way.

It will use the Eclipse Microprofile to read/parse the properties.

```java
@Inject
private StorageManager manager;
```

The following snippet will look in the ```microprofile-config.properties``` file and other Config sources to the property that will be a file to load directly by MicroStream with the ``EmbeddedStorageConfiguration.load(value);`` method.

```java
@Inject
@ConfigProperty(name = "one.microstream.ini")
private StorageManager manager;
```

### Core

The relation with the properties from [Microstream docs](https://docs.microstream.one/manual/storage/configuration/properties.html):

* ```one.microstream.storage.directory```: storage-directory; The base directory of the storage in the file system. Default is "storage" in the working directory.
* ```one.microstream.storage.filesystem```: storage-filesystem; The live file system configuration. See storage targets configuration.
* ```one.microstream.deletion.directory```: deletion-directory; If configured, the storage will not delete files. Instead of deleting a file it will be moved to this directory.
* ```one.microstream.truncation.directory```: truncation-directory; If configured, files that will get truncated are copied into this directory.
* ```one.microstream.backup.directory```: backup-directory; The backup directory.
* ```one.microstream.backup.filesystem```: backup-filesystem; The backup file system configuration. See storage targets configuration.
* ```one.microstream.channel.count```: channel-count; The number of threads and number of directories used by the storage engine. Every thread has exclusive access to its directory. Default is 1.
* ```one.microstream.channel.directory.prefix```: channel-directory-prefix; Name prefix of the subdirectories used by the channel threads. Default is "channel_".
* ```one.microstream.data.file.prefix```: data-file-prefix; Name prefix of the storage files. Default is "channel_".
* ```one.microstream.data.file.suffix```: data-file-suffix; Name suffix of the storage files. Default is ".dat".
* ```one.microstream.transaction.file.prefix```: transaction-file-prefix; Name prefix of the storage transaction file. Default is "transactions_".
* ```one.microstream.transaction.file.suffix```: transaction-file-suffix; Name suffix of the storage transaction file. Default is ".sft".
* ```one.microstream.type.dictionary.file.name```: type-dictionary-file-name; The name of the dictionary file. Default is "PersistenceTypeDictionary.ptd".
* ```one.microstream.rescued.file.suffix```: rescued-file-suffix; Name suffix of the storage rescue files. Default is ".bak".
* ```one.microstream.lock.file.name```: lock-file-name; Name of the lock file. Default is "used.lock".
* ```one.microstream.housekeeping.interval```: housekeeping-interval; Interval for the housekeeping. This is work like garbage collection or cache checking. In combination with houseKeepingNanoTimeBudget the maximum processor time for housekeeping work can be set. Default is 1 second.
* ```one.microstream.housekeeping.time.budget```: housekeeping-time-budget; Number of nanoseconds used for each housekeeping cycle. Default is 10 milliseconds = 0.01 seconds.
* ```one.microstream.entity.cache.threshold```: entity-cache-threshold; Abstract threshold value for the lifetime of entities in the cache. Default is 1000000000.
* ```one.microstream.entity.cache.timeout```: entity-cache-timeout; Timeout in milliseconds for the entity cache evaluator. If an entity wasn’t accessed in this timespan it will be removed from the cache. Default is 1 day.
* ```one.microstream.data.file.minimum.size```: data-file-minimum-size; Minimum file size for a data file to avoid cleaning it up. Default is 1024^2 = 1 MiB.
* ```one.microstream.data.file.maximum.size```: data-file-maximum-size; Maximum file size for a data file to avoid cleaning it up. Default is 1024^2*8 = 8 MiB.
* ```one.microstream.data.file.minimum.use.ratio```: data-file-minimum-use-ratio; The ratio (value in ]0.0;1.0]) of non-gap data contained in a storage file to prevent the file from being dissolved. Default is 0.75 (75%).
* ```one.microstream.data.file.cleanup.head.file```: data-file-cleanup-head-file; A flag defining whether the current head file (the only file actively written to) shall be subjected to file cleanups as well.

### Cache

The relation with the properties from [Microstream docs](https://docs.microstream.one/manual/cache/configuration/properties.html):

There is a list of properties in the ```CacheProperties``` enum.

The primary purpose of this configuration is to allow you to explore the Configuration of Cache through Eclipse MicroProfile.

* ```one.microstream.cache.loader.factory```: cacheLoaderFactory - A CacheLoader should be configured for "Read Through" caches to load values when a cache miss occurs.
* ```one.microstream.cache.writer.factory```: cacheWriterFactory - A CacheWriter is used for write-through to an external resource.
* ```one.microstream.cache.expires.factory```: expiryPolicyFactory - Determines when cache entries will expire based on creation, access and modification operations.
* ```one.microstream.cache.read.through```: readThrough - When in "read-through" mode, cache misses that occur due to cache entries not existing as a result of performing a "get" will appropriately cause the configured CacheLoader to be invoked.
* ```one.microstream.cache.write.through```: writeThrough - When in "write-through" mode, cache updates that occur as a result of performing "put" operations will appropriately cause the configured CacheWriter to be invoked.
* ```one.microstream.cache.store.value```: storeByValue - When a cache is storeByValue, any mutation to the key or value does not affect the key of value stored in the cache.
* ```one.microstream.cache.statistics```: statisticsEnabled - Checks whether statistics collection is enabled in this cache.
* ```one.microstream.cache.management```: managementEnabled - Checks whether management is enabled on this cache. 

