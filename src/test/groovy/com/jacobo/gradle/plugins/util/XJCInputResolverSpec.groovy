package com.jacobo.gradle.plugins.util

import spock.lang.Specification

import com.jacobo.gradle.plugins.structures.NamespaceMetaData
import com.jacobo.gradle.plugins.model.XsdSlurper

class XJCInputResolverSpec extends Specification {
  
  def ns1, ns2, ns3, ns4, ns5

  def setup() {
    ns1 = new NamespaceMetaData(namespace: "ns1")
    ns2 = new NamespaceMetaData(namespace: "ns2")
    ns3 = new NamespaceMetaData(namespace: "ns3")
    ns4 = new NamespaceMetaData(namespace: "ns4")
    ns5 = new NamespaceMetaData(namespace: "ns5")
  }

  def "transfrom list of bindings to a space separated string" () {
  when:
  def bindingsString = XJCInputResolver.transformBindingListToString(bindings)

  then:
  bindingsString == "binding1.xjc binding2.xjc binding3.xjc "

  where:
  bindings = ["binding1.xjc", "binding2.xjc", "binding3.xjc"]
  }

  def "transform xjc parse File to relative path compared to xsd Dir" () { 
  setup:
  def nsData = new NamespaceMetaData()
  xsdFiles.each { 
    def slurper = new XsdSlurper()
    slurper.document = it
    nsData.slurpers << slurper
  }
  when:
  def relativeIncludes = XJCInputResolver.transformSchemaListToString(nsData)

  then:
  relativeIncludes == "Kitchen.xsd KitchenSubset.xsd LivingRoom.xsd "

  where:
  xsdDir = new File(this.getClass().getResource("/schema/House").toURI()).absolutePath
  xsdFiles = [new File(this.getClass().getResource("/schema/House/Kitchen.xsd").toURI()), new File(this.getClass().getResource("/schema/House/KitchenSubset.xsd").toURI()), new File(this.getClass().getResource("/schema/House/LivingRoom.xsd").toURI())]*.absoluteFile
  }

  def "find all dependencies episode binding names for this Namespace" () { 
  setup: "populate all the namespaces and their dependencies"
    ns1.importedNamespaces << "none"
    ns2.importedNamespaces = [ns1]
    ns3.importedNamespaces = [ns2]
    ns4.importedNamespaces = [ns3]
    ns5.importedNamespaces = [ns4]
    [ns2, ns3, ns4, ns5]*.dependsOnAnotherNamespace = true

  when: "we find dependencies up the graph"
    def dependencies = XJCInputResolver.findDependenciesUpGraph(ns5, [])
    dependencies*.convertNamespaceToEpisodeName()
    def episodeNames = dependencies.episodeName

  then: "we can expect this episode list"
    ns4.episodeName == "ns4"
    dependencies == [ns4, ns3, ns2, ns1]
    episodeNames == ["ns4", "ns3", "ns2", "ns1"]
  }
  
  def "base namespace has no imports but has external imports" () { 
  setup: "populate all the namespaces and their dependencies"
	  ns1.dependsOnAnotherNamespace = false //base
	  ns2.importedNamespaces << "none" //external base
	  ns3.importedNamespaces = [ns2]
	  ns4.importedNamespaces = [ns3, ns2]
	  ns1.externalImportedNamespaces = [ns4]
	  [ns3, ns4]*.dependsOnAnotherNamespace = true

  when: "we try to get dependencies up the graph, need to do external and not bail"
	  def dependencies = XJCInputResolver.findDependenciesUpGraph(ns1, [])
	  dependencies*.convertNamespaceToEpisodeName()
          def episodeNames = dependencies.episodeName

  then: "we can expect this dependency and episode binding list for ns1"
	  dependencies == [ns4, ns3, ns2]
	  episodeNames == ["ns4", "ns3", "ns2"]
  }
}