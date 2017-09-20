package org.openrepose.gradle.plugins.jaxb.resolver

import spock.lang.Unroll

import org.openrepose.gradle.plugins.jaxb.fixtures.DocumentFixture

class NamespaceResolverSpec extends DocumentFixture {
  
  def resolver = new NamespaceResolver()

  @Unroll
  def "resolve '#docInfo.size()' docs into '#uniques' unique namespaces"() {
    given:
    def documents = docInfo.collect { k, v -> 
      createXsdDocument(new File(k), v)
    }

    when:
    def result = resolver.resolve(documents)

    then:
    result.size() == uniques
    result.namespace.containsAll(docInfo.values() as Set)
    multipleDocInfo.each { k,v ->
      result.find{ it.namespace == k}.documents.size() == v
    }

    where:
    // filename:namespace
    docInfo = ["xsd1":"1", "xsd2":"1", "xsd3":"2",
	       "xsd4":"3", "xsd5":"3",, "xsd6":"3",
	       "xsd7":"4", "xsd8":"5"]
    // namespace:numDocuments
    multipleDocInfo = ["1":2, "3":3]
    uniques = 5
  }
}