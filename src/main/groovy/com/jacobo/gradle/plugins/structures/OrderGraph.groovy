package com.jacobo.gradle.plugins.structures

import org.gradle.api.logging.Logging
import org.gradle.api.logging.Logger

class OrderGraph { 
  static final Logger log = Logging.getLogger(OrderGraph.class)
  
  String startingDirectory
  List orderGraph = []
  List dependentNamespaces = []
  List nsCollection = []
  List externalDependencyGraphs = []

  //  def isAlreadyInList = nsCollection[0].&isAlreadyInList
  def boolean isAlreadyInList(List list, String namespace) { 
    return list.contains(namespace)
  }

  def addToDependencyLevel = { list, ns ->
    if(list) { 
      if(!isAlreadyInList(list, ns)) { 
	list << ns
      } 
    } else { 
      list = [ns]
    }
    return list
  }

  def slurpXsdFiles = { doc, namespace ->
    def schema = new XmlSlurper().parse(doc)
    def imports = schema.import
    def includes = schema.include
    slurpImports(imports, namespace, doc)
    slurpIncludes(includes, namespace, doc)
  }

  def slurpIncludes = { includes, ns, doc ->
    if(!includes.isEmpty()) { 
      includes.@schemaLocation.each {
	def includePath = new File(doc.parent, it.text()).canonicalPath
	log.debug("includes the path {}", includePath)
	def includeFile = new File(includePath)
	log.debug("include File is {}", includeFile)
	ns.addIncludes(includeFile)
      }
    }
  }

  def slurpImports = { imports, ns, doc ->
    if(!imports.isEmpty()) { 
      imports.each { imp ->
	def namespace = imp.@namespace.text()
	if(!ns.isExternalDependency(this.nsCollection, namespace)) {
	  ns.addImports(namespace)
	} else { 
	  def externalSchemaLocale = imp.@schemaLocation.text()
	  def externalSchemaPath = new File(doc.parent, externalSchemaLocale).canonicalPath
	  def externalSchemaFile = new File(externalSchemaPath)
	  ns.addExternalDependency(namespace, externalSchemaFile)
	}
      }
    }
  }

  /**
   * Gather all the nsCollection.externalDependencies maps, make them unique (could be dupes) and attach the starting files
   * to each namespace
   */
  def processExternalImports() { 
    def containsExternals = this.nsCollection.findAll { !it.externalDependencies.isEmpty() } 
    log.debug("namespaces with external dependencies are {}", containsExternals)
    def uniqueExternalNamespaces = [:]
    containsExternals.each { ns ->
      ns.externalDependencies.each { key, val ->
	if (uniqueExternalNamespaces.containsKey(key)) {
	  if (!uniqueExternalNamespaces[key].contains(val)) {
	    uniqueExternalNamespaces[key] << val
	  }
	} else {
	  uniqueExternalNamespace[key] = [val]
	}
      }
    }
    log.debug("unique external namespace is {}", uniqueExternalNamespace)
    def importedNamespaces = []
    uniqueExternalNamespaces.each { key, val ->
      def resolver = new ExternalNamespaceResolver(rootLocation : val)
      resolver.resolveExternalDependencies()
      importNamespaces << resolver.importedNamespaces
    }
    log.debug("imported Namespaces list is {}", importedNamespaces)
  }

  def populateIncludesAndImportsData() { 
    this.nsCollection.each { namespace ->
      namespace.xsdFiles.each { slurpXsdFiles(it, namespace) } //TODO might be a good currying exercise
      if(!namespace.fileImports) namespace.fileImports << "none" // if a namespace imports nothing, flag it for being parse first
    }
  }

  def performIncludesProcessing() { 
    def withIncludes = this.nsCollection.findAll { !it.fileIncludes.isEmpty() }
    log.info("Total includes over the whole Directory")
    withIncludes.each { ns ->
      log.info("namespace {}, includes {}", ns.namespace, ns.fileIncludes)
      def hasIncludeFiles = this.nsCollection.findAll { !it.xsdFiles.disjoint(ns.fileIncludes)}
      hasIncludeFiles.each { nsHasIncludes ->
	log.info("namespaces with these includes are {}, namespace xsd files are {}", nsHasIncludes.namespace, nsHasIncludes.xsdFiles)
	nsHasIncludes.xsdFiles = nsHasIncludes.xsdFiles.minus(ns.fileIncludes)
	log.info("New xsd Files for namespace {} is : {}", nsHasIncludes.namespace, nsHasIncludes.xsdFiles)
      }
    }
    //if any of the field xsdFiles are empty (couldn't be null), then get them off of the nsCollection
    def emptyData = this.nsCollection.findAll { it.xsdFiles.isEmpty() }
    if(emptyData) {
      log.warn("There is empty Namespace xsd files in {}", emptyData)
      this.nsCollection = this.nsCollection.findAll{ !it.xsdFiles.isEmpty() } 
    }
  }

  def gatherInitialNamespaceGraphOrdering() { 
      this.orderGraph[0] = this.nsCollection.findAll { it.fileImports == ["none"] }.namespace
      log.info("found the base namespace packages to be parsed first")
      log.debug("Base schema meta data information {}", this.nsCollection.findAll { it.fileImports == ["none"]})
      this.dependentNamespaces = this.nsCollection.findAll { !this.orderGraph[0].contains(it.namespace) }
      log.info("aquired the rest of the dependent namespaces to be graphed out")
  }

