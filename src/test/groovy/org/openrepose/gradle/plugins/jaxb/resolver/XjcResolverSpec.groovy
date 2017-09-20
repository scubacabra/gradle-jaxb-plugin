package org.openrepose.gradle.plugins.jaxb.resolver

import org.gradle.api.GradleException
import org.openrepose.gradle.plugins.jaxb.fixtures.DocumentFixture
import org.openrepose.gradle.plugins.jaxb.xsd.XsdNamespace

class XjcResolverSpec extends DocumentFixture {
  def resolver = new XjcResolver()

  def namespace = new XsdNamespace()
  def sameNamespace = "same-namespace"
  def mainXsdFile = new File("main-document.xsd")
  def doc1 = createXsdDocument(mainXsdFile, sameNamespace)

  def setup() {
    namespace.namespace = sameNamespace
  }

  def "namespace has 2 documents, 1 includes the other, resolve to 1 file for xjc to parse"() {
    given:
    doc1.with {
      xsdIncludes = includes
      includes.each { documentDependencies[it] = new File(it) }
    }
    def doc2 = createXsdDocument(new File("xsd2.xsd"), sameNamespace)
    namespace.documents = [doc1, doc2]

    and: "return values from mock"
    xsdSlurper.findResolvedXsdIncludes(
      "same-namespace", "main-document.xsd",
      doc1.xsdIncludes, doc1.documentDependencies) >> includes.collect { new File(it) }
    xsdSlurper.findResolvedXsdIncludes(
      "same-namespace", "xsd2.xsd",
      doc2.xsdIncludes, doc2.documentDependencies) >> []

    when: "resolve these namespace files for xjc"
    def result = resolver.resolve(namespace)

    then:
    result.size() == 1
    result == [mainXsdFile] as Set

    where:
    includes = ["xsd2.xsd"] as Set
  }

  def "namespace has 3 documents -- main includes an xsd, and the included xsd includes another 1, only one xsd should be resolved to parse with xjc"() {
    given:
    doc1.with {
      xsdIncludes = includes
      includes.each { documentDependencies[it] = new File(it) }
    }
    def doc2 = createXsdDocument(new File("xsd2.xsd"), sameNamespace)
    doc2.with {
      xsdIncludes = includes2
      includes2.each { documentDependencies[it] = new File(it) }
    }
    def doc3 = createXsdDocument(new File("xsd3.xsd"), sameNamespace)
    namespace.documents = [doc1, doc2, doc3]

    and: "return values from mock"
    xsdSlurper.findResolvedXsdIncludes(
      "same-namespace", "main-document.xsd",
      doc1.xsdIncludes, doc1.documentDependencies) >> includes.collect { new File(it) }
    xsdSlurper.findResolvedXsdIncludes(
      "same-namespace", "xsd2.xsd",
      doc2.xsdIncludes, doc2.documentDependencies) >> includes2.collect { new File(it) }
    xsdSlurper.findResolvedXsdIncludes(
      "same-namespace", "xsd3.xsd",
      doc3.xsdIncludes, doc3.documentDependencies) >> []

    when: "resolve these namespace files for xjc"
    def result = resolver.resolve(namespace)

    then:
    result.size() == 1
    result == [mainXsdFile] as Set

    where:
    includes = ["xsd2.xsd"] as Set
    includes2 = ["xsd3.xsd"] as Set
  }

  def "namespace has 3 documents -- main includes 2 documents, and doc1 includes doc2 as well -- should resolve to 1 file for xjc to parse"() {
    given:
    doc1.with {
      xsdIncludes = includes
      includes.each { documentDependencies[it] = new File(it) }
    }
    def doc2 = createXsdDocument(new File("xsd2.xsd"), sameNamespace)
    doc2.with {
      xsdIncludes = includes2
      includes2.each { documentDependencies[it] = new File(it) }
    }
    def doc3 = createXsdDocument(new File("xsd3.xsd"), sameNamespace)
    namespace.documents = [doc1, doc2, doc3]
    
    and: "return values from mock"
    xsdSlurper.findResolvedXsdIncludes(
      "same-namespace", "main-document.xsd",
      doc1.xsdIncludes, doc1.documentDependencies) >> includes.collect { new File(it) }
    xsdSlurper.findResolvedXsdIncludes(
      "same-namespace", "xsd2.xsd",
      doc2.xsdIncludes, doc2.documentDependencies) >> includes2.collect { new File(it) }
    xsdSlurper.findResolvedXsdIncludes(
      "same-namespace", "xsd3.xsd",
      doc3.xsdIncludes, doc3.documentDependencies) >> []

    when: "resolve these namespace files for xjc"
    def result = resolver.resolve(namespace)

    then:
    result.size() == 1
    result == [mainXsdFile] as Set

    where:
    includes = ["xsd2.xsd", "xsd3.xsd"] as Set
    includes2 = ["xsd3.xsd"] as Set
  }

  def "resolving namespace files reveals circular dependency -- all files in the namespace include each other -- exception is thrown to alert user"() {
    given:
    doc1.with {
      xsdIncludes = includes
      includes.each { documentDependencies[it] = new File(it) }
    }
    def doc2 = createXsdDocument(new File("xsd2.xsd"), sameNamespace)
    doc2.with {
      xsdIncludes = includes2
      includes2.each { documentDependencies[it] = new File(it) }
    }
    def doc3 = createXsdDocument(new File("xsd3.xsd"), sameNamespace)
    doc3.with {
      xsdIncludes = includes3
      includes3.each { documentDependencies[it] = new File(it) }
    }
    namespace.documents = [doc1, doc2, doc3]

    and: "return values from mock"
    xsdSlurper.findResolvedXsdIncludes(
      "same-namespace", "main-document.xsd",
      doc1.xsdIncludes, doc1.documentDependencies) >> includes.collect { new File(it) }
    xsdSlurper.findResolvedXsdIncludes(
      "same-namespace", "xsd2.xsd",
      doc2.xsdIncludes, doc2.documentDependencies) >> includes2.collect { new File(it) }
    xsdSlurper.findResolvedXsdIncludes(
      "same-namespace", "xsd3.xsd",
      doc3.xsdIncludes, doc3.documentDependencies) >> includes3.collect { new File(it) }

    when: "resolve these namespace files for xjc"
    def result = resolver.resolve(namespace)
    
    then:
    thrown GradleException

    where:
    includes = ["xsd2.xsd"] as Set
    includes2 = ["xsd3.xsd"] as Set
    includes3 = ["main-document.xsd"] as Set
  }
}