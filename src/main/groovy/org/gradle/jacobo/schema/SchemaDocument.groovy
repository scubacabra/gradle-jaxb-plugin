package org.gradle.jacobo.schema

/**
 * Slurps a schema document({@code .xsd/.wsdl}).
 * The implementing class should have an instnace field with some
 * parsed/slurped representation of a physical schema file({@code .xsd/.wsdl})
 * to slurp.
 * @see groovy.util.XmlSlurper
 */
interface SchemaDocument {
  
  /**
   * Slurps this {@code SchemaDocument}.
   */
  public void slurp()
}