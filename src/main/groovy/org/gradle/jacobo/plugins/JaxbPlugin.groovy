package org.gradle.jacobo.plugins

import com.google.inject.Guice
import com.google.inject.Injector

import org.gradle.api.Project
import org.gradle.api.Plugin

import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.logging.Logging
import org.gradle.api.logging.Logger

import org.gradle.jacobo.plugins.task.JaxbDependencyTree
import org.gradle.jacobo.plugins.task.JaxbXjc
import org.gradle.jacobo.plugins.extension.JaxbExtension
import org.gradle.jacobo.plugins.extension.XjcExtension
import org.gradle.jacobo.plugins.guice.JaxbPluginModule
import org.gradle.jacobo.schema.guice.DocSlurperModule

import org.gradle.jacobo.plugins.factory.XsdDependencyTreeFactory
import org.gradle.jacobo.plugins.resolver.ExternalDependencyResolver
import org.gradle.jacobo.plugins.resolver.NamespaceResolver
import org.gradle.jacobo.schema.factory.DocumentFactory
import org.gradle.jacobo.plugins.ant.AntXjc
import org.gradle.jacobo.plugins.converter.NamespaceToEpisodeConverter
import org.gradle.jacobo.plugins.resolver.XjcResolver
import org.gradle.jacobo.plugins.resolver.EpisodeDependencyResolver

/**
 * A plugin used for taking a whole folder full of .xsd files and enabling
 * separate compilation parsing the xsd files for @targetNamespace
 * information, grouping unique namespaces and generating an
 * @see OrderGraph
 * that is then run through the jaxb xjc task for each namespace and associated
 * @see XsdNamespaces data
 * <p>
 * Declares a <tt>jaxb</tt> configuration which needs to be configured with the
 * jaxb libraries to be used.
 * <p>
 * Declares a <tt>xjc</tt> task to be executed in gradle.  
 * <p>
 * Each XsdNamespaces data runs through the task <tt>xjc</tt> and generates an
 * episode file, all namespaces dependent on other namespaces re-use episode
 * files, so that projects depending on different schema set can be grouped
 * accordingly with no duplicate regeneration.
 * @author Daniel Mijares
 * Created: Tue Dec 04 09:01:34 EST 2012
 * @see JaxbExtension
 */
class JaxbPlugin implements Plugin<Project> {
  static final String JAXB_TASK_GROUP = 'parse'
  static final String JAXB_XSD_DEPENDENCY_TREE_TASK = 'xsd-dependency-tree'
  static final String JAXB_XJC_TASK = 'xjc'
  static final String JAXB_CONFIGURATION_NAME = 'jaxb'

  static final Logger log = Logging.getLogger(JaxbPlugin.class)

  private JaxbExtension extension

  void apply (Project project) {
    project.plugins.apply(JavaPlugin)
    Injector injector = Guice.createInjector([new JaxbPluginModule(), new DocSlurperModule()])
    configureJaxbExtension(project)
    configureJaxbConfiguration(project)
    JaxbDependencyTree jnt = configureJaxbDependencyTree(project, extension, injector)
    configureJaxbXjc(project, extension, jnt, injector)
  }
  
  private void configureJaxbExtension(final Project project) { 
    extension = project.extensions.create("jaxb", JaxbExtension, project)
    extension.with { 
      xsdDir = "schema"
      episodesDir = "schema/episodes"
      bindingsDir = "schema/bindings"
      bindings = []
    }
    def xjcExtension = project.jaxb.extensions.create("xjc", XjcExtension)
    xjcExtension.with {
      destinationDir = "src/main/java"
      producesDir = "src/main/java"
      extension = 'true'
      removeOldOutput = 'yes'
      header = true
    }
  }

  private void configureJaxbConfiguration(final Project project) {
    project.configurations.create(JAXB_CONFIGURATION_NAME) { 
      visible = true
      transitive = true
      description = "The JAXB XJC libraries to be used for this project."
    }
  }

