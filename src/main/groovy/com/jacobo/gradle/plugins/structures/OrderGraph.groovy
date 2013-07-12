package com.jacobo.gradle.plugins.structures

import org.gradle.api.logging.Logging
import org.gradle.api.logging.Logger
import com.jacobo.gradle.plugins.util.ListUtil
import com.jacobo.gradle.plugins.reader.DocumentReader

class OrderGraph { 
  static final Logger log = Logging.getLogger(OrderGraph.class)

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
   * Obtains the actual dependency information for all externally imported namespaces.
   * Gather all the namespaceData.externalImportedNamespaces maps, make them unique (could be dupes) and attach the starting files
   * to each namespace
   */
  def obtainExternalImportsDependencies() { 
    def containsExternals = this.namespaceData.findAll { !it.externalImportedNamespaces.isEmpty() } 
    log.debug("namespaces with external dependencies are {}", containsExternals)

    //populate this top level external dependencies, so you avoid ton's of processing if many target Namespace Data objects, import the same external namespace
    containsExternals.each { ns ->
      ns.externalImportedNamespaces.each { ext ->
	def extNamespace = externalDependencies.findAll { it.externalSchemaLocation == ext.externalSchemaLocation }
	if (extNamespace) { // has this file already
	  log.debug("target Namespace {} has an external dependency at file {} and this is already on the current external dependency list list {}", ns.namespace, ext.externalSchemaLocation, externalDependencies)
	} else { // new addition
	  log.debug("target Namespace {} has an external dependency at file {}, adding to this external dependency list", ns.namespace, ext.externalSchemaLocation)
	  externalDependencies << ext
	}
      }
    }

    log.debug("unique external dependencies locations for all namespaces {} is {}", namespaceData, externalDependencies*.externalSchemaLocation)

    // resolve these external namespaces, that is, go through and slurp their files with the External Namespace Resolver
    externalDependencies.each { external ->
      def resolver = new ExternalNamespaceResolver(external)
      external = resolver.resolveExternalImportedNamespaces()
      log.info("resolved the external namespace {} and got it's list of namespace it imports {}", external.namespace, external.importedNamespaces)
      log.debug("external namespace {} importedNs {} and externalDependencies has external imports {}", external.namespace, external.importedNamespaces, externalDependencies.importedNamespaces)
    }

    // I think that here, we should re populate the objects in containsExternals with their full resolved Meta Data from externalDependencies
    containsExternals.each { ns ->
      ns.externalImportedNamespaces.each { ext ->
    	def resolvedExternal = externalDependencies.find { it.externalSchemaLocation == ext.externalSchemaLocation }
    	if(resolvedExternal) { 
    	  log.info("namespace {} with external dependency file {} is being replaced with the same object, just populated data instead of being sparse", ns.namespace, ext.externalSchemaLocation)
    	  ext = resolvedExternal //make it the resolved
    	}
      }
    }

  }

  /**
   * gathers initial namespace graph ordering data objects that are flagged with a "none" for their #importedNamespaces field
   * populates with a reference from namespaceData
   */
  def gatherIndependentNamespaces() { 
    log.info("finding the base namespace packages to be parsed first, i.e. those namespace that do not import any other namespaces")
    orderGraph[0] = this.namespaceData.findAll { it.dependsOnAnotherNamespace == false }
    log.debug("Independent Namespaces are: {}", orderGraph[0])
  }

  /**
   * get's all the dependent NamespaceMetaData references that don't match those in orderGraph[0] i.e. the base namespaces without imports
   */
  def List<NamespaceMetaData> getDependentNamespaces() { 
    log.info("aquiring the List of the dependent namespaces to be graphed out")
    def dependentNamespaces = namespaceData.findAll { it.dependsOnAnotherNamespace == true }
    log.debug("namespaceData {} has these namespaces that depend on Another Namespace {}", namespaceData, dependentNamespaces)
    return dependentNamespaces
  }

  /**
   * Finds the level to place the namespace that has these imports, if ALL imports are NOT found in the order graph can't place, must put at back of queue and reprocess later.
   * @param List<NamespaceMetaData> #NamespaceMetaData
   * @return an integer of what level the namespace should be placed (takes the largest matching import index and places it immediately aftre that)
   */
  def findNamespacePlaceInOrderGraph(List<NamespaceMetaData> imports) {
    def importsMatchingAtLevels = []
    for (namespace in imports) {
      def matchingIndex
      log.debug("looking at import namespace {}, trying to find in order graph", namespace)
      int index = 0
      for(list in orderGraph) { 
	      log.debug("order graph is {}, we are looking at it's {} level {}", orderGraph, index, list)
        if(list.contains(namespace)) { 
	  log.debug("Order Graph level {} with List {} contains import {}", index, list, namespace)
	  matchingIndex = index
	  break
	}
	index++
      }
      if(matchingIndex >= 0) { 
        importsMatchingAtLevels << matchingIndex
      } else {
        log.debug("imported Namespace {} is NOT found in Order Graph {}, cannot continue processing must re-try later", namespace, orderGraph)
	return null
      }
    }
    return importsMatchingAtLevels.max() + 1
  }

  /**
   * graphs out each dependent namespace on the #orderGraph
   * 
   */
  def arrangeDependentNamespacesInOrder() { 
    def namespacesToArrange = []
    namespacesToArrange = getDependentNamespaces()
    while(namespacesToArrange) { 
      def ns = namespacesToArrange.pop()
      log.debug("looking at namespace {}", ns.namespace)
      log.debug("this namespace imports {} namespaces: {}", ns.importedNamespaces.size, ns.importedNamespaces.namespace)
      def allMatching = findNamespacePlaceInOrderGraph(ns.importedNamespaces)
      if(allMatching) { 
        log.debug("All {} imported namespaces were found in the current Order Graph {}, putting the namespace into the {} level of Order Graph", ns.importedNamespaces.size, orderGraph, allMatching)
	addNamespaceToLevel(ns, allMatching)
      } else {
      	log.debug("All {} imported namespaces were NOT found in the current Order Graph {} -- will reprocess after all other namespaces {} have been processed, adding to beginning (index 0) of toArrange list", ns.importedNamespaces.size, orderGraph, namespacesToArrange)
      	namespacesToArrange.add(0, ns)
      }
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
   * @param schemaDoc the xsd file to parse and populate the Meta Data
   * Takes each xsd file, parses, gathers target namespace, and populates an array of MetaData with unique namespaces and files that declare those namespaces
   */
  def obtainUniqueNamespaces = { schemaDoc ->
    def xsd = DocumentReader.slurpDocument(schemaDoc)
    xsd.grabXsdNamespace()
    def matchingNamespace =  namespaceData?.find{ it.namespace == xsd.xsdNamespace }
    if(matchingNamespace) {
      matchingNamespace.slurpers << xsd
    }
    else { 
      def newNamespace = new NamespaceMetaData()
      newNamespace.namespace = xsd.xsdNamespace
      newNamespace.slurpers = [xsd]
      namespaceData << newNamespace
      newNamespace.convertNamespaceToEpisodeName()
    }
  }

  /**
   * obtains the includes and imports data for each namespace in namespaceData
   * Obtains imported namespace, that being internal (to namespaceData) or external (to namespaceData)
   * if a namespace does import another namespace, it is flagged for importing and will not be grabbed first
   */
  def obtainNamespacesMetaData() { 
    this.namespaceData.each { namespace ->
      namespace.slurpers.each { slurped ->
        slurped.grabXsdDependencies()
      }
    }
    this.namespaceData.each { namespace ->
     namespace.obtainImportedNamespaces(namespaceData)
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