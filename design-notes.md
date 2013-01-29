# Where my idea stemmed from #
I kept running into teams with gigantic schema folders that they were parsing with xjc. 

They were pointing the xjc task to a folder with a BUNCH of schemas under it and just generating, **EVERY** time they ran. I mean every time. The memory the build hogged up was insane.  And the amount of time it took to keep parsing this schema data even when they just threw it away on the next build was just killing me.  

They also were re generating schemas, if say, one folders schema depended on another schema in another folder -- they didn't think to only parse once and re use parsed elements.

So then I started playing around with episode binding the way they were doing it.  The problem was they were grouping the schemas into logical folders based on what they were describing, but they had many, many, *many* different target namespaces in those folder. So the original episode bindings I had were just huge globs of binding text.  I had to parse by hand and then use trial and error to see what namespace depended on what others.  

## then i had my idea ##
What if I use xml slurper to parse all these files and find the dependencies for me, create the episode files on the fly based on it's namespace and if I made a graph that is sort of like a gradle task graph but with xsd namespaces and the data they encapsulate.  

so you end up with this data structure sort of like 

```groovy
[[reference-to-namespace-data-A, reference-to-namespace-data-B], 
 [reference-to-namespace-data-C, reference-to-namespace-data-D],
 [reference-to-namespace-data-E, reference-to-namespace-data-F]]
```
each line is the `level` of the dependency graph, and then you can loop over every element and push that namespace data object to the xjc task and it has everything it needs, --- files to parse, imported namespaces to bind with episodes etc. The first level index `0` of the list, are the namespaces that are the base for this folder, that is to say, they don't import anything they need to be parsed first.  Every other namespace data in any level other than `0` depends on something before it.  It's object data will know what that is. 

For instance, `reference-to-namespace-data-C` will depend on the namespace in `reference-to-namespace-data-A`. 

And it goes recursively up the graph as well.  

`reference-to-namespace-data-E` --> `reference-to-namespace-data-C` --> `reference-to-namespace-data-A`

here ^^^ `reference-to-namespace-data-E` binds with episode file from `reference-to-namespace-data-C` and `reference-to-namespace-data-A` to only generate the files it needs to, avoiding many duplicates.  

### How I use it in gradle ###
I usually have a folder called `schema` in `rootDir` and inside schema I have folder `episodes` and `bindings`.

`episodes` is where all the generated episode files go and `bindings` is where you can place custom bindings to bind to in xjc task. 

I create a project called `some-project-schema` and this will contain the java class from the generated output from folder `schema/someProject`

so `someProject` can be whatever you want it to be, but basically the generated files are going to find all the `xsd` files in `schema/someProject` generate a graph and parse them in order, and the result is all generated classes going into `some-project-schema/src/main/java`

Take a look in the `examples` folder for further clarification. `examples/schema` has the folder.  the folder `bindings` is a folder to place all the external binding files you may or may not write to further customization the code generation from xjc

# Model design #
`NamespaceMetaData` -- holds all the data for the namespace --> files to push into xjc, imported namespace objects, externally imported namespace objects, include Files etc.

`ExternalNamespaceMetaData` -- is an extension of `NamespaceMetaData` and what I mean when I say external, is let's say that you are in a folder `/foo/A` and schema anywhere under this folder is referencing something in `/foo/B` the namespace for the `schema` in `/foo/B` is considered external because at the time, the dependency graph has no knowledge of this imported namespace

`OrderGraph`
Order graph is the main object used to gather data.  It stores all the unique `NamespaceMetaData` objects and references them to build what I call the `orderGraph`.  It also keeps track of namespaces that were put in the `orderGraph` that weren't process yet, so that you could reprocess them when you came back across them (i.e. remove them and re insert them where appropriate)