  /**
   * Adds a dependency on tasks with the specified name in other projects.  The other projects are determined from
   * project lib dependencies using the specified configuration name. These may be projects this project depends on or
   * projects that depend on this project based on the useDependOn argument.
   *
   * @param task Task to add dependencies to
   * @param useDependedOn if true, add tasks from projects this project depends on, otherwise use projects that depend
   * on this one.
   * @param otherProjectTaskName name of task in other projects
   * @param configurationName name of configuration to use to find the other projects
   */
  private void addDependsOnTaskInOtherProjects(final Task task, boolean useDependedOn,
					       String otherProjectTaskName,
					       String configurationName) {
    Project project = task.getProject()
    final Configuration configuration = project.getConfigurations().getByName(
      configurationName)
    task.dependsOn(configuration.getTaskDependencyFromProjectDependency(
		     useDependedOn,otherProjectTaskName))
  }

  private JaxbDependencyTree configureJaxbDependencyTree(final Project project,
							JaxbExtension jaxb,
						        def injector) {
    JaxbDependencyTree jnt = project.tasks.create(JAXB_XSD_DEPENDENCY_TREE_TASK,
						 JaxbDependencyTree)
    jnt.description = "go through the folder ${jaxb.xsdDir} folder " +
      "and find all unique namespaces, create a namespace graph and parse in " +
      "the graph order with jaxb"
    jnt.group = JAXB_TASK_GROUP
    jnt.conventionMapping.xsds = { project.fileTree( dir:
	new File(project.rootDir,
		 project.jaxb.xsdDir), include: '**/*.xsd')
    }
    jnt.conventionMapping.docFactory = {
      injector.getInstance(DocumentFactory.class)
    }
    jnt.conventionMapping.namespaceResolver = {
      injector.getInstance(NamespaceResolver.class)
    }
    jnt.conventionMapping.externalDependencyResolver = {
      injector.getInstance(ExternalDependencyResolver.class)
    }
    jnt.conventionMapping.dependencyTreeFactory = {
      injector.getInstance(XsdDependencyTreeFactory.class)
    }
    // dependencies on projects with config jaxb, adds their xjc task to this
    // tasks dependencies
    addDependsOnTaskInOtherProjects(jnt, true, JAXB_XJC_TASK,
    				    JAXB_CONFIGURATION_NAME)
    return jnt
  }

  private void configureJaxbXjc(final Project project, JaxbExtension jaxb,
				JaxbDependencyTree jnt, def injector) {
    JaxbXjc xjc = project.tasks.create(JAXB_XJC_TASK, JaxbXjc)
    xjc.description = "run through the Directory Graph for " +
      "${jaxb.xsdDir} and parse all schemas in order generating" +
      " episode files to ${jaxb.episodesDir}"
    xjc.group = JAXB_TASK_GROUP
    xjc.dependsOn(jnt)
    xjc.conventionMapping.manager = { project.jaxb.dependencyGraph }
    xjc.conventionMapping.episodeDirectory = {
      new File(project.rootDir, project.jaxb.episodesDir) }
    xjc.conventionMapping.bindings = {
       // empty filecollection if no bindinfs listed, so that
       // files.files == empty set
       project.jaxb.bindings.isEmpty() ? project.files() :
       project.fileTree(dir:new File(project.rootDir, project.jaxb.bindingsDir),
			include: project.jaxb.bindings)
     }
    // these two are here so that gradle can check if anything
    // has changed in these folders and run this task again if so
    // they serve no other purpose than this
    xjc.conventionMapping.generatedFilesDirectory = {
      new File(project.projectDir, project.jaxb.xjc.destinationDir) }
    xjc.conventionMapping.schemasDirectory = {
      new File(project.rootDir, project.jaxb.xsdDir) }
    xjc.conventionMapping.xjc = { injector.getInstance(AntXjc.class) }
    xjc.conventionMapping.episodeConverter = {
      injector.getInstance(NamespaceToEpisodeConverter.class)
    }
    xjc.conventionMapping.xjcResolver = { injector.getInstance(XjcResolver.class) }
    xjc.conventionMapping.dependencyResolver = {
      injector.getInstance(EpisodeDependencyResolver.class)
    }
  }
}