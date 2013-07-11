package com.jacobo.gradle.plugins.structures

import org.gradle.api.logging.Logging
import org.gradle.api.logging.Logger
import com.jacobo.gradle.plugins.util.ListUtil
import com.jacobo.gradle.plugins.util.FileHelper
import com.jacobo.gradle.plugins.reader.DocumentReader

/**
 * This class resolves all external namespaces, traversing all schemaLocations and gethering all the imported namespaces
 *
 * @author Daniel Mijares
 * @version 1.0
 */
class ExternalNamespaceResolver { 
  private static final Logger log = Logging.getLogger(ExternalNamespaceResolver.class)

  /**
   * external namespace Data to resolve
   */
  private ExternalNamespaceMetaData externalImport

  /**
   * List of externally imported Namespace data that is returned from method resolve
   */
  List<ExternalNamespaceMetaData> externalImportedNamespaces = []

  /**
   * a list of schema locations left to parse
   */
  List<ExternalNamespaceMetaData> namespacesToParse = []

  /**
   * construct this object with the External Namespace Meta Data emd
   */
  public ExternalNamespaceResolver(ExternalNamespaceMetaData emd) { 
    externalImport = emd
  }

  /**
   * resolves the external imported namespaces starting at
   * @see #externalImportStartLocation
   * While there are files in
   * @see #namespacesToParse
   * keep slurping documents and gather schema locations
   * @return a list of #ExternalNamespaceMetaData to add to the external namespace that is being resolved
   */
  List<ExternalNamespaceMetaData> resolveExternalImportedNamespaces() {
    log.info("resolving external dependencies starting at {}", externalImport.externalSchemaLocation)
    namespacesToParse << externalImport
    while(namespacesToParse) {
      def namespace = namespacesToParse.pop()
      log.debug("popping {} from namespacesToParse list", namespace)
      namespace.externalSlurper = DocumentReader.slurpDocument(namespace.externalSlurper.document)
      def externalDependencies = namespace.externalSlurper.obtainExternalDependencies()
      externalImport.importedNamespaces.addAll(externalDependencies)
      externalDependencies.each {
        ListUtil.addElementToList(namespacesToParse, it)
      }
    }
    log.debug("returning the imported namespaces {}", externalImportedNamespaces)
    return externalImport.importedNamespaces
  }
}