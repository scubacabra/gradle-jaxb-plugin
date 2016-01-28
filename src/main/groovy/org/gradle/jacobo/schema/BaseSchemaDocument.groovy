package org.gradle.jacobo.schema

import groovy.util.slurpersupport.GPathResult

import com.google.inject.Inject

import org.gradle.jacobo.schema.resolver.DocumentResolver
import org.gradle.jacobo.schema.slurper.DocumentSlurper

/**
 * Base representation of a {@code SchemaDocument} for Xsd's and Wsdl's.
 * Contains instance variables and delegate services that are common to the
 * processing of Xsd's and Wsdl's.
 */
abstract class BaseSchemaDocument implements SchemaDocument {

  /**
   * The absolute path File of the slurped schema document
   */
  File documentFile

  /**
   * The slurped schema Document @see {@link groovy.util.XmlSlurper}
   */
  GPathResult slurpedDocument

  /**
   * Map of document dependencies in the {@link #documentFile} defined by an
   * {@code import} or {@code include} statement.  Key of path specified in the
   * {@link #documentFile} with a corresponding value of the absolute path of
   * that dependency
   */
  Map<String, File> documentDependencies

  /**
   * Slurps the {@link #slurpedDocument}'s dependencies
   */
  DocumentSlurper documentSlurper

  /**
   * Resolves relative paths to absolute paths for dependencies
   */
  DocumentResolver documentResolver

  /**
   * Creates a new {@code BaseSchemaDocument} instance with a document slurper,
   * document resolver, document File and a slurped Document
   */
  @Inject
  BaseSchemaDocument(DocumentSlurper documentSlurper,
		     DocumentResolver documentResolver, File documentFile,
		     GPathResult slurpedDocument) {
    this.documentSlurper = documentSlurper
    this.documentResolver = documentResolver
    this.documentFile = documentFile
    this.slurpedDocument = slurpedDocument
  }

  /**
   * Slurps for data in {@link #slurpedDocument}
   */
  public abstract void slurp()
}