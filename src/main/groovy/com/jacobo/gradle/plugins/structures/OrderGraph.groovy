package com.jacobo.gradle.plugins.structures

import org.gradle.api.logging.Logging
import org.gradle.api.logging.Logger
import com.jacobo.gradle.plugins.util.ListUtil

import groovy.io.FileType

class OrderGraph { 
  static final Logger log = Logging.getLogger(OrderGraph.class)

  /**
   * the directory we are starting our analysis on
   */
  String startingDirectory

  /**
   * a list of lists that hold references to #NamespaceMetaData objects
   */
  List orderGraph = []

  /**
   * Namespace meta data list for use in the xjc task
   */
  List<NamespaceMetaData> namespaceData = []
  
  /**
   * this is an intermediary to hold all external namespace data with the same namespace so you don't have to resolve the same namespace many many times
   */
  def private externalDependencies = []

  /**
   * this is a private map that contains a namespace string for a key and the value is the max depth level in the order graph it was originally placed in because it was listed as a dependency but it was nowhere to be found in the dependency graph so these need special processing when you finally get to them
   */
  def private notMatchingMap = [:]


  /**
   * @param namesapce the namespace to be added to the order graph
   * @param level the level in the order graph to add this namespace
   * Adds a namespace to the level specified in the order graph
   */
  def addNamespaceToLevel(NamespaceMetaData namespace, int level) {
    log.info("adding namespace {} to level {}", namespace, level)
    if(orderGraph[level]) { // this level exists, don't have to create it
      if(!ListUtil.isAlreadyInList(orderGraph[level], namespace)) { // isn't at this level
	orderGraph[level] << namespace
      } 
    } else { // level doesn't exist yet, make it
      orderGraph[level] = [namespace]
    }
  }

  /**
   * @param namesapce the namespace to be added to the order graph
   * @param level the level in the order graph to add this namespace
   * reinsert a namespace to the specifiec level only when it has first been reomved
   */
  def reinsertNamespaceIntoLevel(NamespaceMetaData namespace, int level) {
    addNamespaceToLevel(namespace, level)
    def anythingDependsOnMe = findNamespacesThatDependOnMe(namespace)
    log.info("what depends on namespace {} : {}", namespace.namespace, anythingDependsOnMe)
    if(anythingDependsOnMe) { //something depends on me
      anythingDependsOnMe.each { nsKey, levelDepth ->
	log.debug("namespace key is {} and the level depth where it currently lies is {}", nsKey, levelDepth)
	def dependentNamespace = namespaceData.find { it.namespace == nsKey }
	log.debug("dependent namespace is {}, it has files {}", dependentNamespace, dependentNamespace.parseFiles )
	removeNamespaceFromOrderGraph(dependentNamespace, levelDepth)
	log.debug("trying to insert {} at level {}", dependentNamespace, level+1)
	reinsertNamespaceIntoLevel(dependentNamespace, level+1)
      }
    }
  }

  /**
   * @param namesapce the namespace to be checked if others depend on 
   * @return dependsOnMe is a map with namespaces as string for the key and values that is the level they are currently at or null if nothing is found
   * finds all namespace objects that depend on namespace
   */
  def findNamespacesThatDependOnMe(NamespaceMetaData namespace) {
    log.info("looking for namespaces that import {}", namespace.namespace)
    def dependsOnMe = [:]
    orderGraph.eachWithIndex { listAtLevel, level ->
      log.debug("list {} at level {}", listAtLevel, level)
      def dependents = listAtLevel.find { it.importedNamespaces.contains(namespace)}
      log.debug("dependent namespaces found is {}", dependents)
      if(dependents) { //if you find something you can then populate the map and return it
	dependents.each { 
	  dependsOnMe[dependents.namespace] = level
	}
	log.debug("depends Map is {}", dependsOnMe)
      } else {
	log.debug("no namespace in the graph depend on {}", namespace.namespace)
      }
    }
    log.debug("returning map {}", dependsOnMe)
    return (dependsOnMe.isEmpty()) ? null : dependsOnMe
    
  }
  
