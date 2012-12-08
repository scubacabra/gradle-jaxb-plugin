package com.jacobo.gradle.plugins

import org.gradle.api.Project
import org.gradle.api.Plugin
import org.gradle.api.logging.Logging
import org.gradle.api.logging.Logger
/**
 * @author djmijares
 * Created: Tue Dec 04 09:01:34 EST 2012
 */
class JaxbNamespacePlugin implements Plugin<Project> {
  static final String JAXB_NAMESPACE_TASK_GROUP = 'parse'
  static final String JAXB_NAMESPACE_TASK = 'jaxbNamespaceResolve'
  static final Logger log = Logging.getLogger(this.getClass())

  void apply (Project project) {
    JaxbNamespacePluginConvention jaxbNamespaceConvention = new JaxbNamespacePluginConvention(project)
    project.convention.plugins.JaxbNamespacePlugin = jaxbNamespaceConvention
    
    JaxbNamespaceTask jnt = project.tasks.add(JAXB_NAMESPACE_TASK,  JaxbNamespaceTask)
  }
}