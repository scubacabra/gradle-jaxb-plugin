package com.jacobo.gradle.plugins.structures

import com.jacobo.gradle.plugins.model.XsdSlurper
import com.jacobo.gradle.plugins.reader.DocumentReader
import groovy.util.XmlSlurper
import groovy.util.slurpersupport.NodeChild

import com.jacobo.gradle.plugins.BaseSpecification
import spock.lang.Unroll

import org.gradle.api.logging.Logging
import org.gradle.api.logging.Logger

import spock.lang.*

class NamespaceDataSpec extends BaseSpecification {
  static final Logger log = Logging.getLogger(NamespaceDataSpec.class)

  def nmd = new NamespaceData()
  def operatingNamespaces = ["ns1", "ns2", "ns3"] as Set
  def historySlurpedFiles = [:]

  def setup() {
    def ns1File = new File("ns1")
    def ns2File = new File("ns2")
    def slurpedNs1 = new XsdSlurper(xsdNamespace: "ns1", documentFile: ns1File)
    def slurpedNs2 = new XsdSlurper(xsdNamespace: "ns2", documentFile: ns2File)
    historySlurpedFiles[ns1File] = slurpedNs1
    historySlurpedFiles[ns2File] = slurpedNs2
  }

  @Unroll("dependency #slurpedNamespace with current tree having #operatingNamespaces: has Dependencies? -- #emptyExternal, has externals? -- #empty")
  def "add dependency to this namespace"() { 
    setup:
    def slurpedDoc = new XsdSlurper(xsdNamespace: slurpedNamespace,
				    documentFile: slurpedFile)
    
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
    nmd.namespace = "ns3"
    nmd.slurpedDocuments = new XsdSlurper(documentFile: new File("ns3"),
					  xsdImports: slurpedImports,
					  documentDependencies: documentDependencies)
    
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

  def "namespace has an external dependency, need to parse it and add it to the history list"() { 
    setup:
    nmd.namespace = "ns3"
    nmd.slurpedDocuments = new XsdSlurper(documentFile: new File("ns3"),
  					  xsdImports: slurpedImports,
					  documentDependencies: documentDependencies)
    def slurper = GroovyMock(XsdSlurper)
    def reader = GroovySpy(DocumentReader, global: true)

    and:
    slurper.slurpNamespace(_) >> externalDependency
    slurper.getDocumentFile() >> new File(externalDependency + ".xsd")
    slurper.getXsdNamespace() >> externalDependency
    1 * DocumentReader.slurpDocument(_) >> slurper

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