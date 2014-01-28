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
   * absolute File Object Set of document dependencies
   */
  AbstractSet<File> documentDependencies = new HashSet<File>()

  /**
   * resolve dependencies for this slurped Document
   */
  public abstract void resolveDocumentDependencies()

  /**
   * resolve all relative Dependency Paths from a variety of dependency lists and
   * resolve to their absolute Location on the File System, add to documentDependencies
   */
  def resolveRelativePathDependencies(List<Set<File>> relativeDependencies) { 
    relativeDependencies?.each { dependencyList ->
      dependencyList.each { relativeLocation ->
	this.documentDependencies.add(FileHelper.getAbsoluteSchemaLocation(relativeLocation, this.documentFile.parentFile))
      }
    }
  }

}