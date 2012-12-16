package com.jacobo.gradle.plugins

import org.gradle.api.Project
import com.jacobo.gradle.plugins.structures.OrderGraph

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
  List episodeBindings = []

  OrderGraph dependencyGraph

  JaxbExtension(Project project) { 
    this.project = project
  }
}