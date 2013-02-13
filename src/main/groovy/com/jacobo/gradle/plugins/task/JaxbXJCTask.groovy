package com.jacobo.gradle.plugins.task

import org.gradle.api.logging.Logging
import org.gradle.api.logging.Logger
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.DefaultTask
import org.gradle.api.InvalidUserDataException

import com.jacobo.gradle.plugins.JaxbPlugin
import com.jacobo.gradle.plugins.structures.OrderGraph
import com.jacobo.gradle.plugins.structures.NamespaceMetaData
import com.jacobo.gradle.plugins.util.XJCInputResolver
import com.jacobo.gradle.plugins.util.ListUtil

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
    	def nsData = dependencyGraph.namespaceData.find{ it == namespace}
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

    def schemaIncludes = XJCInputResolver.transformSchemaListToString(ns)
    def episodeBindings = XJCInputResolver.gatherAllDependencies(ns)
    def episodeName = "${project.jaxb.jaxbEpisodeDirectory}/${ns.episodeName}.episode"
    def bindings = XJCInputResolver.transformBindingListToString(project.jaxb.bindingIncludes)
    
    sanityChecks(episodeBindings, schemaIncludes, ns)

    ant.taskdef (name : 'xjc', 
    classname : 'com.sun.tools.xjc.XJCTask',
    classpath : project.configurations[JaxbPlugin.JAXB_CONFIGURATION_NAME].asPath)

    ant.xjc(destdir : project.jaxb.jaxbSchemaDestinationDirectory,
    extension : project.jaxb.extension,
    removeOldOutput : project.jaxb.removeOldOutput,
    header : project.jaxb.header) {
      //      produces (dir : project.jaxb.jaxbSchemaDestinationDirectory) //maybe should put the produces in there?
      schema (dir : project.jaxb.xsdDirectoryForGraph, includes : schemaIncludes )
      if (!project.jaxb.bindingIncludes.isEmpty() && project.jaxb.bindingIncludes != null) {
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

  def sanityChecks(episodeBindingsNames, schemaIncludes, ns) { 
    // needs to have include files
    if(schemaIncludes == "" || schemaIncludes.isEmpty()) throw new RuntimeException("There are no files to include in the parsing in " + ns.parseFiles + "for namespace " + ns.namespace)
    
    // default directory schema destination dir is relative to the project.projectDir not the rootDir like the rest of these
    directoryExists(project.projectDir.path + File.separator + project.jaxb.jaxbSchemaDestinationDirectory)

    // these Directories need to exist
    [project.jaxb.jaxbSchemaDirectory, project.jaxb.jaxbEpisodeDirectory].each directoryExists

    // if we have includes, then check for this binding directory existing.  if not don't check
    if (!project.jaxb.bindingIncludes.isEmpty() && project.jaxb.bindingIncludes != null) {
      directoryExists(project.jaxb.jaxbBindingDirectory)
    }
    
    // all episode file bindings MUST exist
    episodeBindingsNames.each { 
      def episode = it + ".episode"
      def file = new File(project.jaxb.jaxbEpisodeDirectory, episode)
      if(!file.exists()) throw new RuntimeException("${file} does not exist, there is most likely a null Namespace data somewhere")
    }
  }

  def directoryExists = { dir ->
    def directory = new File(dir)
    if(!directory.isDirectory() && !directory.exists()) throw new InvalidUserDataException(" ${directory} is not an existing directory, make this directory or adjust the extensions to point to a proper directory")
  }
}