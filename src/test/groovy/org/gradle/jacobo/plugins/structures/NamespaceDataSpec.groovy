package org.gradle.jacobo.plugins.structures

import org.gradle.jacobo.schema.XsdDocument
import org.gradle.jacobo.schema.BaseSchemaDocument
import org.gradle.jacobo.schema.slurper.XsdSlurper
import org.gradle.jacobo.schema.slurper.DocumentSlurper
import org.gradle.jacobo.schema.resolver.DocumentResolver
import org.gradle.jacobo.schema.factory.DocumentFactory

import groovy.util.XmlSlurper
import groovy.util.slurpersupport.NodeChild

import org.gradle.jacobo.plugins.BaseSpecification
import spock.lang.Unroll

import org.gradle.api.logging.Logging
import org.gradle.api.logging.Logger

import spock.lang.*

class NamespaceDataSpec extends BaseSpecification {
  static final Logger log = Logging.getLogger(NamespaceDataSpec.class)

  def xsdSlurper = Mock(XsdSlurper)
  def documentSlurper = Mock(DocumentSlurper)
  def documentResolver = Mock(DocumentResolver)
  def mockSlurper = new XmlSlurper().parseText("<xsd></xsd>")

  def nmd = new NamespaceData()
  def operatingNamespaces = ["ns1", "ns2", "ns3"] as Set
  def historySlurpedFiles = [:]

  def setup() {
    def ns1File = new File("ns1")
    def ns2File = new File("ns2")
    def slurpedNs1 = new XsdDocument(documentSlurper, documentResolver,
				     xsdSlurper, ns1File, mockSlurper)
    slurpedNs1.xsdNamespace = "ns1"
    def slurpedNs2 = new XsdDocument(documentSlurper, documentResolver,
				     xsdSlurper, ns2File, mockSlurper)
    slurpedNs2.xsdNamespace = "ns2"
    historySlurpedFiles[ns1File] = slurpedNs1
    historySlurpedFiles[ns2File] = slurpedNs2
  }

  @Unroll("dependency #slurpedNamespace with current tree having #operatingNamespaces: has Dependencies? -- #emptyExternal, has externals? -- #empty")
  def "add dependency to this namespace"() { 
    setup:
    def slurpedDoc = new XsdDocument(documentSlurper, documentResolver,
				     xsdSlurper, slurpedFile, mockSlurper)
    slurpedDoc.xsdNamespace = slurpedNamespace
    
    when:
    nmd.addDependencyToNamespace(operatingNamespaces, slurpedDoc)
    
    then:
    nmd.dependentNamespaces.isEmpty() == empty
    nmd.dependentExternalFiles.isEmpty() == emptyExternal
    nmd.dependentNamespaces == depNamespaces as Set
    nmd.dependentExternalFiles == depExternal as Set

    where:
    slurpedFile = new File("someFile.xsd")
    slurpedNamespace << ["ns2", "ns3", "ns4"]
    empty << [false, false, true]
    emptyExternal << [true, true, false]
    depNamespaces << [["ns2"], ["ns3"], []]
    depExternal << [[], [], [new File("someFile.xsd")]]
  }

  @Unroll("ns3 with imports #slurpedImports relative location, in #documentDependencies")
  def "namespace depends on all internal namespaces" () { 
    setup:
    def factory = Mock(DocumentFactory)
    nmd.namespace = "ns3"
    def slurpedDoc = new XsdDocument(documentSlurper, documentResolver,
				     xsdSlurper, new File("ns3"), mockSlurper)
    slurpedDoc.xsdImports = slurpedImports
    slurpedDoc.documentDependencies = documentDependencies

    nmd.slurpedDocuments = slurpedDoc
    nmd.documentFactory = factory
    
    and:
    xsdSlurper.findResolvedXsdImports(*_) >> documentDependencies.collect { k,v -> v}

    when:
    def result = nmd.findNamespacesDependentOn(operatingNamespaces,
					       historySlurpedFiles)

    then:
    nmd.hasDependencies == hasDeps
    nmd.dependentNamespaces.isEmpty() == depsEmpty
    nmd.dependentNamespaces == depNamespaces as Set
    nmd.hasExternalDependencies == false
    nmd.dependentExternalFiles.isEmpty() == true
    result.size() == 2
    result.each { k,v ->
      [new File("ns1"), new File("ns2")].contains(k)
      ["ns1", "ns2"].contains(v.xsdNamespace) == true
    }
    
    where:
    slurpedImports << [["ns2"] as Set, [] as Set]
    documentDependencies << [["ns2": new File("ns2")], [:]]
    hasDeps << [true, false]
    depsEmpty << [false, true]
    depNamespaces << [["ns2"], []]
  }

  @Unroll
  def "namespace has an external dependency on '#externalDependency', need to parse it and add it to the history list"() {
    setup:
    def factory = Mock(DocumentFactory)
    nmd.namespace = "ns3"
    def slurpedDoc = new XsdDocument(documentSlurper, documentResolver,
				     xsdSlurper, new File("ns3"), mockSlurper)
    slurpedDoc.xsdImports = slurpedImports
    slurpedDoc.documentDependencies = documentDependencies
    nmd.slurpedDocuments = slurpedDoc
    nmd.documentFactory = factory

    def slurper = GroovyMock(XsdDocument)

    and:
    xsdSlurper.findResolvedXsdImports(*_) >> [new File(externalDependency + ".xsd")]
    factory.createDocument(_ as File) >> slurper
    slurper.getDocumentFile() >> new File(externalDependency + ".xsd")
    slurper.getXsdNamespace() >> externalDependency

    when:
    def result = nmd.findNamespacesDependentOn(operatingNamespaces,
					       historySlurpedFiles)

    then:
    nmd.hasDependencies == false
    nmd.dependentNamespaces.isEmpty() == true
    nmd.hasExternalDependencies == true
    nmd.dependentExternalFiles == [new File(externalDependency + ".xsd") ] as Set
    result.size() == 3
    result.each { k,v ->
      [new File("ns1"), new File("ns2"), new File(externalDependency + ".xsd")].contains(k)
      ["ns1", "ns2", externalDependency].contains(v.xsdNamespace)
    }

    where:
    slurpedImports << [["ns13.xsd"] as Set, ["ns10.xsd"] as Set]
    documentDependencies << [["ns13": new File("ns13")], ["ns10": new File("ns10")]]
    externalDependency << ["ns13", "ns10"]
  }
}