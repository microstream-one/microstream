# MicroProfile generated Application

## Introduction

MicroProfile Starter has generated this MicroProfile application for you.

The generation of the executable jar file can be performed by issuing the following command

```shell
mvn clean package
```
This will create an executable jar file **wildfly-example-bootable.jar** within the _target_ maven folder. This can be started by executing the following command

```shell
java -jar target/wildfly-example-bootable.jar
```

To launch the test page, open your browser at the following URL

```shell
http://localhost:8080/hello  
```
To execute the tests:

```shell
curl --location --request POST 'http://localhost:8080/products/' \
--header 'Content-Type: application/json' \
--data-raw '{"id": 1, "name": "banana", "description": "a fruit", "rating": 5}'

curl --location --request POST 'http://localhost:8080/products/' \
--header 'Content-Type: application/json' \
--data-raw '{"id": 2, "name": "watermelon", "description": "watermelon sugar ahh", "rating": 4}'

curl --location --request GET 'http://localhost:8080/products/'

curl --location --request GET 'http://localhost:8080/products/1'

```