  /**
   * @param namespace the namespace string to be removed from the map
   * @param depth the depth level this namespace didn't match at, all other values in this map greater then depth should be decremented because of the removal
   * Removes the not Matching entry namespace and decrements all other values by one if they have a value greater then provided
   **/
  def removeNotMatchingEntry(String namespace, int depth) { 
    log.debug("going to remove this namespace {} from the not matching map", namespace)
    notMatchingMap = notMatchingMap.findAll { it.key != namespace} // get rid of the namespace

    log.debug("taking the indexes of notMatching Map greater than depth {}  and decrementing them because of this removal", depth)
    notMatchingMap.each { key, val ->
      if(val > depth) { 
	notMatchingMap[key] -= 1
      }
    }

    log.debug("new notMatching map is {}", notMatchingMap)

  }
  
  /**
   * @param namespace the namespace to be removed
   * @param depth the depth level in the order graph list to remove the namespace
   * Removes a namespace from the order graph at a certain level
   **/
  def removeNamespaceFromOrderGraph(NamespaceMetaData namespace, int depth) { 
    orderGraph[depth] -= namespace
    log.debug("modified  order Graph minus {}  at {} is {}", namespace, depth, orderGraph[depth])
    if (orderGraph[depth].isEmpty()) {
      log.debug("order Graph at level {} is empty: see?? {}, removing it", depth, orderGraph[depth])
      orderGraph.remove(depth) // could probably use a find closure to be nicer TODO
    }
  }

  /**
   * Gather all the namespaceData.externalImportedNamespaces maps, make them unique (could be dupes) and attach the starting files
   * to each namespace
   */
  def processExternalImports() { 
    def containsExternals = this.namespaceData.findAll { !it.externalImportedNamespaces.isEmpty() } 
    log.debug("namespaces with external dependencies are {}", containsExternals)

    //populate this top level external dependencies, so you avoid ton's of processing if many target Namespace Data objects, import the same external namespace
    containsExternals.each { ns ->
      ns.externalImportedNamespaces.each { ext ->
	def extNamespace = externalDependencies.findAll { it.namespace == ext.namespace}
	if (extNamespace) { // has this namespace already
	  log.info("target Namespace {} has an external dependency on {} at file {} and this is already on the resolve list {}", ns.namespace, ext.namespace, ext.externalSchemaLocation, externalDependencies)
	} else { // new addition
	  externalDependencies << ext
	}
      }
    }

    log.debug("external dependencies is {} it has resolved imports at externalImportedNamespaces {}", externalDependencies, externalDependencies*.externalImportedNamespaces)

    // resolve these external namespaces, that is, go through and slurp their files with the External Namespace Resolver
    externalDependencies.each { external ->
      def resolver = new ExternalNamespaceResolver(external)
      def importedExternals = resolver.resolveExternalImportedNamespaces()
      log.info("resolved the external namespace {} and got it's list of namespace it imports {}", external.namespace, external.externalImportedNamespaces)
      log.debug("resolver external imported namespaces is {}", resolver.externalImportedNamespaces)
      log.debug("this external dependy list for ORder Graph is {}", importedExternals)
      external.externalImportedNamespaces = importedExternals
      external.importedNamespaces = importedExternals
      log.debug("external namespace {} external imports {} and importedNs {} and externalDependencies has external imports {}", external.namespace, external.externalImportedNamespaces, external.importedNamespaces, externalDependencies.externalImportedNamespaces)
    }

    // I think that here, we should re populate the objects in containsExternals with their full resolved Meta Data from externalDependencies
    containsExternals.each { ns ->
      ns.externalImportedNamespaces.each { ext ->
    	def resolvedExternal = externalDependencies.find { it.namespace == ext.namespace }
    	if(resolvedExternal) { 
    	  log.info("namespace {} with external namespace {} is being replaced with the same object, just populated instead of sparse", ns.namespace, ext.namespace)
    	  ext = resolvedExternal //make it the resolved
    	}
      }
    }

  }

  /**
   * populates the includes and imports data
   * if a namespace does nothing, it is flagged for being parsed first in the Namespace Graph Order
   */
  def populateIncludesAndImportsData() { 
    this.namespaceData.each { namespace ->
      namespace.slurpXsdFiles(namespaceData)
    }
  }

  /**
   * finds all nameaspace meta data with includes files, and calls that objects method to process those files
   */
  def processIncludes() { 
    def withIncludes = this.namespaceData.findAll { !it.includeFiles.isEmpty() }
    log.info("processing Total includes over the whole Directory")
    withIncludes.each { ns ->
      ns.processIncludeFiles()
    }
  }

