package com.jacobo.gradle.plugins.task

import org.gradle.api.logging.Logging
import org.gradle.api.logging.Logger
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.DefaultTask
import com.jacobo.gradle.plugins.structures.OrderGraph
import com.jacobo.gradle.plugins.structures.XsdNamespaces
import com.jacobo.gradle.plugins.structures.FindNamespaces
import com.jacobo.gradle.plugins.JaxbNamespacePlugin

/**
 * @author djmijares
 * Created: Tue Dec 04 09:01:34 EST 2012
 */

class JaxbNamespaceTask extends DefaultTask { 
  
  static final Logger log = Logging.getLogger(JaxbNamespaceTask.class)
  
  String xsdDir
  OrderGraph order = new OrderGraph()

  def grabUniqueNamespaces() { 
    def findNs = new FindNamespaces(baseDir: getXsdDir())
    findNs.startFinding()
    findNs.nsMap.each { key, val -> 
      order.nsCollection << new XsdNamespaces(namespace: key, xsdFiles: val)
    }
  }

  @TaskAction
  void start() { 
    log.info("starting jaxb namespace dependency task at: {}", getXsdDir())
    grabUniqueNamespaces()
    log.info("unique namespaces aquired")
    log.info("getting all import statments in namespace files to see what they depend on")
    order.grabNamespaceImports()
    //order.grabNamespaceIncludes()
    order.findBaseSchemaNamespaces()
    log.info("found the base namespace packages to be parsed first")
    order.findDependentSchemaNamespaces()
    log.info("aquired the rest of the namespaces to be graphed out")
    order.parseEachDependentNamespace()
    log.info("namespace dependency graph is resolved")
    // project.convention.plugins.JaxbNamespacePlugin.target = nsCollection
    // log.info ("asafasf, {}", project.convention.plugins.JaxbNamespacePlugin.target[0])
    log.debug("order Graph is {}", order.orderGraph)
    order.orderGraph.each { order -> // fix this declaration, conflicting
      order.each { namespace ->
    	def nsData = this.order.nsCollection.find{ it.namespace == namespace}
    	log.info("found structure {}", nsData)
   	doTheSchemaParsing(nsData)
      }
    }
  }

  def doTheSchemaParsing(XsdNamespaces ns) { 
    ant.taskdef (name : 'xjc', classname : 'com.sun.tools.xjc.XJCTask', classpath : project.configurations[JaxbNamespacePlugin.JAXB_CONFIGURATION].asPath)
    ant.xjc(destdir : "src/main/java", extension : 'true', removeOldOutput : 'yes', header : false, target : '2.1') {
      //      produces (dir : "src/main/java")
      schema (dir : getXsdDir(), includes : getIncludesList(ns) )
      binding(dir : "${project.rootDir}/XMLSchema", includes : '*.xjb')
      gatherAllDependencies(ns).each { episode ->
	log.info("binding with file {}", ns.convertNamespaceToEpisodeName(episode)+ ".episode")
	binding (dir : "${project.rootDir}/XMLSchema/Episodes", includes : "${ns.convertNamespaceToEpisodeName(episode)}.episode")
      }
      log.info("episode file is {}", "${project.rootDir}/XMLSchema/Episodes/${ns.convertNamespaceToEpisodeName(ns.namespace)}.episode")
      arg(value : '-episode')
      arg(value: "${project.rootDir}/XMLSchema/Episodes/${ns.convertNamespaceToEpisodeName(ns.namespace)}.episode")
      arg(value : '-verbose')
      arg(value : '-npa')
    }
  }

  def addToDependencyList = { List depList, String ns ->
    log.info("trying to add  {} to dependency List {}", ns, depList)
    if(ns != "none" && !depList.contains(ns)) { 
      log.info("equals none ? : {}", ns == "none")
      log.info("adding {} to dep List {}", ns, depList)
      depList << ns
    }
    log.info("return dependency list is {}", depList)
    return depList
  }

  List findDependenciesUpGraph(XsdNamespaces xsdData, List depList) { 
    log.info("dep list is {}, xsd data is {}", depList, xsdData)
    xsdData.fileImports.each {
      log.info("file ns Import is {}, dependency List is {}", it, depList)
      depList = addToDependencyList(depList, it)
    }
    xsdData.externalDependencies.each {
      log.info("external dependency is {}, dependency List is {}", it, depList)
      depList = addToDependencyList(depList, it)
    }
    log.info("current dependency List {}", depList)
    xsdData.fileImports.each { ns ->
      def xsdDependency = order.nsCollection.find{ it.namespace == ns}
      if(xsdDependency) { 
    	depList = findDependenciesUpGraph(xsdDependency, depList)
    	log.info("current dependency List {}", depList)
      }
    }
    return depList
  }

  List gatherAllDependencies(XsdNamespaces ns) { 
    def allDeps = []
    log.info("Traversing the Graph up to the top for dependencies")
    allDeps = findDependenciesUpGraph(ns, allDeps)
    log.info("all dependencies are {}", allDeps)
    return allDeps
  }

  def String getIncludesList(XsdNamespaces data) { 
    def includes = ""
    log.debug("xsdDir is {}", getXsdDir())
    def absoluteDir = new File(getXsdDir()).path + File.separator
    log.debug("xsdDir is {}", absoluteDir)
    data.xsdFiles.each { path ->
      def relPath = path.path - absoluteDir
      relPath = relPath.replace("\\", "/")
      log.debug("the relative File Path is {}", relPath)
      includes += relPath + " "
    }
    log.debug("argument to inludes is {}", includes)
    return includes
  }
}