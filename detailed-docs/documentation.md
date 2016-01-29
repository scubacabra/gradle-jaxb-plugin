Documentation
=============

This is a library for two gradle plugins that share these common
objects and interfaces:

* [gradle-wsdl-plugin](https://www.github.com/jacobono/gradle-wsdl-plugin)
* [gradle-jaxb-plugin](https://www.github.com/jacobono/gradle-jaxb-plugin)


This library provides support for slurping the dependencies
of an `xsd` or a `wsdl` file.  This library will only slurp files with
these extensions.

Slurping
--------

### Xsd Slurping
Searches the `xsd` file for its `xsd:import` and `xsd:include`
data.  Slurps the `schemaLocation` attribute for each xsd tag.

### Wsdl Slurping
Searches the `wsdl` file, getting the `wsdl:import`,
`xsd:import`, and the `xsd:include` data.  For `wsdl:import`, the attribute
to slurp is `location`, while the xsd dependencies in a wsdl document
are slurped as above.

Relative Dependencies
---------------------

The dependencies that are slurped are _relative_ (they don't have to
be, but they usually are).  That is they are
listed in the document relative to the documents current position in
the file system.  They are assumed to be relative for processing, **unexpected
results** for dependencies that are absolute.

The services `DocumentResolver` and
`AbsoulteFileResolver` handle the resolution of these relative
dependencies into their aboslute paths on the file system.

### Document Dependencies
Every `XsdDocument` and `WsdlDocument` has an instance field called
`documentDependencies` that is a map of the _relative_ paths (Strings)
as keys and the corresponding resolved files as values.
The `gradle-jaxb-plugin` needs access to the relative declarations
**AND** the resolved files, where as the `gradle-wsdl-plugin` only
needs access to the resolved files. Hence an extra variable to keep
track of what is common to both.

Guicing it up
-------------

Deciding to use Guice, and its dependency injection.  The `XsdDocument` and
`WsdlDocument` are just POGO's.  They delegate to services to do
the slurping and resolving of the documents dependencies.

Here is the class Diagram to see the dependencies visually.

[![class-diagram](./img/uml-class-diagram-small.jpg "uml class
diagram")](./img/uml-class-diagram.png)


Xsd Processing Flow
-------------------
Here is quick, high level flow of what happens when you process an
xsd.

[![class-diagram](./img/xsd-processing-small.jpg "xsd
processing")](./img/xsd-processing.png)


Wsdl Processing flow
--------------------
Here is quick, high level flow of what happens when you process a wsdl.

[![class-diagram](./img/wsdl-processing-small.jpg "wsdl
processing")](./img/wsdl-processing.png)
