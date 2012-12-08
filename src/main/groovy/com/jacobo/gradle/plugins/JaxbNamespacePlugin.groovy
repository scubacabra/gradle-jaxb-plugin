package com.jacobo.gradle.plugins

import org.gradle.api.Project
import org.gradle.api.Plugin
import org.gradle.api.logging.Logging
import org.gradle.api.logging.Logger
/**
 * @author djmijares
 * Created: Tue Dec 04 09:01:34 EST 2012
 */
class JaxbNamespacePlugin implements Plugin<Project> {
  static final String JAXB_NAMESPACE_TASK_GROUP = 'parse'
  static final Logger log = Logging.getLogger(this.getClass())

  void apply (Project project) {
    JaxbNamespacePluginConvention jaxbNamespaceConvention = new JaxbNamespacePluginConvention()
    project.convention.plugins.JaxbNamespacePlugin = jaxbNamespaceConvention
    
    project.task('jaxbns-test', group: JAXB_NAMESPACE_TASK_GROUP) << { 
      def findNs = new findNamespaces(baseDir: jaxbNamespaceConvention.xsdDir)
      findNs.startFinding()
      def order = new orderGraph()
      def nsCollection = []
      findNs.nsMap.each { key, val -> 
      	nsCollection << new xsdNamespaces(namespace: key, xsdFiles: val)
      }

      nsCollection.each { ns -> 
	ns.xsdFiles.each { doc ->
	  def schema = new XmlSlurper().parse(doc)
	  def imports = schema.import
	  if(!imports.isEmpty()) { 
	    imports.@namespace.each {
	      if(!ns.isExternalDependency(findNs.nsMap, it.text()))
		ns.addImports(it.text())
	    }
	  }
	}
	if(!ns.fileImports) ns.fileImports << "none"
      }
      
      nsCollection.each { println it }
      order.orderGraph[0] = nsCollection.findAll { it.fileImports == ["none"] }.namespace
      println order
      
      def nsToParse = nsCollection.findAll { !order.orderGraph[0].contains(it.namespace) }

      def isAlreadyInList = nsCollection[0].&isAlreadyInList
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
      def notMatchingMap = [:]
      nsToParse.each { ns ->
	def orderDepth = order.orderGraph.size() // how long is the depth parse list
	def matchesIdx = []
	def notMatching = []
	def fileCount = 0;
	println "looking at namespace ${ns.namespace}"
	ns.fileImports.each { namespace ->
	  fileCount++
	    println "import count is ${fileCount}, looking at import ${namespace}"


	    for (def i = 0; i< orderDepth; i++) { 
	      if(order.orderGraph[i].contains(namespace)) { 
		//	println "List ${orderToParse[i]} contains import ${namespace}"
		matchesIdx << i
	      }
	    }
	    if(matchesIdx.size() != fileCount - notMatching.size()) { 
	      // println "matches size is ${matchesIdx.size()}"
	      // println "not matching size is ${notMatching.size()}"
	      // println "file count is ${fileCount}"
	      // println "fileCount - notMatching.size() is ${fileCount - notMatching.size()}"
	      notMatching << namespace
	      // println "not matching is ${notMatching}"
	      // println "import count is ${fileCount}"
	      // println "matchesIds is ${matchesIdx}"
	    }
	}
	def max = matchesIdx.max() //get the max dependency depth
	println "max match depth is ${max}"
	if(ns.fileImports.size() != matchesIdx.size()) { // this namespace imports something not in our map yet :/ boo
	  //    println "something is not matching yet"
	  //    println notMatching
	  def size = order.orderGraph.size()
	  // orderToParse[size+1] = (orderToParse[size+1]) ? orderToParse[size+1] << ns.namespace : [ns.namespace]
	  // orderToParse[size] = (orderToParse[size]) ? orderToParse[size] << notMatching : notMatching
	  order.orderGraph[size+1] = addToDependencyLevel(order.orderGraph[size+1], ns.namespace)
	  order.orderGraph[size] = notMatching.each { 
	    addToDependencyLevel(order.orderGraph[size], it) 
	    notMatchingMap[it] = size
	  }
    
	} else { // every import has a match works nicely!
	  // orderToParse[max+1] = (orderToParse[max+1]) ? orderToParse[max+1] << ns.namespace : [ns.namespace]
	  order.orderGraph[max+1] = addToDependencyLevel(order.orderGraph[max+1], ns.namespace)
	}

	//  println orderToParse
	//find out if this namespace is already in the graph, just in case becuase it could be
	println "is there anything in the not matching map : ${notMatchingMap}"
	if(notMatchingMap.containsKey(ns.namespace)) { 
	  println "OMG redone no matching, need to remove it from order.orderGraph and re insert somewhere"
	  println "depth in original is ${notMatchingMap[ns.namespace]}"
	  println "it is in this list ${order.orderGraph[notMatchingMap[ns.namespace]]}"
	  def depth =  notMatchingMap[ns.namespace]
	  println order.orderGraph[depth]
	  println order.orderGraph[depth..-1]
	  println order.orderGraph[max]
	  if(max+1 < depth) { // if this is true, then delete the depth portion
	    order.orderGraph[depth] -= ns.namespace
	    if(order.orderGraph[depth].isEmpty()) { 
	      order.orderGraph.remove(depth)
	    }
	  }
	  notMatchingMap = notMatchingMap.findAll { it.key != ns.namespace} // get rid of the namespace we just corrected
	}
      }

      println nsCollection.size()

      def count = 0
      order.orderGraph.each { 
	println it
	count += it.size()
      }
      println count	 
    }
    // add your plugin tasks here.
  }
}