package org.gradle.jacobo.plugins.extension

import org.gradle.api.Project

class JaxbExtension {
  
  private Project project

  String xsdDir
  String bindingsDir
  String episodesDir
  
  List bindings

  def dependencyGraph

  JaxbExtension(Project project) { 
    this.project = project
  }
}