  def processFileNamespaceImports = { String namespace, fileCount ->
      def importMatches = [matchesIndex: [], noMatches: []]
      log.debug("import count is ${fileCount}, looking at import ${namespace}")
      for (def i = 0; i< this.orderGraph.size(); i++) { 
	if(this.orderGraph[i].contains(namespace)) { 
	  log.debug("List ${this.orderGraph[i]} contains import ${namespace}")
	  importMatches.matchesIndex << i
	}
      }
      if(importMatches.matchesIndex.isEmpty()) { 
	importMatches.noMatches << namespace
      }
      return importMatches
  }

  Map addImportNamespacesToOrderGraph(XsdNamespaces namespace, List matchingNamespaceIndexes, List notMatchingNamespaces, Map notMatchingMap) { 
    def max = matchingNamespaceIndexes.max() //get the max dependency matching depth
    log.debug "max match depth is ${max}"
    if(namespace.fileImports.size() != matchingNamespaceIndexes.size()) { // this namespace imports something not in our map yet :/ boo
      log.debug("some import namespace(s) {}  not yet in our order graph, {}", notMatchingNamespaces, this.orderGraph)
      def size = this.orderGraph.size()
      this.orderGraph[size+1] = this.addToDependencyLevel(this.orderGraph[size+1], namespace.namespace)
      this.orderGraph[size] = notMatchingNamespaces.each { 
	this.addToDependencyLevel(this.orderGraph[size], it) 
	notMatchingMap[it] = size
      }
    } else { // every import has a match and works nicely! Add to orderGraph
      if(notMatchingMap.containsKey(namespace.namespace)) { 
	log.debug ("{} is in the not matching map : {}", namespace.namespace, notMatchingMap)
	notMatchingMap = processParentNamespaceAlreadyInGraph(namespace, notMatchingMap, max)
      }
      this.orderGraph[max+1] = this.addToDependencyLevel(this.orderGraph[max+1], namespace.namespace)
    }
    log.debug("!!!! this iteration of the order graph is {}", this.orderGraph)
    return notMatchingMap
  }

  Map processParentNamespaceAlreadyInGraph(XsdNamespaces ns, Map notMatchingMap, int max) { 
    //find out if this namespace is already in the graph, just in case becuase it could be
    log.debug "OMG redone no matching, need to remove it from this.orderGraph and re insert somewhere"
    def depth = notMatchingMap[ns.namespace]
    log.debug ("depth in original is {}",depth)
    log.debug ("it is in this list {} at {}",this.orderGraph[depth], depth)
    log.debug("order @ depth {} is {}", depth, this.orderGraph[depth])
    log.debug(" range of orders from {} to {} is {}", depth, this.orderGraph.size(), this.orderGraph[depth..-1])
    log.debug("order @ max depth {} is {}", max, this.orderGraph[max])
    if(max <= depth) { // if this is true, then delete the depth portion
      log.debug("max + 1 is {} and depth is {}", max+1, depth)
      log.debug("original order Graph at {} is {}", depth, this.orderGraph[depth])
      this.orderGraph[depth] -= ns.namespace
      log.debug("modified  order Graph minus {}  at {} is {}", ns.namespace, depth, this.orderGraph[depth])
      if(this.orderGraph[depth].isEmpty()) { 
	log.debug("order Graph at {} is empty: see ? {}, removing it", depth, this.orderGraph[depth])
	this.orderGraph.remove(depth)
	log.debug("taking the indexes of notMatching Map greater than five and decrementing them because of this removal")
	notMatchingMap.each { key, val ->
	  if(val > depth) { 
	    notMatchingMap[key] -= 1
	  }
	}
      }
    }
    notMatchingMap = notMatchingMap.findAll { it.key != ns.namespace} // get rid of the namespace we just corrected
    log.debug("new notMatching map is {}", notMatchingMap)
    return notMatchingMap
  }

  def parseEachDependentNamespace() { 
    def notMatchingMap = [:]
    this.dependentNamespaces.each { ns ->
      log.debug("looking at namespace {}", ns.namespace)
      log.debug("namespace has files {}", ns.xsdFiles)
      log.debug("namespace has imports {}", ns.fileImports)
      def matchingNamespaceIndexes = []
      def notMatching = []
      def fileCount = 0
      ns.fileImports.each { 
	fileCount++
	def importMatch = processFileNamespaceImports(it, fileCount)
	matchingNamespaceIndexes.addAll(importMatch.matchesIndex)
	notMatching.addAll(importMatch.noMatches)
      }
      notMatchingMap = addImportNamespacesToOrderGraph(ns, matchingNamespaceIndexes, notMatching, notMatchingMap)
    }

    log.info("size is : ${nsCollection.size()}")

    def count = 0
    this.orderGraph.each { 
      count += it.size()
    }
    log.info("matching count is ${count}") 
    log.debug(this.toString())
  }

  def String toString() { 
    def out = "The order to parse is:"
    orderGraph.each { 
      out += it
    }
    return out
  }
}