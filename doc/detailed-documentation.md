# Objective

* I wanted to make a plugin that used the ant jaxb task to parse xsds in a specific folder associated with this project.

* I wanted the jaxb task to be enhanced a little bit and be able to parse different namespaces in each folder and generate episode files.
    --> this way the episode files could be bound in another project without REGENERATING all of the boilerplate code (it's already been done)

# Steps

* Find all xsd files in a particular folder -- called the *project operating folder* which is **user generated** through extensions.

* Recursively slurp up the xsd files in the *project operating folder* **including**
   * `import` statements (one xsd imports another i.e. **differenet namespaces**)
   * `include` statments (one xsd, include another xsd's contents.  Usually happens to clearly separate files that are long but belong to the same namespace)

* Group slurped up documents by their *unique* namespaces -- use a hash map with namespaces as keys and a list of *DocumentSlurpers* as a value

* Create a type of dependency graph, or dependency Tree, with the base namepsace (i.e. no other dependencies on schema files in *project operating folder*)

* Parse each namespace accordingly along with the xsd files that belong in that namespace. Starting with the top of the tree and descending children first.

# Gotchas

* Dealing with external dependencies -- what I call when an xsd file in the *project operating folder* is dependent on another file in a totally different directory that **is not** a child directory of the *project operating folder*
   * *project operating folder* --> /something/some/so/operating_folder
   * depenent file --> /something/some/just/doit/jordan.xsd
      * these external namepsaces are found because a namespace group depends on a namespace not found in the namespace group.  Because the processing for all files only gets the namesapces of the files in the *project operating folder*, so when you analyze the slurpedDocuments in a group for their dependencies you may find a namespace that isn't in the keys of groupedNamespaces.
   * Problem here is that this external dependency *could* have it's own totally differnet dependency graph.

# Plugin Tasks

* ant xjc task -- suped up though, like a lowrider, a lowrider with episode file generation and binding
   * Basic flow is have some namespace data (files to parse, dependencies etc.), extract files to parse from the *project operating folder*, episode bindings if you have any (external user input), withing a *project operating folder* the plugin will autowire dependencies from the graph as episode bindings.

* Xsd Dependency Tree --> generate the xsd namespace dependency tree from the *project operating folder*
   * value of *project operating folder* input by user as a String. **relative to projects root directory** -- a `/` denotes the root directory.
   * Find all xsd files
   * slurp all Xsd files
   * group slurped files by namespace
   * generate namespace dependency tree

## Xsd Dependency Tree Task

* First, you have to find all the xsd files in the *operating folder*

* Next, you need to parse the files **slurp slurp** and get their namespaces.
  These are unique namespaces for all xsd files in the *operating folder*.

* With these files slurped and namespaces in their slurped objects, you can now group unique namespaces together in a map
   keys are namespace strings and values are an array of Slurped Files (could be more than one).

These are all trivial steps.

Slurping an xsd file will yield an object of `DocumentSlurper` with `XsdSlurper` being its implementing class.  Takes care of the absolute path resolution from relative paths in the files for this documents dependencies.

### DocumentSlurper fields
**TODO put fields here**
* File name
* XmlSlurper data
*

### Now it gets interesting
Now that the files are grouped by namespace in the operating folder, it is time to parse their dependencies, and then parse those dependcies dependencies. etc.

The catch is that you don't want to be parsing the same file's over and over.  And just internally, the most dependent namespace will need to go through all the other dependencies of his parent dependency.

#### method 1
So you need to go through them all in one shot.  **BUT** there needs to be some manager structure that is keeping track of all the project dependencies.  Because once you have this structure of slurpers, you need to go back and associate them with their files.

#### Method 2
Group the map with a NamepsaceData object per entry.  Then with this array of objects, loop over them and resolve this namespace dependency.  You can keep track of previously slurped dependencies --> skipping them if you come across them and returning them so that the new ones can be added.

**going with Method 2**


### Generating a tree graph

With the grouped namespace map, you should probably encapsulate this grouped data in an object.  Something to hold the slurped Documents and the namespace, and dependencies?

**TODO flush out**
#### NamesapceData
* namespace
* slurpedDocuments
* hasDependencies
* hasExternalDependencies

### Back to generating a tree graph
Go through the map, group by namespace and make these objects.

### Finding Dependent Namespace
For every NamespaceData object from the map, loop through them and find their dependent namespaces

Keep track of the dependent files you have slurped so you don't need to do anymore of those if you come across them.

**TODO flush this out internally**

### Generate A tree
Then you just generate a tree, finding the dependent namespaces populates the hasDependencies boolean flag of the Object.  Find the one that are false, and you now have your base namespaces

all the other ones are the dependent namespaces

## TreeNode
As TreeNode has a data object, NamespaceData.  It has mutlitple parents if it wants, and multiple children. It is also separated into rows, you start at the base row and then move on until there are no more rows.  This way all the parents are parsed, and the children can just go off of them.

This is really implemented as many trees (because this is capable of having several base namespaces).

## Tree Manager
All managed nodes are kept track of by a TreeManager.  This manager keeps track of all nodes currently managed, adds a group of children NamespaceData objects, referencing already managed children if necessary. The manager can find the next available children namespaces to go in the next row by comparing a list of NamespaceData objects and checking to see if each NamespaceData object meets the following requirements:

* objects dependent Namespaces must be less than or equal to the total number of currently managedNodes.
    * If they depend on more than that, you don't know what their parents are, at least not all of them, and that defeats the tree making process
* each dependent namespace must be in the currently managed namespaces
* at least one dependent namespace must be in the namespaces of the currentRow

If all these are met *in order* then said namespace is graphable next.

The tree manager also works the other way, allowing you to traverse from the top down.  Start at the basenamespaces and retrieve the next nodes in the next row.  Basically looping over the current nodes in the current row and finding their children from the managed nodes, and placing them in a set to avoid duplication
 `nextNodeRow` -> returns null when there are no more node rows left.  Accepts a Set of current Nodes that may or may not have a next row of nodes
 **This doesn't increment the currentNodeRow pointer, just returns the next row, I don't think it should change the state of the current node row pointer**
 
Finally the tree manager allows upwards traversal to get all the parents for a particular node.  This is done recursively, and placing results in a set.  Each child node can have multiple parents, so you must go through each parent upwards as far as you can, before coming back down.
 `getParents` -> takes a TreeNode and recursively finds its parents all the way up until there are no parents.  Returns an empty set if there are no parents.  Parents are sorted from highest ancestor(grandparent, great-grandparent) to parent (immediate level up)

# Handling External Namespaces
An example of an external namespace is if the schema to be parsed are under `/schema/parse_me` and one of the schemas there imports something that is found in `/schema/no_parse_me`.  The plugin handles this by taking a note of the File where this external dependency lies and slurping it.  It stores this in the history of slurped files.  After the tree is laid out, the plugin checks if any namespaces have external dependencies (a flag is set if the above happens).  It takes all of these namespaces and calls findAllDependentNamespaces on these objects (`namespace` object).

This is not the standard findDependentsOn. It takes the external namespace File and parses it until there are no more imports.  It stores the `targetNamespace` of each slurper (keeps going as long as `imports` section is populated in the xsd) into the `namespace` object that is calling this method (as an externalNamespaceDependency). If, `targetNamespace` is null, the file name is set as the namespace

history of slurped files is used to make sure that Files of external dependency objects are not parsed more than once.

A circular dependency check is performed to make sure an external dependency doesn't depend on ANY of the schema namespaces that are in the current parse directory.  This would be bad. then the plugin would never end, I don't think at least. **TODO** **WRITE TEST**

# Parsing
Start at the base namespaces, parse each one of these, then move on to the next node row and parse these.

## For every namespaceData object you are parsing
* What files to include (method filesToParse())
* resolve dependent namespaces
* episode path -> own full episode path based on this namespaceData objects namespace
* include any user custom bindings

## Resolving Dependent Namespaces
Takes the current Node the plugin is looking to parse.

* checks if the current Node has external Dependencies
  * could have external dependencies and no parents, need these to go up first
* get parents of the node through the TreeManager
  * add parent namespaces to dependency list
  * go through each parent and if they have external dependencies add them to the list

these dependent namespaces return a set, because parent namespaces COULD depend on the same externals as the children.  It would be poorly written schema, but I've seen this in real life.

Take dependent namespaces and convert each to their episode name.

# Slurper Objects storing relative file paths (from document) and Absolute File objects
The slurper obviously stores the relative file paths in the inherited objects, but the object that is extended `DocumentSlurper` resolves those relative paths into File objects and stores them in `documentDependencies`.

This works for the wsdl plugin, because all it cares about (for now) is what to include in the WAR.  But for this jaxb plugin, it needs to know xsdImports and xsdIncludes.  So the way it works is going to have to be changed.

`documentDependencies` will store a map with the *relative_path* as the key, and the *absolute* file object as the value.  This way the `wsdl-plugin` can just use the map.values() to get the dependencies and during jaxb processing, it can use the xsdImports values as keys to get their absolute File equivalents.

## Overriding getXsdImports and getXsdIncludes
So then getting these fields from `XsdSlurper` will require the overriding of the default methods generated from a groovy bean.  The code for each override is practically the same, but is just iterating over the *current* values for either `xsdImport` or `xsdInclude` and then finding their values in the `documentDependencies` map.  The return is a Set of File objects, **the values for the fields xsdImport and xsdInclude remain the same** the overrides just return the absolute file equivalents instead of strings.

## Processing xsd files with no namespace
God, this really bothers me, and maybe it is not prevalent, but I have seen many a schema from a *nameless company* that used these types of schema patterns.

The naming convention won't work for a null namespace, so if there is no `targetNamespace` value, it takes the filename as the namespace.

xjc task folder checks
======================

There are a couple of things that need to be present for the XJC task to succeed, and not barf.

* There needs to be *at least* 1 file name to pass in to the xjc param `includes`

	```groovy
	schema (dir : schemasDirectory, includes : xsdFiles )
	```

	Here, `xsdFiles` is a string of space separated file names that fall under
	the `schemasDirectory`.  So if that string is empty, then the ant xjc task
	is going to fail.  This can be checked and an error thrown in the
	`NamespaceData` object that gets the list of files.

* Next certain directories need to exist:

  * `jaxbSchemaDestinationDirectory`[^1]
	This is the directory where the generated java files will go.  usually
	`<project-folder>/src/main/java`. 

  * `jaxbEpisodeDirectory`[^2]
	This is the directory where to find **and** put episode files.  This
	directory needs to exist even if not depending on episode files
	because the plugin generates episode files for each namespace

  * `jaxbBindingDirectory`[^3]
    This is the directory where custom bindings for jaxb sjc task may be placed.
	It doesn't necessarily have any bindings, but should still have the directory
	anyway.
	
  * episode binding files must be on the file system, inside the`jaxbEpisodeDirectory` directory
	Only if the namesapce is depending on another namespace and using episode files.

If these don't exist when they need to, then the xjc task will just barf.  So it might be nice to
provide the user with a little exception to let them know what is going on without having to look
through debug or info output.

Gradle uses the annotation `OutputDirectory` for its incremental build support, and might be nice
for the directories that need to be instantiated, as it just creates the directories if they don't
exist. :) oh happy day.

With this, then, `jaxbSchemaDestinationDirectory`, `jaxbEpisodeDirectory`, `jaxbBindingDirectory`
can be passed in from the `JaxbPlugin` config class and annoted with `OutputDirectory` and we don't
have to worry about checking for these directories existing.

Episode files I am guessing should still be checked, even if there are many -- better to see an error here than sift through info or debug output on console

integration testing
===================
**TODO**
It would be a good idea to put some empty xsd files with different namespaces that each depend on each other in a specific hierarchy.

The only problem is that to run the full Task methods, the `ant.*` is called twice.  Once for `taskdef` and once for `xjc`.  I tried to mock the `org.gradle.api.AntBuilder` but it is an abstract class and can't mock it. If I could somehow mock it, I could test that it gets called a certain number of times and the code is run all the way through so I know it probably won't barf when you try and run it.

One such integration test is written, but it must be stashed, because I run it by commenting out the xjc code so that I can test **most** of the top to bottom calls.  Would really like to mock AntBuilder though.  **TODO**

Task Depends On Another Task
============================
**TODO**
The same way that one would write a compilation dependency project to project,
i.e.

```groovy
dependencies {
    compile 'project(:projectA)'
}
```

I would like to do but between the tree generation tasks.  In the simple case of two projects.  *internal* and *external*:

Basically, the schema of *internal* depends on the schema of *external* to be parsed first, and that schema is not in the current folders of the `jaxbSchemaDirectory` for *internal*, hence it is an **external** namespace dependency, then this _external_ namespace should be parsed first.  This *external* namespace should also be compiled first and its jar artifact used for the compilation of the *internal* project.

I would like to specify all of the above ala

```groovy
dependencies {
  jaxb 'project(:external)'
}
```

technically, I can do this right now, but there is nothing that happens. I need some code in the `JaxbPlugin` code that handles this.  But I am not sure how to get to this.

Testing in a project before deploying to artifactory server
===========================================================

After running integration tests AND unit tests, one further test should be run in the `examples` folder.  Build the plugin jar and reference it in the `examples` folder.

Extensions and default configurations
=====================================

**TODO** *Flush out*

[^1]: required

[^2]: required

[^3]: only required if there are custom jaxb bindings that are being used
