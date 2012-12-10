package com.jacobo.gradle.plugins

import org.gradle.api.Project
/**
 * @author djmijares
 * Created: Tue Dec 04 09:01:34 EST 2012
 */
class JaxbNamespacePluginConvention {
   private Project project
  
  String xsdDir

  String destDir = "src/main/java"
  String extension = 'true'
  String removeOldOutput = 'yes'
  boolean header = false
  def target
  String producesDir = destDir
  String bindingDir
  String bindingIncludes
  String episodeDir
  
  JaxbNamespacePluginConvention(Project project) { 
    this.project = project
  }

  def jaxbNamespace(Closure closure) { 
    closure.delegate = this
    closure()
  }
}