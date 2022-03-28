# Java Project

This is a set of common configurations and libraries to start your own Java project. Focuses on working with JSON using
Jackson, but contains other helpful utilities as well.

## Prerequisites

You need at least Java 11.

This project and all its modules are using the following dependencies:

        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-api</artifactId>
            <version>${slf4j-version}</version>
        </dependency>
        <dependency>
            <groupId>org.apache.commons</groupId>
            <artifactId>commons-text</artifactId>
            <version>${commons-text-version}</version>
        </dependency>
        <dependency>
            <groupId>commons-io</groupId>
            <artifactId>commons-io</artifactId>
            <version>${commons-io-version}</version>
        </dependency>

Testing is done via JUnit5

        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter</artifactId>
            <version>${junit5-version}</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-simple</artifactId>
            <version>${slf4j-version}</version>
            <scope>test</scope>
        </dependency>

For more information, look at the [pom.xml](pom.xml).

More information available under the respective modules:

* [collections](collections/README.md)
* [common](common/README.md)
* [json](json/README.md)
    * [json-schema](json-schema/README.md)
    * [json-surfer](json-surfer/README.md)

