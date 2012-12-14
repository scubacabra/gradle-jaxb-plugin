package com.jacobo.gradle.plugins

import org.gradle.api.Project
import org.gradle.api.Plugin
import org.gradle.api.logging.Logging
import org.gradle.api.logging.Logger
import com.jacobo.gradle.plugins.task.JaxbNamespaceTask
import com.jacobo.gradle.plugins.task.JaxbXJCTask
import com.jacobo.gradle.plugins.JaxbExtension

/**
 * @author djmijares
 * Created: Tue Dec 04 09:01:34 EST 2012
 */
class JaxbNamespacePlugin implements Plugin<Project> {
  static final String JAXB_NAMESPACE_TASK_GROUP = 'parse'
  static final String JAXB_NAMESPACE_GRAPH_TASK = 'jaxb-generate-dependency-graph'
  static final String JAXB_NAMESPACE_GENERATE_TASK = 'xjc'
  static final String JAXB_CONFIGURATION = 'jaxbXjc'

  static final Logger log = Logging.getLogger(JaxbNamespacePlugin.class)

  private JaxbExtension extension
  void apply (Project project) {
    extension = project.extensions.create("jaxb", JaxbExtension, project)
    extension.with { 
      jaxbSchemaDirectory = "${project.rootDir}/schema"
      jaxbEpisodeDirectory = "${project.rootDir}/schema/episodes" 
      jaxbBindingDirectory = "${project.rootDir}/schema/bindings"
      jaxbSchemaDestinationDirectory = "src/main/java"
      extension = 'true'
      removeOldOutput = 'yes'
    }
    configureJaxbNamespaceDependencyGraph(project, extension)
  }

  private void configureJaxbNamespaceDependencyGraph(final Project project, JaxbExtension jaxb) { 
    JaxbNamespaceTask jnt = project.tasks.add(JAXB_NAMESPACE_GRAPH_TASK,  JaxbNamespaceTask)
    jnt.description = "go through the ${jaxb.xsdDirectoryForGraph} folder and find all unique namespaces, create a namespace graph and parse in teh graph order with jaxb"
  }

  private void configureJaxbGenerateSchemas(final Project project, JaxbExtension jaxb) { 
    JaxbXJCTask xjc = project.tasks.add(JAXB_NAMESPACE_GENERATE_TASK,  JaxbXJCTask)
    xjc.description = "run through the Directory Graph for ${jaxb.xsdDirectoryForGraph} and parse all schemas in order generating episode files to ${jaxb.jaxbEpisodeDirectory}"
    xjc.dependsOn('')
  }
}