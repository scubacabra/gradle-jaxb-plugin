# Plugin Objective

* I wanted to make a plugin that used the ant `xjc` task to parse xsds
  in a specific folder associated with this project. 

* I wanted the `xjc` task to be enhanced a little bit and be able to
  parse different namespaces in each folder and generate episode files
  for each namespace.
  - this way the episode files could be bound in another project
    without **REGENERATING** all of the boilerplate code (it would have already
    been done)

I had tried this without the dependency tree in previous projects, and
the episode file would be HUGE.  The only other solution was to
physically refactor the way the xsds are in the file system.  I saw
that several projects just kind of grouped xsds into folder by their
more top level function, and didn't worry about the rest.

When such a giant episode file was generated, i couldn't use it
so that a project that only needed the bindings for a particular
namespace could re-use the episode file (all bindings in an episode
file needed to be used).  I had to break it up manually. It disgusted
me.

I hope this plugin solves these problems and makes maintenance of xsd
projects easier.

# Requirements

* Find all xsd files in a particular folder defined by user

* Recursively slurp up the xsd files in user defined folder

* Create a dependency Tree that lays out namespaces in a hierarchial
  order

* Parse each namespace in a tree node with `xjc` task, generating an
  episode file for each namespace.

## Processing

1. Find all the xsd files in the *user defined folder*

2. **slurp slurp** each file and get their namespaces.

3. Group files according to their unique namespaces

4. Generate a dependency tree based on namespace dependencies

### Slurping XSD Documents

xsd dependencies are found under the root
element of the document.

```xsd
<xsd>
  <xsd:import schemaLocation="some_RELATIVE_location"/>
  <xsd:include schemaLocation="some_RELATIVE_location_2"/>
  <other stuff after>
</xsd>
```

The only important parts are the `schemaLocation`'s. :)

# Class Diagrams

Class diagrams showing their relationships.
