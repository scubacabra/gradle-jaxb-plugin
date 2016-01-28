package org.gradle.jacobo.schema

import groovy.util.slurpersupport.GPathResult

import com.google.inject.Inject
import com.google.inject.assistedinject.Assisted

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import org.gradle.jacobo.schema.slurper.DocumentSlurper
import org.gradle.jacobo.schema.resolver.DocumentResolver

/**
 * Representation of a {@code WsdlDocument}.
 * Slurps/parses the {@code .wsdl} file and stores all of its parsed dependency
 * data.  Dependencies are usually defined  as relative paths to the current
 * directory.  For a {@code .wsdl} document, there are 3 types of dependencies.
 * <ul>
 * <li>WSDL imports other WSDL's
 * <li>WSDL imports XSD files
 * <li>WSDL includes XSD files with its own namespace
 * </ul>
 */
class WsdlDocument extends BaseSchemaDocument {
  private static final Logger log = LoggerFactory.getLogger(WsdlDocument.class)

  /**
   * Dependencies(set of strings) on other WSDL documents via the
   * {@code wsdl:import} statement.
   */
  def wsdlImports


  /**
   * Dependencies(set of strings) (imported) on XSD documents via the
   * {@code xsd:import} statement.
   */
  def xsdImports

  /**
   * Dependencies(set of strings) (included) on XSD documents via the
   * {@code xsd:include} statement.
   */
  def xsdIncludes

  /**
   * Creates a new {@code WsdlDocument} instance with a document slurper,
   * document resolver, document File and a slurped Document
   */
  @Inject
  WsdlDocument(DocumentSlurper documentSlurper, DocumentResolver documentResolver,
	       @Assisted File documentFile,
	       @Assisted GPathResult slurpedDocument) {
    super(documentSlurper, documentResolver, documentFile, slurpedDocument)
  }

  /**
   * Slurps for dependency data in {@link #slurpedDocument}.
   * Slurps for wsdl imports and other wsdl'ss, imports on xsd's, and any included
   * xsds.
   */
  @Override
  public void slurp() {
    def importedWsdl = slurpedDocument?.import
    wsdlImports = documentSlurper.slurpDependencies(importedWsdl, documentFile,
						    "wsdl imports")
    def importedXsds = slurpedDocument?.types?.schema?.import
    xsdImports = documentSlurper.slurpDependencies(importedXsds, documentFile,
						   "xsd imports")
    def inludedXsds = slurpedDocument?.types?.schema?.include
    xsdIncludes = documentSlurper.slurpDependencies(inludedXsds, documentFile,
						    "xsd includes")
    documentDependencies = documentResolver.resolveRelativePaths(
      wsdlImports + xsdImports + xsdIncludes, documentFile.parentFile)
  }
}