  /**
   * gathers initial namespace graph ordering data objects that are flagged with a "none" for their #importedNamespaces field
   * populates with a reference from namespaceData
   */
  def gatherInitialNamespaceGraphOrdering() { 
    log.info("finding the base namespace packages to be parsed first, i.e. those namespace that do not import any other namespaces")
    orderGraph[0] = this.namespaceData.findAll { it.importedNamespaces == ["none"] }
    log.debug("Base schema meta data information {}", orderGraph[0])
  }


  /**
   * @param namespace the namespace to add in to the #OrderGraph
   * @param matchingImportLevelsIndexes the indexes into the orderGraph 'level' that match to this #namespace.importedNamespaces
   * @param importedNamespaceNotMatching List of #NamespaceMetaData objects that weren't found in the current order Graph that need to be specially added to the graph
   *
   */
  def addNamespaceToOrderGraph(NamespaceMetaData namespace, List matchingImportLevelsIndexes, List importedNamespaceNotMatching) { 
    int max = matchingImportLevelsIndexes.max() //max depth of the `level` the import matched
    def howManyImportsMatched = matchingImportLevelsIndexes.size()
    def numberOfImports = namespace.importedNamespaces.size
    log.debug("max level depth on the current Order Graph of size {} is {} -- {} out of {} imports found in current Order Graph", orderGraph.size, max, howManyImportsMatched, numberOfImports)

    if(numberOfImports != howManyImportsMatched) { // this namespace imports something not in our map yet :/ boo
      log.debug("some import namespace(s) {}  not yet in our order graph, {}", importedNamespaceNotMatching, orderGraph)

      //add two levels, put this namespace at the 2nd addition, put the not mathching imports at the first addition
      def size = orderGraph.size()
      addNamespaceToLevel(namespace, size+1)
      importedNamespaceNotMatching.each { 
	addNamespaceToLevel(it, size) 
	notMatchingMap[it.namespace] = size
      }
    } else { // every import has a match and works nicely! :)
      log.debug("every imported namespace {} for namespace {} has a match", namespace.importedNamespaces.namespace, namespace.namespace)
      if(notMatchingMap.containsKey(namespace.namespace)) { 
	log.debug ("{} is in the not matching map : {}", namespace.namespace, notMatchingMap)
	if(processParentNamespaceAlreadyInGraph(namespace, max)) { 
	  log.debug("parent namespace process, it removed the parent need to re insert at {}, max match depth was {}", max+1, max)
	  reinsertNamespaceIntoLevel(namespace, max+1)
	}
	log.debug("!!!! this iteration of the order graph is {}", orderGraph)
	return
      }
      addNamespaceToLevel(namespace, max+1)
    }

    log.debug("!!!! this iteration of the order graph is {}", orderGraph)
  }

  /**
   * @param ns
   * @param max
   *
   * This function finds a previously added namespace to the #orderGraph, deletes its entry and reinsert at the correct place
   */
  boolean processParentNamespaceAlreadyInGraph(NamespaceMetaData ns, int max) { 
    def depth = notMatchingMap[ns.namespace]

    log.debug("this namespace was previously added to the order graph with no matches, so it was placed at the end of the order Graph plus two which at the time was {}, this has undoubtedly changed by now as the order Graph size is {}.  Need to remove from it's original position and re insert somewhere.  The max depth of matches is {}", depth, orderGraph.size, max)
    log.debug("order @ level depth {} is {}", depth, orderGraph[depth])
    log.debug("range of orders from {} to {} is {}", depth, orderGraph.size(), orderGraph[depth..-1])
    log.debug("order @ max depth {} is {}", max, orderGraph[max])

    if(max <= depth) { // if this is true, then delete the depth portion
      log.debug("max + 1 is {} and depth is {}", max+1, depth)
      removeNamespaceFromOrderGraph(ns, depth)
      removeNotMatchingEntry(ns.namespace, depth)
      return true
    }
    return false
  }

