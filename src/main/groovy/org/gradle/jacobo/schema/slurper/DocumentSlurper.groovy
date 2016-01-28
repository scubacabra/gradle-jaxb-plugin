package org.gradle.jacobo.schema.slurper

import groovy.util.slurpersupport.GPathResult

/**
 * Slurps {@code SchemaDocument}'s dependencies.
 */
interface DocumentSlurper {
  
  /**
   * Slurps the dependencies of a {@code SchemaDocument}.
   * 
   * @param dependencies  {@link groovy.util.XmlSlurper} dependency nodes
   *   to slurp up
   * @param documentFile  slurped xsd file in file system
   * @param dependencyType the type of dependency being slurped
   * @return Set of relative path file strings.
   */
  public Set<String> slurpDependencies(GPathResult dependencies,
				       File documentFile, String dependencyType)
}