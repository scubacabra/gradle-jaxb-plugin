package org.gradle.jacobo.plugins.resolver

import org.gradle.jacobo.schema.factory.DocumentFactory
import org.gradle.jacobo.plugins.fixtures.DocumentFixture
import org.gradle.jacobo.plugins.xsd.XsdNamespace

class ExternalDependencyResolverSpec extends DocumentFixture {

  def factory = Stub(DocumentFactory) 
  def resolver = new ExternalDependencyResolver(factory)

  def xsdNamespaces = ["xsd1", "xsd2", "xsd3", "xsd4", "xsd5",
		       "xsd6", "xsd7", "xsd8", "xsd9"]
  def externalNamespaces = ["e1", "e2", "e3", "e4", "e5", "e6", "e7", "e8", "e9"]

  def documents
  def namespaces

  def setup() {
    def allNamespaces = xsdNamespaces + externalNamespaces
    // create XsdDocuments with the file name the same as the namespace
    documents = allNamespaces.collect {
      createXsdDocument(new File(it + ".xsd"), it)
    }
    // create namespaces and add 1 document per namespace for xsdNamespaces
    namespaces = xsdNamespaces.collect { new XsdNamespace(it) }
    xsdNamespaces.each { ns ->
      def xsd = documents.find { doc -> doc.xsdNamespace == ns }
      def namespace = namespaces.find { it.namespace == ns }
      namespace.documents = [xsd]
    }

    // set up documentFactory to return the right associated external XsdDocument object
    // using Mock object
    externalNamespaces.each { externalNamespace ->
      def doc = documents.find { it.xsdNamespace ==  externalNamespace }
      with(factory) {
	createDocument(doc.documentFile) >> doc
      }
    }

    // set up xsd Slurper returns -- key is the namespace, the value
    // is the namespaces to find in the documents variable
    ["xsd1":["e1", "e2"], "xsd2":["e2"], "xsd3":["e1", "xsd1"],
     "xsd4":["xsd1", "xsd2"], "xsd5":["xsd2"], "xsd6":["xsd3", "e3"],
     "xsd7":["xsd4"], "xsd8":["xsd3"], "xsd9":[], "e1":["e4", "e5"], "e2":["e6"],
     "e3":["e7", "e8", "e9"], "e4":["e6"], "e5":["e7", "e8"], "e6":[], "e7":[],
     "e8":[] , "e9":[] ].each { k,v ->
      with(xsdSlurper) {
	findResolvedXsdImports(k, _, _) >> documents.findAll { v.contains(it.xsdNamespace) }.documentFile
      }
    }
  }

  def "resolve external dependencies on all the namespaces"() {
    given:
    def xsds = documents.findAll { doc -> xsdNamespaces.contains(doc.xsdNamespace)  }.documentFile as Set

    when:
    resolver.resolve(xsds, namespaces)

    then: 'only xsd9 has no dependencies at all'
    def xsd9 = namespaces.find{ it.namespace == "xsd9" }
    xsd9.hasDependencies == false
    
    and: 'these namespace have ONLY external dependencies'
    def onlyExternal = namespaces.findAll { ["xsd1", "xsd2"].contains(it.namespace) }
    onlyExternal.hasDependencies == [false, false]

    and: "these namespaces have internal dependencies, some have externals"
    def internalOnly = ["xsd3", "xsd4", "xsd5", "xsd6", "xsd7", "xsd8"]
    def internalDeps = namespaces.findAll { internalOnly.contains(it.namespace)}
    internalDeps.hasDependencies == (1..6).collect { true }

    and: "these namespaces have external dependencies"
    ["xsd1":["e1", "e2", "e4", "e5", "e6", "e7", "e8"], "xsd2":["e2", "e6"],
     "xsd3":["e1", "e4", "e5", "e6", "e7", "e8"],
     "xsd6":["e3", "e7", "e8", "e9"]].each { k,v ->
       def namespace = namespaces.find { it.namespace == k }
       with(namespace) {
	 externalDependencies.containsAll(v as Set) == true
       }
    }
  }
}