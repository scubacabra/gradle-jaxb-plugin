# Objective

* I wanted to make a plugin that used the ant jaxb task to parse xsds in a specific folder associated with this project.

* I wanted the jaxb task to be enhanced a little bit and be able to parse different namespaces in each folder and generate episode files.
    --> this way the episode files could be bound in another project without REGENERATING all of the boilerplate code (it's already been done)

# Steps

* Find all xsd files in a particular folder -- called the *project operating folder* which is **user generated** through extensions.

* Recursively slurp up the xsd files in the *project operating folder* **including**
   * `import` statements (one xsd imports another i.e. **differenet namespaces**)
   * `include` statments (one xsd, include another xsd's contents.  Usually happens to clearly separate files that are long but belong to the same namespace)

* Group slurped up documents by their *unique* namespaces -- use a hash map with namespaces as keys and a list of *xsd objects* as value

* Create a type of dependency graph, or dependency Tree, with the base namepsace (i.e. no other dependencies on schema files in *project operating folder*)

* Parse each namespace accordingly along with the xsd files that belong in that namespace. Starting with the top of the tree and descending children first.

# Gotchas

* Dealing with external dependencies -- what I call when an xsd file in the *project operating folder* is dependent on another file in a totally different directory that **is not** a child directory of the *project operating folder*
   * *project operating folder* --> /something/some/so/operating_folder
   * depenent file --> /something/some/just/doit/jordan.xsd
   * Problem here is that this external dependency *could* have it's own totally differnet dependency graph.

# Plugin Tasks

* ant xjc task -- suped up though, like a lowrider, a lowrider with episode file generation and binding
   * Basic flow is have some namespace data (files to parse, dependencies etc.), extract files to parse from the *project operating folder*, episode bindings if you have any (external user input), withing a *project operating folder* the plugin will autowire dependencies from the graph as episode bindings.

* Xsd Dependency Tree --> generate the xsd namespace dependency tree from the *project operating folder*
   * basically entails all that are in 'Steps' section above

# 
