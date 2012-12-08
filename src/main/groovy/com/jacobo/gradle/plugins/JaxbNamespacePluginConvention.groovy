package com.jacobo.gradle.plugins

/**
 * @author djmijares
 * Created: Tue Dec 04 09:01:34 EST 2012
 */
class JaxbNamespacePluginConvention {
  String xsdDir
  
  def jaxbNamespace(Closure closure) { 
    closure.delegate = this
    closure()
  }
}