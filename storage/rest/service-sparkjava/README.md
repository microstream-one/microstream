sparkjava# Storage Viewer HowTo:

# Content
- [Setup](#1-setup)
- [Usage](#2-usage)
- [Example](#3-example)
- [Routes](#4-available-routes)
  - [typeDictionary](#41-typedictionary)
  - [object](#42-object)
  - [root](#43-root)
  - [filesStatistics](#44-filesstatistics)
- [Configuration](#5-configuration)
  - [Server](#51-server)
    - [URL](#511-url-root)
    - [port](#512-port)
  - [Logging](#52-logging)

## 1. Setup:

### Maven Dependencies
```xml
<dependency>
	<groupId>one.microstream</groupId>
	<artifactId>microstream-storage-restservice-sparkjava</artifactId>
	<version>${microstream.version}</version>
</dependency>
```

### Required Java imports
``` 
one.microstream.storage.restservice.types.StorageRestService
```

## 2. Usage

### 2.1.	Start the Microstream storage as usual:
```java
EmbeddedStorageManager storage =  EmbeddedStorage
				.Foundation(storageDir)
				.start();
```
### 2.2.	Starting the viewer server: 
- Create a StorageRestService instance using the RestServiceResolver
- Start the server
By default  the server will listen on port 4567

```java
final StorageRestService service = RestServiceResolver.getType(storage, StorageRestServiceSparkJava.class);
service.start();
```

### 2.3    Stoping the viewer server
```java
service.stop();
```
the EmbeddedStorageManager will not be stopped.

##	3.	Example
```java
public class MainTestStorageRestService
{
     public static void main(final String[] args)
     {
         final EmbeddedStorageManager storage = EmbeddedStorage.start();
         if(storage.root() == null)
         {
              storage.setRoot(new Object[] {"A", "B", "C"});
              storage.storeRoot();
         }
         
         final StorageRestService service = RestServiceResolver.getType(storage, StorageRestServiceSparkJava.class);
         service.start();
     }         
}

```

##  4.	Available Routes

### 4.1 TypeDictionary
/[InstanceName]/ dictionary
Get the typeDictionary as String
```
http://localhost:4567/microstream/dictionary
```

### 4.2 Object
/[InstanceName]/object/:oid
Get an objects description and values by the object’s storage id

the default InstanceName is "microstream"

```
http://localhost:4567/microstream/object/1000000000000000028?valueLength=3&references=true&referenceOffset=2&referenceLength=4
```

#### Parameter:
##### Format: text
optional parameter, if not denoted json is default\
Explicitly specify the requested response content format.\
Currently only json is supported.

##### valueLength: long number
optional, default is java.lang.Long.MAX_VALUE\
limit size of returned value elements to this value
e.g. limit the size of String values

##### fixedOffset: long number
optional, default is 0 \
returned fixed sized member start at this index

##### fixedLength: long number
optional, default is java.lang.Long.MAX_VALUE \
limit the number of returned fixed sized members

##### variableOffset: long number
optional, default is 0 \
returnd variable sized members start at this index

##### variableLength: long number
optional, default is default is java.lang.Long.MAX_VALUE \
limit the number of returned variable sized members (e.g. list members)

##### references: text
optional, either “true” or “false \
if true the returned data set will contain resolved top level references of the requested object

##### referenceOffset: long number
optional, default is 0 \
if supplied together with “references=true” the returned data set will contain resolved references starting from this offset

##### referenceLength: long number
optional, default is default is java.lang.Long.MAX_VALUE \
limit the number of returned resolved references to supplied count.\
requires “references=true”

### 4.3. Root
`/[InstanceName]/root`
Get Name and object ID of the current storage root element
```
http://localhost:4567/microstream/root
```

### 4.4. FilesStatistics
`/[InstanceName]/maintenance/filesStatistics`
Get some statistics about the used storage files
```
http://localhost:4567/microstream/maintenance/filesStatistics
```

## 5. Configuration
### 5.1. Server
The Storage viewer uses the Spark micro framework from http://sparkjava.com/ as server.

To provide a custom configured server just create an Spark.service and initialize the StorageRestService with this Spark.service

```java
final Service service = Service.ignite().port(port);
final StorageRestServiceSparkJava service = RestServiceResolver.getType(storage, StorageRestServiceSparkJava.class);
service.setSparkService(sparkService);
service.start();
```

#### 5.1.1 Url Root
To set another url root then 'microstream' use the constructor:

```java
final StorageRestServiceSparkJava service = RestServiceResolver.getType(storage, StorageRestServiceSparkJava.class);
service.setInstanceName(storageName);
service.start();
```

#### 5.1.2 Port
To set another port then the default port 4567 it is required to provide a custom configured Spark Server session to the StorageRestService constructor:

```java
final Service service = Service.ignite().port(port);
final StorageRestServiceSparkJava service = RestServiceResolver.getType(storage, StorageRestServiceSparkJava.class);
service.setSparkService(sparkService);
service.start();
```

### 5.2 Logging
To enable logging add the following dependency to the project:
```xml
<dependency>
	<groupId>org.slf4j</groupId>
	<artifactId>slf4j-simple</artifactId>
	<version>1.7.28</version>
</dependency>
```

