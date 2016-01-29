package org.openrepose.gradle.plugins.jaxb.schema.factory

import spock.lang.Specification
import spock.lang.Unroll

import org.openrepose.gradle.plugins.jaxb.schema.SchemaDocumentFactory
import org.openrepose.gradle.plugins.jaxb.schema.BaseSchemaDocument

class DefaultDocumentFactorySpec extends Specification {
  
  def schemaDocFactory = Mock(SchemaDocumentFactory)
  def schemaDoc = Mock(BaseSchemaDocument)
  def docFactory = new DefaultDocumentFactory(schemaDocFactory)

  def getFileFromResourcePath(path) {
    return new File(this.getClass().getResource(path).toURI())
  }

  @Unroll
  def "create a document from '#file'"() {
    when:
    def result = docFactory.createDocument(file)

    then:
    xsdCalls * schemaDocFactory.createXsdDocument(_, _) >> schemaDoc
    wsdlCalls * schemaDocFactory.createWsdlDocument(_, _) >> schemaDoc
    xsdCalls * schemaDoc.slurp()
    wsdlCalls * schemaDoc.slurp()

    where:
    file << ["/schema/xsd-no-namespace.xsd", "/wsdl/wsdl-no-xsd.wsdl"].collect {
      getFileFromResourcePath(it)
    }
    xsdCalls << [1, 0]
    wsdlCalls << [0, 1]
  }

  def "Try to process something other than a wsdl or xsd file"() {
    when:
    docFactory.createDocument(file)

    then:
    thrown(Exception)

    where:
    file = getFileFromResourcePath("/test-file.txt")
  }
}