package org.gradle.jacobo.plugins.xsd

import org.gradle.jacobo.schema.BaseSchemaDocument

import org.gradle.api.logging.Logging
import org.gradle.api.logging.Logger

/**
 * Container to hold documents that contain the same namespace definition.
 * Makes certain data available based on these documents like if this namespace
 * has dependencies, or which external dependencies (by namespace depended on)
 * are defined in these documents.
 */
class XsdNamespace { 
  static final Logger log = Logging.getLogger(XsdNamespace.class)

  /**
   * Namespace defined by {@code targetNamespace} common to all documents.
   */
  def namespace

  /**
   * List of documents all containing the same namepsace.
   */
  def documents

  /**
   * Flags this object as having dependencies.
   */
  boolean hasDependencies = false

  /**
   * Set of namespaces defined by {@code targetNamespace} in xsd files that are
   * not found in current common parent directory.
   */
  def externalDependencies = [] as Set

  /**
   * Creates this container.
   */
  public XsdNamespace() { }
  
  /**
   * Creates this container.
   *
   * @param namespace  namespace for this container
   */
  public XsdNamespace(String namespace) {
    this.namespace = namespace
  }

  /**
   * Creates this container.
   *
   * @param namespace  namespace for this container
   * @param documents  documents that contain the same unique namespace
   */
  public XsdNamespace(String namespace, List<BaseSchemaDocument> documents) {
    this.namespace = namespace
    this.documents = documents
  }
  
  /**
   * Converts this object to human readable string.
   */
  def String toString() { 
    return namespace
  }
}