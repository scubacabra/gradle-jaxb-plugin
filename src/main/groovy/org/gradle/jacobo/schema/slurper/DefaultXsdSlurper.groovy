package org.gradle.jacobo.schema.slurper

import groovy.util.slurpersupport.GPathResult

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Slurps and finds XSD specific data
 */
class DefaultXsdSlurper implements XsdSlurper {
  private static final Logger log = LoggerFactory.getLogger(DefaultXsdSlurper.class)

  /**
   * Slurps the namespace of the XSD document.
   * If the namespaces is not defined via {@code xsd:targetNamespace} in the
   * root attributes, the namespace will be defined as the
   * {@code documentFile.fileName} of the xsd documents.  A warning is issued
   * when this happens, but nothing to stop the execution.
   * 
   * @param slurpedDocument  slurped object to operate on
   * @param documentFile  xsd file in systetm
   * @return namespace for XSD
   */
  @Override
  public String slurpNamespace(GPathResult slurpedDocument, File documentFile) {
    def namespace = slurpedDocument?.@targetNamespace.text() 
    if (namespace) return namespace
    // TODO try to find xsd:tns as well.
    def warning = ["There is no targetNamespace attribute for file '{}' ",
		   "(assigning filname as namespace to it). A Schema ",
		   "should ALWAYS include a targetNamespace attribute at ",
		   "its root element.  No targetNamespace are categorized ",
		   "as using the Chameleon Design Pattern, which is not ",
		   "an advisable pattern, AT ALL!"]
    def warningText = ""
    warning.each { warningText += it }
    log.warn(warningText, documentFile)
    return documentFile.name
  }
  
  /**
   * Finds defined imports on the xsd document.
   * In a {@link org.gradle.jacobo.schema.XsdDocument}, the imports and includes
   * dependencies are saved as sets of Strings (which can be relative paths).
   * There is also a map of <b>All</b> relative dependencies to their absolute
   * file objects.  Resolving the imports dependencies is merely a query on the
   * maps keys for every relative import.
   *
   * @param namespace  the namesapce of the document being operated on
   * @param importedDependencies  Set of imported dependencies this document
   *   has defined
   * @param absoluteDependencies  map of absolute dependencies to query for the
   *   imported dependencies
   * @return set of absolute Files
   */
  @Override
  public Set<File> findResolvedXsdImports(
    String namespace, Set<String> importedDependencies,
    Map<String, File> absoluteDependencies) {
    log.debug("'{}' has relative imports '{}' that correspond to '{}'",
	      namespace, importedDependencies, absoluteDependencies.values())
    def resolvedImports = resolveDependencies(importedDependencies,
						   absoluteDependencies)
    return resolvedImports as Set
  }

  /**
   * Finds defined imports on the xsd document.
   * In a {@link org.gradle.jacobo.schema.XsdDocument}, the imports and includes
   * dependencies are saved as sets of Strings (which can be relative paths).
   * There is also a map of <b>All</b> relative dependencies to their absolute
   * file objects.  Resolving the includes dependencies is merely a query on
   * the maps keys for every relative import.
   *
   * @param namespace  the namesapce of the document being operated on
   * @param fileName  the name of the xsd document being operated on
   * @param includedDependencies  Set of included dependencies this document
   *   has defined
   * @param absoluteDependencies  map of absolute dependencies to query for the
   *   imported dependencies
   * @return set of absolute Files
   */
  @Override
  public Set<File> findResolvedXsdIncludes(
    String namespace, String fileName, Set<String> includedDependencies,
    Map<String, File> absoluteDependencies) {
    log.debug("'{}'@'{}' has relative includes '{}' that correspond to '{}'",
	      namespace, fileName, includedDependencies,
	      absoluteDependencies.values())
    def resolvedIncludes = resolveDependencies(includedDependencies,
					       absoluteDependencies)
    return resolvedIncludes as Set
  }
  
  /**
   * Generally resolves a set of dependencies for the {@code XsdDocument}.
   * Resolves a set of dependencies against a map that has those same dependencies
   * as keys and the absolute Files as values.
   * <p>
   * If the dependency is not found in the map, a warning is logged and {@code null} is
   * inserted into the returned set.
   *
   * @param dependencies  dependency strings
   * @param absoluteDependencies  map of keys and values to query against
   * @return set of absolute Files that correspond to the {@code XsdDocument}
   *   dependencies
   */
  def resolveDependencies(Set<String> dependencies, Map<String, File> absoluteDependencies) {
    def resolvedDependencies = dependencies.collect { dependency ->
      // this should NEVER happen, because the relative deps are resolved to
      // the absolute dependency map with the relative deps as the key
      // But just as a precaution, throw a warning if this happens, which it shouldnt.
      // TODO take out, and if take out, make a simple closure instead to pass around?
      if(!absoluteDependencies.containsKey(dependency)) {
	log.warn("THE DEPENDENCY '{}' ISN'T IN THE ABSOLUTE FILE HASHMAP '{}'",
		 dependency, absoluteDependencies)
	return null
      }
      absoluteDependencies.get(dependency)
    }
    return resolvedDependencies
  }
}
