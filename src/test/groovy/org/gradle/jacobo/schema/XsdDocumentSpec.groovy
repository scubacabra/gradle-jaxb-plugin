package org.gradle.jacobo.schema

import spock.lang.Specification
import spock.lang.Unroll

import org.gradle.jacobo.schema.slurper.XsdSlurper
import org.gradle.jacobo.schema.slurper.DocumentSlurper
import org.gradle.jacobo.schema.resolver.DocumentResolver

class XsdDocumentSpec extends Specification {
  
  def xsdSlurper = Mock(XsdSlurper)
  def documentSlurper = Mock(DocumentSlurper)
  def documentResolver = Mock(DocumentResolver)
  def slurped = new XmlSlurper().parseText("<xsd></xsd>")
  def file = new File("/file.xsd")
  def xsdDocument

  def setup() { 
    xsdDocument = new XsdDocument(documentSlurper, documentResolver, xsdSlurper,
				  file, slurped)
  }

  def cleanup() { 
    xsdDocument = null
  }

  @Unroll
  def "slurp xsd Document with imports '#imports' and includes '#includes'"() {
    when:
    xsdDocument.slurp()

    then:
    1 * documentSlurper.slurpDependencies(_, xsdDocument.documentFile, "imports") >> imports
    1 * documentSlurper.slurpDependencies(_, xsdDocument.documentFile, "includes") >> includes
    1 * xsdSlurper.slurpNamespace(slurped, file)
    1 * documentResolver.resolveRelativePaths(
      imports + includes, file.parentFile) >> [:]

    where:
    imports << [["A"], ["A"], [], []].collect{ it as Set }
    includes << [["B"], [], ["B"], []].collect{ it as Set }
  }

  def "find xsd Document resolved imported dependencies"() {
    setup:
    xsdDocument.with { 
      xsdNamespace = "some-namespace"
      xsdImports = [] as Set
      xsdIncludes = [] as Set
      documentDependencies = [:]
    }

    when:
    xsdDocument.findResolvedXsdImports()
    
    then:
    1 * xsdSlurper.findResolvedXsdImports(xsdDocument.xsdNamespace,
					  xsdDocument.xsdImports,
					  xsdDocument.documentDependencies)
  }

  def "find xsd Document resolved included dependencies"() { 
    setup:
    xsdDocument.with { 
      xsdNamespace = "some-namespace"
      xsdImports = [] as Set
      xsdIncludes = [] as Set
      documentDependencies = [:]
    }

    when:
    xsdDocument.findResolvedXsdIncludes()
    
    then:
    1 * xsdSlurper.findResolvedXsdIncludes(xsdDocument.xsdNamespace,
					   "file.xsd",
					   xsdDocument.xsdIncludes,
					   xsdDocument.documentDependencies)
  }
}