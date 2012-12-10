package com.jacobo.gradle.plugins

import org.gradle.api.Project
import org.gradle.api.Plugin
import org.gradle.api.logging.Logging
import org.gradle.api.logging.Logger
import com.jacobo.gradle.plugins.task.JaxbNamespaceTask

/**
 * @author djmijares
 * Created: Tue Dec 04 09:01:34 EST 2012
 */
class JaxbNamespacePlugin implements Plugin<Project> {
  static final String JAXB_NAMESPACE_TASK_GROUP = 'parse'
  static final String JAXB_NAMESPACE_TASK = 'jaxbNamespaceResolve'
  static final String JAXB_CONFIGURATION = 'jaxb'

  static final Logger log = Logging.getLogger(JaxbNamespacePlugin.class)

  void apply (Project project) {
    JaxbNamespacePluginConvention jaxbNamespaceConvention = new JaxbNamespacePluginConvention(project)
    project.convention.plugins.JaxbNamespacePlugin = jaxbNamespaceConvention

       configureJaxbNamespaceTask(project, jaxbNamespaceConvention)

    }

  private void configureJaxbNamespaceTask(final Project project, JaxbNamespacePluginConvention convention) { 
    JaxbNamespaceTask jnt = project.tasks.add(JAXB_NAMESPACE_TASK,  JaxbNamespaceTask)
    jnt.description = "go through the ${convention.xsdDir} folder and find all unique namespaces, create a namespace graph and parse in teh graph order with jaxb"
    jnt.conventionMapping.map('xsdDir') { convention.xsdDir } //this mapping already has a convention for xsdDir so need to use this inconvenient but safer mapping instead of just jnt.conventionMapping.xsdDir or something like that

  }
}