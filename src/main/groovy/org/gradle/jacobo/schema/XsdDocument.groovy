package org.gradle.jacobo.schema

import com.google.inject.Inject
import com.google.inject.assistedinject.Assisted

import groovy.util.slurpersupport.GPathResult

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import org.gradle.jacobo.schema.slurper.DocumentSlurper
import org.gradle.jacobo.schema.slurper.XsdSlurper
import org.gradle.jacobo.schema.resolver.DocumentResolver

/**
 * Representation of a {@code XsdDocument}.
 * Slurps/parses the {@code .xsd} file and stores all of its parsed dependency
 * data.  Dependencies are usually defined  as relative paths to the current
 * directory.  For an {@code .xsd} document, there are 2 types of dependencies.
 * <ul>
 * <li>XSD imports other XSD files
 * <li>XSD includes other XSD files with the same namespace
 * </ul>
 */
class XsdDocument extends BaseSchemaDocument {
  private static final Logger log = LoggerFactory.getLogger(XsdDocument.class)

  /**
   * Slurps the namespace of the xsd file at
   * {@link #slurpedDocument}.
   */
  XsdSlurper xsdSlurper

  /**
   * Dependencies (imported) on other XSD documents via the
   * {@code xsd:import} statement.
   */
  Set<String> xsdImports

  /**
   * Dependencies (included) on XSD documents via the
   * {@code xsd:include} statement.
   */
  Set<String> xsdIncludes

  /**
   * Namespace of this document, from the {@code xsd:targetNamespace} statement.
   */
  String xsdNamespace

  /**
   * Creates a new {@code XsdDocument} instance with a document slurper,
   * document resolver, xsd slurper, document File and a slurped Document
   */
  @Inject
  XsdDocument(DocumentSlurper documentSlurper, DocumentResolver documentResolver,
	      XsdSlurper xsdSlurper, @Assisted File documentFile,
	      @Assisted GPathResult slurpedDocument) {
    super(documentSlurper, documentResolver, documentFile, slurpedDocument)
    this.xsdSlurper = xsdSlurper
  }

  /**
   * Slurps for dependency data in {@link #slurpedDocument}.
   * Slurps for xsd imports and includes dependencies, and slurps xsd namespace.
   */
  @Override
  public void slurp() {
    def imports = slurpedDocument?.import
    this.xsdImports = documentSlurper.slurpDependencies(imports, documentFile,
							"imports")
    def includes = slurpedDocument?.include
    xsdIncludes = documentSlurper.slurpDependencies(includes, documentFile,
						    "includes")
    xsdNamespace = xsdSlurper.slurpNamespace(slurpedDocument, documentFile)
    documentDependencies = documentResolver.resolveRelativePaths(
      xsdImports + xsdIncludes, documentFile.parentFile)
  }

  /**
   * Finds the absolute Files of the included dependencies defined in the XSD.
   *
   * @return the set of File objects associated with the included dependencies
   */
  def findResolvedXsdIncludes() { 
    return xsdSlurper.findResolvedXsdIncludes(xsdNamespace, documentFile.name,
					      xsdIncludes, documentDependencies)
  }

  /**
   * Finds the absolute Files of the imported dependencies defined in the XSD.
   *
   * @return the set of File objects associated with the imported dependencies
   */
  def findResolvedXsdImports() { 
    return xsdSlurper.findResolvedXsdImports(xsdNamespace, xsdImports,
					     documentDependencies)
  }
}