# mapjfx

## about

Information about the project can be found at [the sothawo website](http://www.sothawo.com/projects/mapjfx/)

## building

this project is built using mvn. The library jar is created by running `mvn package`

## running the showcase

the project contains a showcase example. The source is found in the test sources. It is compiled and executed with
the following command:

`mvn test exec:java -Dexec.mainClass="com.sothawo.mapjfx.Showcase" -Dexec.classpathScope=test`
