package org.gradle.jacobo.plugins.resolver

import org.gradle.api.logging.Logging
import org.gradle.api.logging.Logger

import org.gradle.jacobo.schema.BaseSchemaDocument
import org.gradle.jacobo.plugins.xsd.XsdNamespace

/**
 * Groups some xsd files by their unique namespaces.
 */
class NamespaceResolver {
  static final Logger log = Logging.getLogger(NamespaceResolver.class)
  
  /**
   * Resolves a group of documents into a unique namespace container.
   * 
   * @param documents  xsd files to group
   * @return a list of namespace objects with a unique namespace and associated
   *         documents
   */
  public List<XsdNamespace> resolve(def documents) {
    log.info("resolving(grouping) '{}' documents by their unique namespaces",
	     documents.size())
    def namespaces = []
    documents.each { document ->
      def namespace = namespaces.find { it.namespace == document.xsdNamespace }
      if (namespace) {
	namespace.documents << document
	return true
      }
      namespace = new XsdNamespace(document.xsdNamespace, [document])
      namespaces << namespace
    }
    return namespaces
  }
}