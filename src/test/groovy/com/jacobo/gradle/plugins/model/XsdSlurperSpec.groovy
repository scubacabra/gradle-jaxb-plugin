package com.jacobo.gradle.plugins.model

import com.jacobo.gradle.plugins.reader.DocumentReader
import com.jacobo.gradle.plugins.BaseSpecification

import spock.lang.Unroll

class XsdSlurperSpec extends BaseSpecification {
  
  def slurper = new XsdSlurper()
  
  @Unroll("resolved Dependencies for #filePath is 'imports':'#imports' and 'includes':'#includes'")
  def "resolve XSD Document Dependencies" () {
    setup: "slurp the document and populate the slurper object"
      def file = getFileFromResourcePath(filePath)
      slurper.slurpedDocument = new XmlSlurper().parse(file)
      slurper.documentFile = file
      slurper.xsdNamespace = "someNamespace"

    when: "resolve the slurper dependencies"
      slurper.resolveDocumentDependencies()

    then: "get the certain imports and include"
      slurper.xsdImports == imports.collect{ getFileFromResourcePath(it) } as Set
      slurper.xsdIncludes == includes.collect { getFileFromResourcePath(it) } as Set
      // document dependencies not gauranteed to be ordered, LinkedHashMap if want order
      slurper.documentDependencies.values().containsAll(
	dependencyLocations.collect{ getFileFromResourcePath(it) } as Set
      )

    where:
      filePath << ["/schema/Include/OrderNumber.xsd",
		   "/schema/Include/Product.xsd"]
      imports << [["/schema/Include/Product.xsd"], []]
      includes << [ ["/schema/Include/include.xsd",
		     "/schema/Include/include2.xsd"], []]
      dependencyLocations << [["/schema/Include/Product.xsd",
			      "/schema/Include/include.xsd",
			      "/schema/Include/include2.xsd"], []]
  }

  def "xsd with no target Namespace.  Namespace is populated with the documentFile name"() {
    given:
    def xsdFile = getFileFromResourcePath(xsdPath)
    slurper.slurpedDocument = new XmlSlurper().parse(xsdFile)
    slurper.documentFile = xsdFile

    when:
    slurper.slurpNamespace()

    then:
    slurper.xsdNamespace == xsdFile.name

    where:
    xsdPath << ["/schema/no-namespace.xsd", "/schema/living-room-no-namespace.xsd"]
  }
}