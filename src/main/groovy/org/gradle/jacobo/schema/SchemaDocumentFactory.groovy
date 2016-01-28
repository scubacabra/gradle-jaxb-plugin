package org.gradle.jacobo.schema

import com.google.inject.name.Named
import groovy.util.slurpersupport.GPathResult

/**
 * Interface for Guice
 * ({@link "http://google-guice.googlecode.com/svn/trunk/javadoc/"}) to create 
 * {@code SchemaDocument}'s through Assisted Inject.
 */
interface SchemaDocumentFactory {

  /**
   * Creates a {@code WsdlDocument} through Guice's 
   * {@link com.google.inject.assistedinject.Assisted} Inject.
   *
   * @param wsdlFile  Absolute Path File of WSDL
   * @param slurpedWsdl  slurped wsdl from {@link groovy.util.XmlSlurper}
   * @return {@code BaseSchemaDocument}
   */
  @Named("wsdl") BaseSchemaDocument createWsdlDocument(File wsdlFile, GPathResult slurpedWsdl)

  //TODO --> this should be slurpedXsd
  /**
   * Creates a {@code WsdlDocument} through Guice's 
   * {@link com.google.inject.assistedinject.Assisted} Inject.
   *
   * @param xsdFile  Absolute Path File of XSD
   * @param slurpedWsdl  slurped xsd from {@link groovy.util.XmlSlurper}
   * @return {@code BaseSchemaDocument}
   */
  @Named("xsd") BaseSchemaDocument createXsdDocument(File xsdFile, GPathResult slurpedWsdl)
}