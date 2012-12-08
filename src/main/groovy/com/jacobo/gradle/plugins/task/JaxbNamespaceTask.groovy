package com.jacobo.gradle.plugins.task

import org.gradle.api.logging.Logging
import org.gradle.api.logging.Logger
import org.gradle.api.tasks.TaskAction
import org.gradle.api.DefaultTask
import com.jacobo.gradle.plugins.structures.OrderGraph
import com.jacobo.gradle.plugins.structures.XsdNamespaces
import com.jacobo.gradle.plugins.structures.FindNamespaces

/**
 * @author djmijares
 * Created: Tue Dec 04 09:01:34 EST 2012
 */

class JaxbNamespaceTask extends DefaultTask { 
  
  static final Logger log = Logging.getLogger(JaxbNamespaceTask.class)
  String xsdDir
  List nsCollection = []
  OrderGraph order = new OrderGraph()

  def grabUniqueNamespaces() { 
    this.xsdDir = project.convention.plugins.JaxbNamespacePlugin.xsdDir
    def findNs = new FindNamespaces(baseDir: xsdDir)
    findNs.startFinding()
    findNs.nsMap.each { key, val -> 
      nsCollection << new XsdNamespaces(namespace: key, xsdFiles: val)
    }
  }

  def grabNamespaceImports() { 
    nsCollection.each { ns -> 
      ns.xsdFiles.each { doc ->
	def schema = new XmlSlurper().parse(doc)
	def imports = schema.import
	if(!imports.isEmpty()) { 
	  imports.@namespace.each {
	    if(!ns.isExternalDependency(nsCollection, it.text()))
	      ns.addImports(it.text())
	  }
	}
      }
      if(!ns.fileImports) ns.fileImports << "none"
    }
  }

  List findBaseSchemaNamespaces() { 
    order.orderGraph[0] = nsCollection.findAll { it.fileImports == ["none"] }.namespace
    log.debug(order.toString())
  }

  def findDependentSchemaNamespaces() { 
    order.dependentNamespaces = nsCollection.findAll { !order.orderGraph[0].contains(it.namespace) }
  }

  def parseEachDependentNamespace() { 
    def notMatchingMap = [:]
    order.dependentNamespaces.each { ns ->
      def orderDepth = order.orderGraph.size() // how long is the depth parse list
      def matchesIdx = []
      def notMatching = []
      def fileCount = 0;
      log.debug( "looking at namespace ${ns.namespace}")
      ns.fileImports.each { namespace ->
	fileCount++
	  log.debug("import count is ${fileCount}, looking at import ${namespace}")
	  for (def i = 0; i< orderDepth; i++) { 
	    if(order.orderGraph[i].contains(namespace)) { 
	      log.debug("List ${order.orderGraph[i]} contains import ${namespace}")
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
	def size = order.orderGraph.size()
	order.orderGraph[size+1] = order.addToDependencyLevel(order.orderGraph[size+1], ns.namespace)
	order.orderGraph[size] = notMatching.each { 
	  order.addToDependencyLevel(order.orderGraph[size], it) 
	  notMatchingMap[it] = size
	}
      } else { // every import has a match works nicely!
	order.orderGraph[max+1] = order.addToDependencyLevel(order.orderGraph[max+1], ns.namespace)
      }

      //find out if this namespace is already in the graph, just in case becuase it could be
      log.debug "is there anything in the not matching map : ${notMatchingMap}"
      if(notMatchingMap.containsKey(ns.namespace)) { 
	log.debug "OMG redone no matching, need to remove it from order.orderGraph and re insert somewhere"
	log.debug "depth in original is ${notMatchingMap[ns.namespace]}"
	log.debug "it is in this list ${order.orderGraph[notMatchingMap[ns.namespace]]}"
	def depth =  notMatchingMap[ns.namespace]
	log.debug order.orderGraph[depth]
	log.debug order.orderGraph[depth..-1]
	log.trace order.orderGraph[max]
	if(max+1 < depth) { // if this is true, then delete the depth portion
	  order.orderGraph[depth] -= ns.namespace
	  if(order.orderGraph[depth].isEmpty()) { 
	    order.orderGraph.remove(depth)
	  }
	}
	notMatchingMap = notMatchingMap.findAll { it.key != ns.namespace} // get rid of the namespace we just corrected
      }
    }

    log.info("size is : ${nsCollection.size()}")

    def count = 0
    order.orderGraph.each { 
       count += it.size()
    }
    log.info("matching count is ${count}") 
    log.debug(order.toString())
  }

  @TaskAction
  void start() { 
    log.info("starting jaxb namespace dependency task")
    grabUniqueNamespaces()
    log.info("unique namespaces aquired")
    log.info("getting all import statments in namespace files to see what they depend on")
    grabNamespaceImports()
    findBaseSchemaNamespaces()
    log.info("found the base namespace packages to be parsed first")
    findDependentSchemaNamespaces()
    log.info("aquired the rest of the namespaces to be graphed out")
    parseEachDependentNamespace()
    log.info("namespace dependency graph is resolved")
  }

}