package com.jacobo.gradle.plugins.model

import com.jacobo.gradle.plugins.util.FileHelper

abstract class DocumentSlurper {
  
  /**
   * Absolute File Object to Document slurped
   */
  File documentFile

  /**
   * Slurped Object that slurped the document file
   */
  def slurpedDocument

  /**
   * absolute File Object HashMap.  document relative dependent string
   * Key - absolute document File location on file system
   */
  Map<String, File> documentDependencies = new HashMap<String, File>()

  /**
   * resolve dependencies for this slurped Document
   */
  public abstract void resolveDocumentDependencies()

  /**
   * resolve all relative Dependency Paths from a variety of dependency lists and
   * resolve to their absolute Location on the File System, add to documentDependencies
   */
  def resolveRelativePathDependencies(List<Set<String>> relativeDependencies) {
    relativeDependencies?.each { dependencyList ->
      dependencyList.each { relativeLocation ->
	this.documentDependencies.put(relativeLocation, FileHelper.getAbsoluteSchemaLocation(relativeLocation, this.documentFile.parentFile))
      }
    }
  }

}