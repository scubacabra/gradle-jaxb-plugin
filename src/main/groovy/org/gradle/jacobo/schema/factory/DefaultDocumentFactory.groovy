package org.gradle.jacobo.schema.factory

import com.google.inject.Inject

import org.gradle.jacobo.schema.BaseSchemaDocument
import org.gradle.jacobo.schema.SchemaDocumentFactory

/**
 * Handles the creation of a {@code BaseSchemaDocument}.
 */
class DefaultDocumentFactory implements DocumentFactory {
  
  /**
   *  Guice's Assisted Inject Factory Interface
   */
  SchemaDocumentFactory schemaDocumentFactory

  /**
   *  Create this {@code DocumentFactory}.
   *
   *  @param schemaDocFactory  Guice's Interface to create with Assisted inject.
   */
  @Inject
  DefaultDocumentFactory(SchemaDocumentFactory schemaDocFactory) {
    schemaDocumentFactory = schemaDocFactory
  }
  
  /**
   * Creates a {@code BaseSchemaDocument}.
   * Returns a {@link WsdlDocument} if the file has extension of {@code .wsdl} and
   * Returns a {@link XsdDocument} if the file has extension of {@code .xsd}.
   * 
   * @param document  absolute path File to schema document
   * @return {@link org.gradle.jacobo.schema.BaseSchemaDocument}
   * @throws {@link java.io.Exception}  if file has extension other than
   * {@code .wsdl} or {@code .xsd}
   */
  public BaseSchemaDocument createDocument(File document) {
    def slurped = new XmlSlurper().parse(document)
    // is xsd file?
    if (document.name[-3..-1] == "xsd") {
      def xsdDoc = schemaDocumentFactory.createXsdDocument(document, slurped)
      xsdDoc.slurp()
      return xsdDoc
    }
    // is wsdl file?
    if (document.name[-4..-1] == "wsdl") {
      def wsdlDoc = schemaDocumentFactory.createWsdlDocument(document, slurped)
      wsdlDoc.slurp()
      return wsdlDoc
    }

    thrown new Exception("trying to parse $document -- Can only parse .wsdl or .xsd files")
  }
}