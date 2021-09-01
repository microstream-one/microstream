## microstream-integrations-graalvm
This Modules provides the necessary configuration and support classes to run [Microstream](https://www.microstream.one/) storages and caches within a [GraalVM](https://www.graalvm.org/) native image.

###Prerequisites
Just add the following maven dependency to your pom.xml:

```
<dependency>
	<groupId>one.microstream</groupId>
	<artifactId>microstream-integrations-graalvm</artifactId>
	<version>21.2.0</version>
</dependency>
```

###important notes
Please be aware that native images most likely require additional, project specific configurations. <br/>
Microstream makes heavy use of reflections, therefore you need to configure the Reflection support for your classes. <br/>
Additionally, it may be necessary to configure Dynamic class loading. <br/>
Please see the [GraalVM documentation](https://www.graalvm.org/reference-manual/native-image/Limitations/) for details. 