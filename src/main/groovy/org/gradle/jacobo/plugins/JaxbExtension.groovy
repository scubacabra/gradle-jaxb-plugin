package org.gradle.jacobo.plugins

import org.gradle.api.Project

/**
 * @author djmijares
 * Created: Tue Dec 04 09:01:34 EST 2012
 */
class JaxbExtension {
  
  private Project project
  
  String jaxbSchemaDirectory
  String jaxbEpisodeDirectory
  String jaxbBindingDirectory
  String jaxbSchemaDestinationDirectory
  String jaxbProducesDirectory
  
  String xsdDirectoryForGraph

  String extension
  String removeOldOutput
  boolean header

  List bindingIncludes = []

  def dependencyGraph

  JaxbExtension(Project project) { 
    this.project = project
  }
}