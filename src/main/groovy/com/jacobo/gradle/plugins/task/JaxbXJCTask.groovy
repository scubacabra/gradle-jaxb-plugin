package com.jacobo.gradle.plugins.task

import org.gradle.api.logging.Logging
import org.gradle.api.logging.Logger
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.DefaultTask
import org.gradle.api.InvalidUserDataException

import com.jacobo.gradle.plugins.JaxbNamespacePlugin
import com.jacobo.gradle.plugins.structures.OrderGraph
import com.jacobo.gradle.plugins.structures.NamespaceMetaData
import com.jacobo.gradle.plugins.util.XJCInputResolver

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
	log.info("trying to find {} in namespaceData", namespace)
    	def nsData = dependencyGraph.namespaceData.find{ it.namespace == namespace}
    	log.info("found structure {}", nsData)
   	if(!nsData?.parseFiles?.isEmpty()) { 
	  doTheSchemaParsing(nsData)
	}
      }
    }
  }

  def doTheSchemaParsing(NamespaceMetaData ns) { 
    // namespace data should have data
    if(ns == null) throw new RuntimeException("null namespace data trying to be parsed, no parseFiles or any data present")

    def schemaIncludes = transformSchemaListToString(ns)
    def episodeBindings = gatherAllDependencies(ns)
    def episodeName = "${project.jaxb.jaxbEpisodeDirectory}/${ns.episodeName}.episode"
    def bindings = XJCInputResolver.transformBindingListToString(project.jaxb.bindingIncludes)
    
    sanityChecks(episodeBindings, schemaIncludes, ns)

    ant.taskdef (name : 'xjc', 
    classname : 'com.sun.tools.xjc.XJCTask',
    classpath : project.configurations[JaxbNamespacePlugin.JAXB_CONFIGURATION_NAME].asPath)

    ant.xjc(destdir : project.jaxb.jaxbSchemaDestinationDirectory,
    extension : project.jaxb.extension,
    removeOldOutput : project.jaxb.removeOldOutput,
    header : project.jaxb.header) {
      //      produces (dir : "src/main/java") //maybe should put the produces in there?
      schema (dir : project.jaxb.xsdDirectoryForGraph, includes : schemaIncludes )
      if (!project.jaxb.bindingIncludes.isEmpty() || project.jaxb.bindingIncludes != null) {
	binding(dir : project.jaxb.jaxbBindingDirectory, includes : bindings)
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

  List findDependenciesUpGraph(NamespaceMetaData xsdData, List depList) {
    log.info("dep list is {}, xsd data is {}", depList, xsdData)
    xsdData.importedNamespaces.each {
      log.info("file ns Import is {}, dependency List is {}", it, depList)
      depList = addToDependencyList(depList, it)
    }
    xsdData.externalImportedNamespaces.each { map ->
      map.value.each { file ->
	log.info("external dependency is {}, dependency List is {}", map.key, depList)
	if(this.dependencyGraph.externalImportedNamespaces?.containsKey(file.path)) { 
	  log.info("{} is in {}", file, this.dependencyGraph.externalImportedNamespaces)
	  this.dependencyGraph.externalImportedNamespaces[file.path].each { 
	    depList = addToDependencyList(depList, it)
	  }
	}
      }
    }
    log.info("current dependency List {}", depList)
    xsdData.importedNamespaces.each { ns ->
      def xsdDependency = dependencyGraph.namespaceData.find{ it.namespace == ns}
      if(xsdDependency) { 
    	depList = findDependenciesUpGraph(xsdDependency, depList)
    	log.info("current dependency List {}", depList)
      }
    }
    return depList
  }

  List gatherAllDependencies(NamespaceMetaData ns) { 
    def allDeps = []
    log.info("Traversing the Graph up to the top for dependencies")
    allDeps = findDependenciesUpGraph(ns, allDeps)
    log.info("all dependencies are {}", allDeps)
    allDeps = allDeps.collect{ ns.convertNamespaceToEpisodeName(it) }
    return allDeps
  }
  
  def sanityChecks(episodeBindingsNames, schemaIncludes, ns) { 
    // needs to have include files
    if(schemaIncludes == "" || schemaIncludes.isEmpty()) throw new RuntimeException("There are no files to include in the parsing in " + ns.parseFiles + "for namespace " + ns.namespace)
    
    // default directory schema destination dir is relative to the project.projectDir not the rootDir like the rest of these
    def destDir = new File(project.projectDir, project.jaxb.jaxbSchemaDestinationDirectory)
    if(!destDir.isDirectory() && !destDir.exists()) throw new InvalidUserDataException(" ${destDir} is not an existing directory, make this directory or adjust the extensions to point to a proper directory")

    // these Directories need to exist
    [project.jaxb.jaxbSchemaDirectory, project.jaxb.jaxbEpisodeDirectory, project.jaxb.jaxbBindingDirectory].each { 
      def dir = new File(it)
      if(!dir.isDirectory() && !dir.exists()) throw new InvalidUserDataException(" ${dir} is not an existing directory, make this directory or adjust the extensions to point to a proper directory")
    }
    
    // all episode file bindings MUST exist
    episodeBindingsNames.each { 
      def episode = it + ".episode"
      def file = new File(project.jaxb.jaxbEpisodeDirectory, episode)
      if(!file.exists()) throw new RuntimeException("${file} does not exist, there is most likely a null Namespace data somewhere")
    }
  }
}