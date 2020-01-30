# Storage Viewer HowTo:

# Content
- [Setup](#1-setup)
- [Usage](#2-usage)
- [Example](#3-example)
- [Routes](#4-available-routes)
  - [typeDictionary](#4-1-typedictionary)
  - [object](#4-2-object)
  - [root](#4-3-root)
  - [filesStatistics](#4-4-filesstatistics)
- [Configuration](#5-configuration)
  - [Server](#5-1-server)
    - [URL](#5-1-1-url-root)
    - [port](#5-1-2-port)
  - [Logging](#5-2-logging)

## 1. Setup:

### Maven Dependencies
```
<dependency>
	<groupId>one.microstream</groupId>
	<artifactId>viewer.server</artifactId>
	<version>${microstream.version}</version>
</dependency>
```

### Required imports
``` 
one.microstream.viewer.server.StorageViewer
```

## 2. Usage

### 2.1.	Start the Microstream storage as usual:
```
EmbeddedStorageManager storage =  EmbeddedStorage
				.Foundation(storageDir)
				.start();
```
### 2.2.	Starting the viewer server: 
- Create a StorageViewer instance with a running EmbeddedStorageManager
- Start the server
By default  the server will listen on port 4567

```
final StorageViewer service = new StorageViewer(storage);
service.start();
```

### 2.3    Stoping the viewer server
```
service.shutdown();
```
the EmbeddedStorageManager will not be stopped.

##	3.	Example
```
public class MainTestStorageViewer
{
     public static void main(final String[] args)
     {
         final EmbeddedStorageManager storage = EmbeddedStorage.start();
         if(storage.root() == null)
         {
              storage.setRoot(new Object[] {"A", "B", "C"});
              storage.storeRoot();
         }
         
         final StorageViewer service = new StorageViewer(storage);
         service.start();
     }         
}

```

##  4.	Available Routes

### 4.1 TypeDictionary
/[InstanceName]/ dictionary
Get the typeDictionary as String
```
http://localhost:4567/microstream/dictionary"
```

### 4.2 Object
/[InstanceName]/object/:oid
Get an objects description and values by the object’s storage id

the default InstanceName is "microstream"

```
http://localhost:4567/microstream/object/1000000000000000028?dataLength=3&dataOffset=2&references=true&referenceOffset=2&referenceLength=4
```

#### Parameter:
##### Format: text
optional parameter, if not denoted json is default\
Explicitly specify the requested response content format.\
Currently only json is supported.

##### dataOffset: long number
optional, default is 0

##### dataLength: long number
optional, default is unlimited data length\
limit the number of returned data fields and limit the size of contained value elements to this value

##### references: text
optional, either “true” or “false \
if true the returned data set will contain resolved top level references of the requested object

##### referenceOffset: long number
optional, default is 0\
if supplied together with “references=true” the returned data set will contain resolved references starting from this offset

##### referenceLength: long number
optional, default is unlimited\
limit the number of returned resolved references to supplied count.\
requires “references=true”

### 4.3. Root 
/[InstanceName]/root
Get Name and object ID of the current storage root element
```
http://localhost:4567/microstream/root
```

### 4.4. FilesStatistics
/[InstanceName]/maintenance/filesStatistics
Get some statistics about the used storage files
```
http://localhost:4567/microstream/maintenance/filesStatistics
```

## 5. Configuration
### 5.1. Server
The Storage viewer uses the Spark micro framework from http://sparkjava.com/ as server.

To provide a custom configured server just create an Spark.service and initilize the StorageViewer with this Spark.service

```
final Service service = Service.ignite().port(port);
final StorageViewer viewer = new StorageViewer(storage, service).start();
```

#### 5.1.1 Url Root
To set an other url root then 'microstream' use the constructor:

```
StorageViewer(final EmbeddedStorageManager storage, final String storageName)	
```

#### 5.1.2 Port
To set an other port then the default port 4567 it is required to provide a custom confgured Spark Server session to the StorageViewer constructor:

```
final Service service = Service.ignite().port(port);
final StorageViewer viewer = new StorageViewer(storage, service, "myText").start();
```

### 5.2 Logging
To enable logging add the following dependency to the project:
```
<dependency>
	<groupId>org.slf4j</groupId>
	<artifactId>slf4j-simple</artifactId>
	<version>1.7.28</version>
</dependency>
```

