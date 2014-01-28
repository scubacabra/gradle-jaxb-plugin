package com.jacobo.gradle.plugins.model

import com.jacobo.gradle.plugins.util.FileHelper
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

    when: "resolve the slurper dependencies"
      slurper.resolveDocumentDependencies()

    then: "get the certain imports and include"
      slurper.xsdImports == imports as Set
      slurper.xsdIncludes == includes as Set
      slurper.documentDependencies == dependencyLocations.collect{ getFileFromResourcePath(it) } as Set

    where:
      filePath				| imports		| includes				| dependencyLocations
      "/schema/Include/OrderNumber.xsd"	| ["Product.xsd"]	| ["include.xsd", "include2.xsd"]	| ["/schema/Include/Product.xsd", "/schema/Include/include.xsd", "/schema/Include/include2.xsd"]
      "/schema/Include/Product.xsd"	| []			| []					| []
  }
}