  /**
   * get's all the dependent NamespaceMetaData references that don't match those in orderGraph[0] i.e. the base namespaces without imports
   */
  def getDependentNamespaces() { 
    log.info("aquiring the List of the dependent namespaces to be graphed out")
    if (!namespaceData.disjoint(orderGraph[0])) {
      log.debug("namespaceData {} and base namespaces {} are not disjoint", namespaceData, orderGraph[0])
      return namespaceData - orderGraph[0]
    } else {
      log.debug("namespaceData is {}, orderGraph at first element is {}, the lists are disjoint", namespaceData, orderGraph[0])
      return []
    }
  }

  /**
   * @param namespace #NamespaceMetaData
   * @return a list, the list can either contain one value, a boolean, flagged as true, or two values, first value is a boolean false,followed by an integer that is index into the dependency graph where this import currently matches to.
   */
  def List findImportedNamespacesInOrderGraph(NamespaceMetaData namespace) {
    int graphLevelMatchingIndex = null
    boolean noMatchInDependencyTree

    log.debug("looking at import namespace {}", namespace)
    // find this import in the list 'level' in order graph.
    orderGraph.eachWithIndex { list, idx ->
      if(list.contains(namespace)) { 
	log.debug("Graph level {} with List {} contains import {}", idx, list, namespace)
	graphLevelMatchingIndex = idx
      }
    }
    if(graphLevelMatchingIndex == null) { 
      log.info("index is {}", graphLevelMatchingIndex)
      noMatchInDependencyTree = true
      return [noMatchInDependencyTree]
    }
    return [false, graphLevelMatchingIndex]
  }

  /**
   * graphs out each dependent namespace on the #orderGraph
   * 
   */
  def graphOutDependentNamespaces() { 
    getDependentNamespaces().each { ns ->
      log.debug("looking at namespace {}", ns.namespace)
      log.debug("this namespace imports {} namespaces: {}", ns.importedNamespaces.size, ns.importedNamespaces.namespace)
      def indexesOfMatchesInDependencyGraph = []
      def importsNotFoundInGraph = []
      ns.importedNamespaces.each { ins ->
	def matchedInGraph = findImportedNamespacesInOrderGraph(ins)
	if(matchedInGraph.size == 1) { 
	  importsNotFoundInGraph << ins
	} else { // has two elements, throw the first one away
	  indexesOfMatchesInDependencyGraph << matchedInGraph[1]
	}
      }
      addNamespaceToOrderGraph(ns, indexesOfMatchesInDependencyGraph, importsNotFoundInGraph)
    }

    //TODO make this happen only if namespaceData.size != orderGraph entries over all
    log.info("size is : ${namespaceData.size()}")

    def count = 0
    orderGraph.each { 
      count += it.size()
    }
    log.info("matching count is ${count}") 
    log.debug(this.toString())
  }

  /**
   * @param directory the directory to search for all *.xsd files
   * Finds all the xsd files in #directory, populates the Namesapce Meta Data #namespaceData
   */
  def findAllXsdFiles(String directory) { 
    def schemaList = []
    def baseDirectory = new File(directory)
    baseDirectory.eachFileRecurse(FileType.FILES) {  file -> 
      if(file.name.split("\\.")[-1] == 'xsd') {
	schemaList << file
      }
    }
    schemaList.each populateNamespaceMetaData
  }

  /**
   * @param schemaDoc the xsd file to parse and populate the Meta Data
   * Takes each xsd file, parses, gathers target namespace, and populates an array of MetaData with unique namespaces and files that declare those namespaces
   */
  def populateNamespaceMetaData = { schemaDoc ->
    def records = new XmlSlurper().parse(schemaDoc)
    def target = records.@targetNamespace
    target = (!target.isEmpty()) ? target.text() : "null"
    if (target == "null") {
      log.warning("There is no targetNamespace attribute for file {}, it is strongly advised best practice to ALWAYS include a targetNamespace attribute in your <xsd:schema> root element.  no targetNamespaces are referred to using the Chameleon design pattern, which is not advisable!", schemaDoc)
    }
    def namespace = namespaceData?.find{ it.namespace == target }
    if(namespace) {
      namespace.parseFiles << schemaDoc
    }
    else { 
      def newNamespace = new NamespaceMetaData()
      newNamespace.namespace = target
      newNamespace.parseFiles = [schemaDoc]
      namespaceData << newNamespace
    }
  }

  def String toString() { 
    def out = "The order to parse is:"
    orderGraph.each { 
      out += it
    }
    return out
  }
}