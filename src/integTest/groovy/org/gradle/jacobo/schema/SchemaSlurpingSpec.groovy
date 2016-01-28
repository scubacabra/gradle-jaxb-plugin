package org.gradle.jacobo.schema

import spock.lang.Specification
import spock.guice.UseModules
import com.google.inject.Inject

import org.gradle.jacobo.schema.guice.DocSlurperModule
import org.gradle.jacobo.schema.WsdlDocument
import org.gradle.jacobo.schema.XsdDocument
import org.gradle.jacobo.schema.factory.DocumentFactory

@UseModules(DocSlurperModule)
class SchemaSlurpingSpec extends Specification {

  @Inject
  DocumentFactory documentFactory


  def getFileFromResourcePath(path) {
    return new File(this.getClass().getResource(path).toURI())
  }

  def "Full Wsdl processing on file"() {
    given:
    def file = getFileFromResourcePath("/wsdl/IBMSample.wsdl")
    println file

    when: "create the document"
    def result = documentFactory.createDocument(file)

    then:
    result instanceof WsdlDocument
    result.wsdlImports == ["./abstract.wsdl"] as Set
    result.xsdImports == ["../schema/some-schema.xsd",
			  "../schema/other-schema.xsd"] as Set
    result.xsdIncludes == ["../schema/some-include.xsd",
			   "../schema/some-other-include.xsd"] as Set
    result.documentDependencies.size() == 5
    [result.xsdImports, result.xsdIncludes, result.wsdlImports].each { dependencies ->
      dependencies.each { 
	result.documentDependencies.containsKey(it)
      }
    }
  }

  def "Full XSD Processing on file"() {
    given:
    def file = getFileFromResourcePath("/schema/MSDN/PurchaseOrder.xsd")
    println file
    when: "create the document"
    def result = documentFactory.createDocument(file)

    then:
    result instanceof XsdDocument
    result.xsdImports == ["../schema/imported.xsd",
			  "../schema/other-imported.xsd"] as Set
    result.xsdIncludes == ["../schema/included.xsd",
			   "../schema/other-included.xsd"] as Set
    result.xsdNamespace == "http://tempuri.org/PurchaseOrderSchema.xsd"
    result.documentDependencies.size() == 4
    [result.xsdImports, result.xsdIncludes].each { dependencies ->
      dependencies.each { 
	result.documentDependencies.containsKey(it)
      }
    }

    when: "find the resolved files"
    def resolvedImports = result.findResolvedXsdImports()
    def resolvedIncludes = result.findResolvedXsdIncludes()

    then:
    resolvedImports.size() == 2
    resolvedIncludes.size() == 2
  }
}