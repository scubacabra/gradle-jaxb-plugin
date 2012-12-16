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
  static final String JAXB_CONFIGURATION_NAME = 'jaxb'

  static final Logger log = Logging.getLogger(JaxbNamespacePlugin.class)

  private JaxbExtension extension

  void apply (Project project) {
    configureJaxbExtension(project)
    configureJaxbNamespaceConfiguration(project)
    JaxbNamespaceTask jnt = configureJaxbNamespaceDependencyGraph(project, extension)
    configureJaxbGenerateSchemas(project, extension, jnt)
  }
  
  private void configureJaxbExtension(final Project project) { 
    extension = project.extensions.create("jaxb", JaxbExtension, project)
    extension.with { 
      jaxbSchemaDirectory = "${project.rootDir}/schema"
      jaxbEpisodeDirectory = "${project.rootDir}/schema/episodes" 
      jaxbBindingDirectory = "${project.rootDir}/schema/bindings"
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

  private JaxbNamespaceTask configureJaxbNamespaceDependencyGraph(final Project project, JaxbExtension jaxb) { 
    JaxbNamespaceTask jnt = project.tasks.add(JAXB_NAMESPACE_GRAPH_TASK,  JaxbNamespaceTask)
    jnt.description = "go through the ${jaxb.xsdDirectoryForGraph} folder and find all unique namespaces, create a namespace graph and parse in teh graph order with jaxb"
    jnt.group = JAXB_NAMESPACE_TASK_GROUP
    return jnt
  }

  private void configureJaxbGenerateSchemas(final Project project, JaxbExtension jaxb, JaxbNamespaceTask jnt) { 
    JaxbXJCTask xjc = project.tasks.add(JAXB_NAMESPACE_GENERATE_TASK,  JaxbXJCTask)
    xjc.description = "run through the Directory Graph for ${jaxb.xsdDirectoryForGraph} and parse all schemas in order generating episode files to ${jaxb.jaxbEpisodeDirectory}"
    xjc.group = JAXB_NAMESPACE_TASK_GROUP
    xjc.dependsOn(jnt)
    
  }
}