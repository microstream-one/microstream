# Microstream CDI extension

The Microstream CDI extension is an open-source project to integrate the Jakarta EE/MicroProfile world with the Microstream persistence solution.

This project has two minimum requirements:

* A [CDI](https://jakarta.ee/specifications/cdi/) 2.0 implementation or higher
* An [Eclipse MicroProfile Config](https://github.com/eclipse/microprofile-config) 2.0 implementation or higher

## Dependency

To use in your project you can put it on your maven project:

```xml
<dependency>
    <groupId>one.microstream</groupId>
    <artifactId>microstream-integrations-cdi</artifactId>
    <version>...</version>
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

### Storage

The Storage annotation allows injecting an entity from Microstream.
Each application must have a unique class with this annotation.
Note: To increase performance use immutable sub-graphs as often as possible.

```java
@Storage
public class NameStorage {
    //...
}
```

It will create/load this annotation using CDI.

```java
@Inject
private NameStorage names;
```

### Store

This annotation indicates the operation that will be stored using Microstream automatically.
It is a high-level implementation to save either the Iterable and Map instances or the root itself, where you can set by StoreType.
By default, it is ```LAZY```, and using the ```EAGER``` only is extremely necessary.
The rule is: "The Object that has been modified has to be stored!".
So, to more tuning and optimization in the persistence process, you can always have the option to do it manually through
```StorageManager#store(Object)```. 
[To get more information](https://docs.microstream.one/manual/storage/storing-data/index.html).

```java
@Inject
private Items items;

@Override
@Store
public Item save(Item item) {
    this.items.add(item);
    return item;
}

```

Inside the lazy type, we also can filter the fields to be stored in operation. E.g., Give an Inventory instance where you have several attributes. You can only keep the products field in a method and another the list of users ass it shows the method bellow.

The lazy mode will execute: ```StorageManager#store(Object)```.
[To get more information](https://docs.microstream.one/manual/storage/storing-data/index.html).

```java
@Inject
private Inventory inventory;

@Store(fields = "products")
public void add(Product product) {
       this.inventory.add(product);
}

@Store(fields = "users")
public void add(User user) {
       this.inventory.add(user);
}

//be default lazy, and it will store all iterable and map fields
@Store
public void add(Product product, User user) {
       this.inventory.add(user);
       this.inventory.add(product);
}
//avoid this combination it has a high performance cost.
@Store(StoreType.LAZY, root = true)
public void add(String name) {
        this.inventory.setName(name)
 }
```

In another hand we have the ```EAGER``` strategy that will execute the save eagerly:
E.g.: 
```java
Storer storer = storage.createEagerStorer();
storer.store(inventory.getProducts());
storer.commit();
```

Contrary to Lazy storing this will also store modified child objects at the cost of performance.

```java
@Inject
private Inventory inventory;

@Store(value = StoreType.EAGER, fields = "products")
public void add(Product product) {
       this.inventory.add(product);
}

@Store(value = StoreType.EAGER, fields = "users")
public void add(User user) {
       this.inventory.add(user);
}

//be default, it will store all iterable and map fields
@Store(value = StoreType.EAGER)
public void add(Product product, User user) {
       this.inventory.add(user);
       this.inventory.add(product);
}

@Store(StoreType.EAGER, root = true)
public void add(String name) {
        this.inventory.setName(name)
 }
```

[To get more information](https://docs.microstream.one/manual/storage/storing-data/lazy-eager-full.html)
### Cache

You can use Microsctream as a cache as well, thanks to the ```StorageCache``` annotation.

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

By default, Eclipse Microprofile will read all the properties and do a parser to Microstream, with the properties parses below. Furthermore,  you can read the properties directly as the Microstream way.

It will use the Eclipse Microprofile to read/parse the properties.

```java
@Inject
private StorageManager manager;
```

This injection will look in the ```microprofile-config.properties``` file to the property that will be a file to load directly by Microstream with the ``EmbeddedStorageConfiguration.load(value);`` method.

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
* ```one.microstream.property```: Allow custom properties in through Microprofile, using this prefix. E.g.: If you want to include the "custom.test" property, you will set it as "one.microstream.property.custom.test"

### Cache

The relation with the properties from [Microstream docs](https://docs.microstream.one/manual/cache/configuration/properties.html):

There is a list of properties in the ```CacheProperties``` enum.

The primary purpose of this configuration is to allow you to explore the Configuration of Cache through Eclipse MicroProfile.
     
* ```microstream.cache.loader.factory```: cacheLoaderFactory - A CacheLoader should be configured for "Read Through" caches to load values when a cache miss occurs.
* ```microstream.cache.writer.factory```: cacheWriterFactory - A CacheWriter is used for write-through to an external resource.
* ```microstream.cache.expires.factory```: expiryPolicyFactory - Determines when cache entries will expire based on creation, access and modification operations.
* ```microstream.cache.read.through```: readThrough - When in "read-through" mode, cache misses that occur due to cache entries not existing as a result of performing a "get" will appropriately cause the configured CacheLoader to be invoked.
* ```microstream.cache.write.through```: writeThrough - When in "write-through" mode, cache updates that occur as a result of performing "put" operations will appropriately cause the configured CacheWriter to be invoked. 
* ```microstream.cache.store.value```: storeByValue - When a cache is storeByValue, any mutation to the key or value does not affect the key of value stored in the cache.
* ```microstream.cache.statistics```: statisticsEnabled - Checks whether statistics collection is enabled in this cache. 
* ```microstream.cache.management```: managementEnabled - Checks whether management is enabled on this cache. 

