package com.jacobo.gradle.plugins.task

import org.gradle.api.logging.Logging
import org.gradle.api.logging.Logger
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.DefaultTask
import com.jacobo.gradle.plugins.JaxbNamespacePlugin
import com.jacobo.gradle.plugins.structures.OrderGraph
import com.jacobo.gradle.plugins.structures.XsdNamespaces
import com.jacobo.gradle.plugins.structures.FindNamespaces
import org.gradle.api.InvalidUserDataException

/**
 * @author djmijares
 * Created: Tue Dec 04 09:01:34 EST 2012
 */

class JaxbXJCTask extends DefaultTask { 
  
  static final Logger log = Logging.getLogger(JaxbXJCTask.class)
  
  OrderGraph dependencyGraph

  @TaskAction
  void start() { 
    log.info("getting the order graph from the jaxb Extension on the project")
    dependencyGraph = project.jaxb.dependencyGraph
    dependencyGraph.orderGraph.each { order ->
      order.each { namespace ->
	log.info("trying to find {} in nsCollection", namespace)
    	def nsData = dependencyGraph.nsCollection.find{ it.namespace == namespace}
    	log.info("found structure {}", nsData)
   	if(!nsData?.xsdFiles?.isEmpty()) { 
	  doTheSchemaParsing(nsData)
	}
      }
    }
  }

  def doTheSchemaParsing(XsdNamespaces ns) { 
    // namespace data should have data
    if(ns == null) throw new RuntimeException("null namespace data trying to be parsed, no xsdFiles or any data present")

    def schemaIncludes = getIncludesList(ns)
    def episodeBindings = gatherAllDependencies(ns)
    def episodeName = "${project.jaxb.jaxbEpisodeDirectory}/${ns.convertNamespaceToEpisodeName(ns.namespace)}.episode"
    
    sanityChecks(episodeBindings, schemaIncludes, ns)

    ant.taskdef (name : 'xjc', 
    classname : 'com.sun.tools.xjc.XJCTask',
    classpath : project.configurations[JaxbNamespacePlugin.JAXB_CONFIGURATION_NAME].asPath)

    ant.xjc(destdir : project.jaxb.jaxbSchemaDestinationDirectory,
    extension : project.jaxb.extension,
    removeOldOutput : project.jaxb.removeOldOutput,
    header : project.jaxb.header) {
      //      produces (dir : "src/main/java")
      schema (dir : project.jaxb.xsdDirectoryForGraph, includes : schemaIncludes )
      if (!project.jaxb.bindingIncludes.isEmpty() || project.jaxb.bindingIncludes != null) {
	binding(dir : project.jaxb.jaxbBindingDirectory, includes : getBindingIncludesList(project.jaxb.bindingIncludes))
      }
      episodeBindings.each { episode ->
	log.info("binding with file {}.episode", episode)
	binding (dir : project.jaxb.jaxbEpisodeDirectory, includes : "${episode}.episode")
      }
      log.info("episode file is {}", episodeName)
      arg(value : '-episode')
      arg(value: episodeName)
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
    xsdData.externalDependencies.each { map ->
      map.value.each { file ->
	log.info("external dependency is {}, dependency List is {}", map.key, depList)
	if(this.dependencyGraph.externalDependencies?.containsKey(file.path)) { 
	  log.info("{} is in {}", file, this.dependencyGraph.externalDependencies)
	  this.dependencyGraph.externalDependencies[file.path].each { 
	    depList = addToDependencyList(depList, it)
	  }
	}
      }
    }
    log.info("current dependency List {}", depList)
    xsdData.fileImports.each { ns ->
      def xsdDependency = dependencyGraph.nsCollection.find{ it.namespace == ns}
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
    allDeps = allDeps.collect{ ns.convertNamespaceToEpisodeName(it) }
    return allDeps
  }

  def String getIncludesList(XsdNamespaces data) { 
    def includes = ""
    log.debug("xsdDir is {}", project.jaxb.xsdDirectoryForGraph)
    def absoluteDir = new File(project.jaxb.xsdDirectoryForGraph).path + File.separator
    log.debug("xsdDir is {}", absoluteDir)
    data?.xsdFiles?.each { path ->
      def relPath = path.path - absoluteDir
      relPath = relPath.replace("\\", "/")
      log.debug("the relative File Path is {}", relPath)
      includes += relPath + " "
    }
    log.info("argument to inludes is {}", includes)
    return includes
  }
  
  String getBindingIncludesList(List bindings) { 
    def bindingIncludes = ""
    log.debug("Binding list is {}", bindings)
    bindings.each { 
      bindingIncludes += it + " "
    }
    log.debug("binding list into xjc is {}", bindingIncludes)
    return bindingIncludes
  }

  def sanityChecks(episodeBindingsNames, schemaIncludes, ns) { 
    // needs to have include files
    if(schemaIncludes == "" || schemaIncludes.isEmpty()) throw new RuntimeException("There are no files to include in the parsing in " + ns.xsdFiles + "for namespace " + ns.namespace)
    
    // these Directories need to exist
    [project.jaxb.jaxbSchemaDirectory, project.jaxb.jaxbEpisodeDirectory, project.jaxb.jaxbBindingDirectory, project.jaxb.jaxbSchemaDestinationDirectory].each { 
      def dir = new File(it)
      if(!dir.isDirectory() && !dir.exists()) throw new InvalidUserDataException(" ${dir }is not an existing directory, make this directory or adjust the extensions to point to a proper directory")
    }
    
    // all episode file bindings MUST exist
    episodeBindingsNames.each { 
      def episode = it + ".episode"
      def file = new File(project.jaxb.jaxbEpisodeDirectory, episode)
      if(!file.exists()) throw new RuntimeException("${file} does not exist, there is most likely a null Namespace data somewhere")
    }
  }
}