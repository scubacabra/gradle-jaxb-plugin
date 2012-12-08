package com.jacobo.gradle.plugins

import org.gradle.api.Project
/**
 * @author djmijares
 * Created: Tue Dec 04 09:01:34 EST 2012
 */
class JaxbNamespacePluginConvention {
  String xsdDir
  private Project project

  JaxbNamespacePluginConvention(Project project) { 
    this.project = project
  }
  def jaxbNamespace(Closure closure) { 
    closure.delegate = this
    closure()
  }
}