package org.gradle.jacobo.schema.slurper

import groovy.util.slurpersupport.GPathResult

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Slurps {@code SchemaDocument}'s dependencies
 */
class DefaultDocumentSlurper implements DocumentSlurper {
  private static final Logger log = LoggerFactory.getLogger(DefaultDocumentSlurper.class)

  /**
   * Slurps the dependencies of a {@code SchemaDocument}.
   * The dependencyType is used to define what type of dependencies
   * are being searched and can be either:
   * <ul>
   * <li> {@code wsdl imports}
   * <li> {@code xsd imports}
   * <li> {@code xsd includes}
   * </ul>
   * <p>
   * This is put in place to be able to process XSD and WSDL documents in the
   * same method.  A WSDL document defines the location of an imported WSDL with
   * attribute {@code location} instead of the XSD specs {@code schemaLocation}.
   * 
   * @param dependencies  {@link groovy.util.XmlSlurper} dependency nodes
   *   to slurp up
   * @param documentFile  slurped xsd file in file system
   * @param dependencyType the type of dependency being slurped
   * @return Set of relative path file strings, that  a {@code Schemadocument}
   *   dependends on.
   */
  @Override
  public Set<String> slurpDependencies(GPathResult dependencies,
				       File documentFile, String dependencyType) {
    def relativeDependencies = [] as Set
    def dependencySize = dependencies.size()
    if(dependencySize == 0) { 
      log.debug("'{}' has no dependencies for '{}'", documentFile, dependencyType)
      return relativeDependencies
    }
    log.debug("slurping '{}' dependencies for '{}'", dependencySize, dependencyType)
    dependencies.each { dependency ->
      if (dependencyType.equals("wsdl imports")) {
	relativeDependencies.add(dependency.@location.text())
	return
      }
      relativeDependencies.add(dependency.@schemaLocation.text())
      // TODO any warnings if there is no schemaLocation i.e. malformed xsd/wsdl file?
    }
    return relativeDependencies
  } 
}