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

class JaxbXJCTask extends DefaultTask { 
  
  static final Logger log = Logging.getLogger(JaxbXJCTask.class)
  
  @TaskAction
  void start() { 
    OrderGraph dependencyGraph = project.jaxb.dependencyGraph
    order.orderGraph.each { order -> // fix this declaration, conflicting
      order.each { namespace ->
    	def nsData = dependencyGraph.nsCollection.find{ it.namespace == namespace}
    	log.info("found structure {}", nsData)
   	doTheSchemaParsing(nsData)
      }
    }
  }

  def doTheSchemaParsing(XsdNamespaces ns) { 
    ant.taskdef (name : 'xjc', classname : 'com.sun.tools.xjc.XJCTask', classpath : project.configurations[JaxbNamespacePlugin.JAXB_CONFIGURATION].asPath)
    ant.xjc(destdir : project.jaxb.jaxbSchemaDestinationDirectory, extension : project.jaxb.extension, removeOldOutput : project.jaxb.removeOldOutput, header : project.jaxb.header, target : '2.1') {
      //      produces (dir : "src/main/java")
      schema (dir : project.jaxb.xsdDirectoryForGraph, includes : getIncludesList(ns) )
      binding(dir : project.jaxb.jaxbBindingDirectory, includes : '*.xjb')
      gatherAllDependencies(ns).each { episode ->
	log.info("binding with file {}", ns.convertNamespaceToEpisodeName(episode)+ ".episode")
	binding (dir : project.jaxb.jaxbEpisodeDirectory, includes : "${ns.convertNamespaceToEpisodeName(episode)}.episode")
      }
      log.info("episode file is {}", "${project.jaxb.jaxbEpisodeDirectory}/${ns.convertNamespaceToEpisodeName(ns.namespace)}.episode")
      arg(value : '-episode')
      arg(value: "${project.jaxb.jaxbEpisodeDirectory}/${ns.convertNamespaceToEpisodeName(ns.namespace)}.episode")
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
    log.debug("xsdDir is {}", project.jaxb.xsdDirectoryForGraph)
    def absoluteDir = new File(project.jaxb.xsdDirectoryForGraph).path + File.separator
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