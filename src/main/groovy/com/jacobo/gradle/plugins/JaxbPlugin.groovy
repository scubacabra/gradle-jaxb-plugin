package com.jacobo.gradle.plugins

import org.gradle.api.Project
import org.gradle.api.Plugin

import org.gradle.api.plugins.JavaPlugin

import org.gradle.api.logging.Logging
import org.gradle.api.logging.Logger

import com.jacobo.gradle.plugins.task.JaxbNamespaceTask
import com.jacobo.gradle.plugins.task.JaxbXJCTask
import com.jacobo.gradle.plugins.JaxbExtension

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
  static final String JAXB_NAMESPACE_TASK_GROUP = 'parse'
  static final String JAXB_NAMESPACE_GRAPH_TASK = 'jaxb-generate-dependency-graph'
  static final String JAXB_NAMESPACE_GENERATE_TASK = 'xjc'
  static final String JAXB_CONFIGURATION_NAME = 'jaxb'

  static final Logger log = Logging.getLogger(JaxbPlugin.class)

  private JaxbExtension extension

  void apply (Project project) {
    project.plugins.apply(JavaPlugin)
    configureJaxbExtension(project)
    configureJaxbNamespaceConfiguration(project)
    JaxbNamespaceTask jnt = configureJaxbNamespaceDependencyGraph(project,
								  extension)
    configureJaxbGenerateSchemas(project, extension, jnt)
  }
  
  private void configureJaxbExtension(final Project project) { 
    extension = project.extensions.create("jaxb", JaxbExtension, project)
    extension.with { 
      jaxbSchemaDirectory = "schema"
      jaxbEpisodeDirectory = "schema/episodes"
      jaxbBindingDirectory = "schema/bindings"
      jaxbSchemaDestinationDirectory = "src/main/java"
      extension = 'true'
      removeOldOutput = 'yes'
    }
  }

  private void configureJaxbNamespaceConfiguration(final Project project) { 
    project.configurations.add(JAXB_CONFIGURATION_NAME) { 
      visible = true
      transitive = true
      description = "The JAXB XJC libraries to be used for this project."
    }
  }

  private JaxbNamespaceTask configureJaxbNamespaceDependencyGraph(
    final Project project, JaxbExtension jaxb) {
    JaxbNamespaceTask jnt = project.tasks.add(JAXB_NAMESPACE_GRAPH_TASK,
					      JaxbNamespaceTask)
    jnt.description = "go through the ${jaxb.xsdDirectoryForGraph} folder " +
      "and find all unique namespaces, create a namespace graph and parse in " +
      "the graph order with jaxb"
    jnt.group = JAXB_NAMESPACE_TASK_GROUP
    jnt.conventionMapping.xsdDirectory = {
      new File(project.rootDir, project.jaxb.jaxbSchemaDirectory) }
    return jnt
  }

  private void configureJaxbGenerateSchemas(final Project project,
					    JaxbExtension jaxb,
					    JaxbNamespaceTask jnt) {
    JaxbXJCTask xjc = project.tasks.add(JAXB_NAMESPACE_GENERATE_TASK,
					JaxbXJCTask)
    xjc.description = "run through the Directory Graph for " +
      "${jaxb.xsdDirectoryForGraph} and parse all schemas in order generating" +
      " episode files to ${jaxb.jaxbEpisodeDirectory}"
    xjc.group = JAXB_NAMESPACE_TASK_GROUP
    xjc.dependsOn(jnt)
    xjc.conventionMapping.manager = {
      new File(project.rootDir, project.jaxb.dependencyGraph) }
    xjc.conventionMapping.episodeDirectory = {
      new File(project.rootDir, project.jaxb.jaxbEpisodeDirectory) }
    xjc.conventionMapping.customBindingDirectory = {
      new File(project.rootDir, project.jaxb.jaxbBindingDirectory) }
    xjc.conventionMapping.generatedFilesDirectory = {
      new File(project.projectDir, project.jaxb.jaxbSchemaDestinationDirectory) }

  }
}