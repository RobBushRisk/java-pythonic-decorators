# java-pythonic-decorators
A maven plugin which allows implementation of python-like decorators in Java

How to use:

1. Add the project as a dependency in pom.xml, add the following plugin, changing sourceDirectory as needed:
```xml
<dependency>
    <groupId>com.bob</groupId>
    <artifactId>java-pythonic-decorators</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>

...

<plugins>
    <plugin>
        <groupId>com.bob</groupId>
        <artifactId>java-pythonic-decorators</artifactId>
        <version>1.0-SNAPSHOT</version>
        <extensions>true</extensions>
        <executions>
            <execution>
                <goals>
                    <goal>decorator-compiler-preprocessor</goal>
                    <goal>decorator-compiler-postprocessor</goal>
                </goals>
            </execution>
        </executions>
        <configuration>
            <sourceDirectory>src</sourceDirectory>
            <outputDirectory>src_parsed</outputDirectory>
        </configuration>
    </plugin>
</plugins>
```
2. Add the following to .mvn/extensions.xml:
```xml

<?xml version="1.0" encoding="UTF-8"?>
<extensions>
    <extension>
        <groupId>com.bob</groupId>
        <artifactId>java-pythonic-decorators</artifactId>
        <version>1.0-SNAPSHOT</version>
    </extension>
</extensions>

```
3. Create a new @interface containing a class that extends `Decorator` and implement the `apply` method
. Here's an example:
```java

public @interface MyDecorator {
    class Impl extends Decorator {
        public void apply() {
            // Do something before the method is called
            try {
                proceed();
            } catch (Throwable t) {
                // Do something when exception is caught
                throw t;
            }
            // Do something after the method is called
        }
    }
}

```
4. Annotate a method with the new @interface:
```java

public class SomeClass{
    @MyDecorator
    public void someMethod() {
        System.out.println("Hello, world!");
    }
}

```
5. do `mvn clean compile`
6. The code will be preprocessed by the plugin (weaving the decorators into method bodies), compiled,
7. and then returned to it's original state
