package org.gradle.jacobo.schema.slurper

import groovy.util.slurpersupport.GPathResult

/**
 * Slurps and finds XSD specific data
 */
interface XsdSlurper {
  
  /**
   * Slurps the namespace of the XSD document.
   * 
   * @param slurpedDocument  slurped object to operate on
   * @param documentFile  xsd file in systetm
   * @return namespace for XSD
   */
  public String slurpNamespace(GPathResult slurpedDocument, File documentFile)
  
  /**
   * Finds defined imports on the xsd document.
   *
   * @param namespace  the namesapce of the document being operated on
   * @param importedDependencies  Set of imported dependencies this document
   *   has defined
   * @param absoluteDependencies  map of absolute dependencies to query for the
   *   imported dependencies
   * @return set of absolute Files
   */
  public Set<File> findResolvedXsdImports(String namespace, 
					  Set<String> importedDependencies,
					  Map<String, File> absoluteDependencies)

  /**
   * Finds defined includes on the xsd document.
   *
   * @param namespace  the namesapce of the document being operated on
   * @param fileName  the name of the xsd document being operated on
   * @param includedDependencies  Set of included dependencies this document
   *   has defined
   * @param absoluteDependencies  map of absolute dependencies to query for the
   *   imported dependencies
   * @return set of absolute Files
   */
  public Set<File> findResolvedXsdIncludes(String namespace, String fileName,
					   Set<String> includedDependencies,
					   Map<String, File> absoluteDependencies)
}