# Storage Viewer HowTo:

## 1. Setup:

### Maven Dependencies:
```
<dependency>
	<groupId>one.microstream</groupId>
	<artifactId>viewer.server</artifactId>
	<version>${microstream.version}</version>
</dependency>
```

### Required imports:
``` 
one.microstream.viewer.server.StorageViewer
```

## 2. Usage:

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

### 2.3.    Stoping the viewer server
```
service.shutdown();
```
the EmbeddedStorageManager will not be stopped.

##  3.	Available Routes:

### 3.1. TypeDictionary
/[InstanceName]/ dictionary
Get the typeDictionary as String
```
http://localhost:4567/microstream/dictionary"
```

### 3.2. Object
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

### 3.3. Root 
/[InstanceName]/root
Get Name and object ID of the current storage root element
```
http://localhost:4567/microstream/root
```