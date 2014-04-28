package org.gradle.jacobo.plugins.structures

import org.gradle.jacobo.schema.BaseSchemaDocument

import org.gradle.api.logging.Logging
import org.gradle.api.logging.Logger

/**
 * Model that contains a particular set of unique data for a Namespace for jaxb generation
 * @author jacobono
 * @date 12/19/12
 */
class NamespaceData { 
  static final Logger log = Logging.getLogger(NamespaceData.class)

  /**
   * the namespace for this group of file(s)
   */
  def namespace

  // list of Documents with this namespace
  def documents

  boolean hasDependencies = false

  def externalDependencies = [] as Set

  public NamespaceData() { }
  
  public NamespaceData(String namespace) {
    this.namespace = namespace
  }

  /**
   * basic constructor, need a namespace and a list of slurped documents
   */
  public NamespaceData(String namespace, List<BaseSchemaDocument> documents) {
    this.namespace = namespace
    this.documents = documents
  }
  
  def String toString() { 
    return namespace
  }
}