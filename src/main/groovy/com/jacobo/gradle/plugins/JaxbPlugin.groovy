package com.jacobo.gradle.plugins

import org.gradle.api.Project
import org.gradle.api.Plugin

import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
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
    Project project = task.getProject();
    final Configuration configuration = project.getConfigurations().getByName(
      configurationName);
    task.dependsOn(configuration.getTaskDependencyFromProjectDependency(
		     useDependedOn,otherProjectTaskName));
  }

  private JaxbNamespaceTask configureJaxbNamespaceDependencyGraph(
    final Project project, JaxbExtension jaxb) {
    JaxbNamespaceTask jnt = project.tasks.create(JAXB_NAMESPACE_GRAPH_TASK,
					      JaxbNamespaceTask)
    jnt.description = "go through the ${jaxb.xsdDirectoryForGraph} folder " +
      "and find all unique namespaces, create a namespace graph and parse in " +
      "the graph order with jaxb"
    jnt.group = JAXB_NAMESPACE_TASK_GROUP
    jnt.conventionMapping.xsdDirectory = {
      new File(project.rootDir, project.jaxb.jaxbSchemaDirectory) }
    // dependencies on projects with config jaxb, adds their xjc task to this tasks dependencies
    addDependsOnTaskInOtherProjects(jnt, true, JAXB_NAMESPACE_GENERATE_TASK,
    				    JAXB_CONFIGURATION_NAME)
    return jnt
  }

  private void configureJaxbGenerateSchemas(final Project project,
					    JaxbExtension jaxb,
					    JaxbNamespaceTask jnt) {
    JaxbXJCTask xjc = project.tasks.create(JAXB_NAMESPACE_GENERATE_TASK,
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
    xjc.conventionMapping.schemasDirectory = {
      new File(project.rootDir, project.jaxb.jaxbSchemaDirectory) }
  }
}