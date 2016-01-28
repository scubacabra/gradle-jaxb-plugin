package org.gradle.jacobo.schema

import spock.lang.Specification
import spock.lang.Unroll

import org.gradle.jacobo.schema.slurper.DocumentSlurper
import org.gradle.jacobo.schema.resolver.DocumentResolver

class WsdlDocumentSpec extends Specification {
  
  def documentSlurper = Mock(DocumentSlurper)
  def documentResolver = Mock(DocumentResolver)
  def slurped = new XmlSlurper().parseText("<wsdl></wsdl>")
  def file = new File("file.wsdl")
  def wsdlDocument

  def setup() { 
    wsdlDocument = new WsdlDocument(documentSlurper, documentResolver, file,
				    slurped)
  }

  def cleanup() { 
    wsdlDocument = null
  }

  @Unroll
  def "slurp wsdl Document with wsdl imports '#wsdlImports', xsdImports '#xsdImports' and xsd includes '#xsdIncludes'"() {
    when:
    wsdlDocument.slurp()

    then:
    1 * documentSlurper.slurpDependencies(_, wsdlDocument.documentFile,
					  "wsdl imports") >> wsdlImports
    1 * documentSlurper.slurpDependencies(_, wsdlDocument.documentFile,
					  "xsd imports") >> xsdImports
    1 * documentSlurper.slurpDependencies(_, wsdlDocument.documentFile,
					  "xsd includes") >> xsdIncludes
    1 * documentResolver.resolveRelativePaths(
      wsdlImports + xsdImports + xsdIncludes, file.parentFile) >> [:]

    where:
    wsdlImports << [["I"], ["I"], ["I"], ["I"],
		    [], [], [], []].collect { it as Set }
    xsdImports << [["A"], ["A"], [], [],
		   ["A"], ["A"], [], []].collect{ it as Set }
    xsdIncludes << [["B"], [], ["B"], [],
		    ["B"], [], ["B"], []].collect{ it as Set }
  }
}
