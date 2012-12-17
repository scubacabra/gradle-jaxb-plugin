package com.jacobo.gradle.plugins.structures

import org.gradle.api.logging.Logging
import org.gradle.api.logging.Logger

class OrderGraph { 
  static final Logger log = Logging.getLogger(OrderGraph.class)
  
  String startingDirectory
  List orderGraph = []
  List dependentNamespaces = []
  List nsCollection = []

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

  def grabNamespaceImports() { 
    this.nsCollection.each { ns -> 
      ns.xsdFiles.each { doc ->
	def schema = new XmlSlurper().parse(doc)
	def imports = schema.import
	if(!imports.isEmpty()) { 
	  imports.@namespace.each {
	    if(!ns.isExternalDependency(this.nsCollection, it.text()))
	      ns.addImports(it.text())
	  }
	}
      }
      if(!ns.fileImports) ns.fileImports << "none"
    }
  }

  def grabNamespaceIncludes() { 
    this.nsCollection.each { ns -> 
      ns.xsdFiles.each { doc ->
	def schema = new XmlSlurper().parse(doc)
	def includes = schema.include
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
    }
  }

  def performIncludesProcessing() { 
    def withIncludes = this.nsCollection.findAll { !it.fileIncludes.isEmpty() }
    log.info("Total includes over the whole Directory")
    withIncludes.each { ns ->
      log.info("namespace {}, includes {}", ns.namespace, ns.fileIncludes)
      def hasIncludeFiles = this.nsCollection.findAll { !it.xsdFiles.disjoint(ns.fileIncludes)}
      hasIncludeFiles.each { nsHasIncludes ->
	log.info("namespaces with these includes are {}", nsHasIncludes.namespace)
	nsHasIncludes.xsdFiles = nsHasIncludes.xsdFiles.minus(ns.fileIncludes)
	log.info("New xsd Files for namespace {} is : {}", nsHasIncludes.namespace, nsHasIncludes.xsdFiles)
      }
    }
    //if any of the field xsdFiles are empty (couldn't be null), then get them off of the nsCollection and get the off of the order GRAPH
    def emptyData = this.nsCollection.findAll { it.xsdFiles.isEmpty() }
    if(emptyData) {
      log.warn("There is empty Namespace xsd files in {}", emptyData)
      this.nsCollection = this.nsCollection.findAll{ !it.xsdFiles.isEmpty() } 
    }
  }

  def findBaseSchemaNamespaces() { 
    this.orderGraph[0] = this.nsCollection.findAll { it.fileImports == ["none"] }.namespace
    log.debug("Base schema meta data information {}", this.nsCollection.findAll { it.fileImports == ["none"]})
  }

  def findDependentSchemaNamespaces() { 
    this.dependentNamespaces = this.nsCollection.findAll { !this.orderGraph[0].contains(it.namespace) }
  }

  def parseEachDependentNamespace() { 
    def notMatchingMap = [:]
    this.dependentNamespaces.each { ns ->
      def orderDepth = this.orderGraph.size() // how long is the depth parse list
      def matchesIdx = []
      def notMatching = []
      def fileCount = 0;
      log.debug( "looking at namespace ${ns.namespace}")
      log.debug("namespace has files {}", ns.xsdFiles)
      log.debug("namespace has imports {}", ns.fileImports)
      ns.fileImports.each { namespace ->
	fileCount++
	  log.debug("import count is ${fileCount}, looking at import ${namespace}")
	  for (def i = 0; i< orderDepth; i++) { 
	    if(this.orderGraph[i].contains(namespace)) { 
	      log.debug("List ${this.orderGraph[i]} contains import ${namespace}")
	      matchesIdx << i
	    }
	  }
	  if(matchesIdx.size() != fileCount - notMatching.size()) { 
	    log.debug "matches size is ${matchesIdx.size()}"
	    log.debug "not matching size is ${notMatching.size()}"
	    log.debug "file count is ${fileCount}" 
	    log.debug "fileCount - notMatching.size() is ${fileCount - notMatching.size()}"
	    notMatching << namespace		    
	    log.debug "not matching is ${notMatching}"
	    log.debug "import count is ${fileCount}"
	    log.debug "matchesIds is ${matchesIdx}"
	  }
      }
      def max = matchesIdx.max() //get the max dependency depth
      log.debug "max match depth is ${max}"
      if(ns.fileImports.size() != matchesIdx.size()) { // this namespace imports something not in our map yet :/ boo
	log.debug "something is not matching yet"
	log.debug notMatching
	def size = this.orderGraph.size()
	this.orderGraph[size+1] = this.addToDependencyLevel(this.orderGraph[size+1], ns.namespace)
	this.orderGraph[size] = notMatching.each { 
	  this.addToDependencyLevel(this.orderGraph[size], it) 
	  notMatchingMap[it] = size
	}
      } else { // every import has a match works nicely!
	this.orderGraph[max+1] = this.addToDependencyLevel(this.orderGraph[max+1], ns.namespace)
      }

      //find out if this namespace is already in the graph, just in case becuase it could be
      log.debug "is there anything in the not matching map : ${notMatchingMap}"
      if(notMatchingMap.containsKey(ns.namespace)) { 
	log.debug "OMG redone no matching, need to remove it from this.orderGraph and re insert somewhere"
	log.debug "depth in original is ${notMatchingMap[ns.namespace]}"
	log.debug "it is in this list ${this.orderGraph[notMatchingMap[ns.namespace]]}"
	def depth =  notMatchingMap[ns.namespace]
	log.debug("order @ depth is {}", this.orderGraph[depth])
	log.debug(" range of orders from {}", this.orderGraph[depth..-1])
	log.debug("order @ max depth {}", this.orderGraph[max])
	if(max+1 < depth) { // if this is true, then delete the depth portion
	  this.orderGraph[depth] -= ns.namespace
	  if(this.orderGraph[depth].isEmpty()) { 
	    this.orderGraph.remove(depth)
	  }
	}
	notMatchingMap = notMatchingMap.findAll { it.key != ns.namespace} // get rid of the namespace we just corrected
      }
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