package com.jacobo.gradle.plugins.structures

import com.jacobo.gradle.plugins.model.XsdSlurper

import spock.lang.Specification

class ExternalNamespaceResolverSpec extends Specification {
  
  def enr = new ExternalNamespaceResolver()
  
  def "Start with ExternalNamespaceMetaData initially populated and resolve the external imports, check output"() { 
  setup: "setup the external namespace meta data with schema location and namespace"
    def emd = new ExternalNamespaceMetaData()
    emd.namespace = "http://www.example.org/Kitchen"
    emd.externalSchemaLocation = new File(this.getClass().getResource("/schema/testImports/Kitchen.xsd").toURI()).absoluteFile
    def slurper = new XsdSlurper()
    slurper.document = emd.externalSchemaLocation
    emd.externalSlurper = slurper
    enr.externalImport = emd

  when: "resolve external import"
    def importList = enr.resolveExternalImportedNamespaces()
    emd.importedNamespaces = importList

  then: "Should have 2 imported objects Den and Living Room"
    importList.size == 2
    importList[0].namespace == "http://www.example.org/LivingRoom"
    importList[1].namespace == "http://www.example.org/Den"
    importList[0].externalSlurper.document == new File(this.getClass().getResource("/schema/testImports/LivingRoom.xsd").toURI()).absoluteFile
    importList[1].externalSlurper.document == new File(this.getClass().getResource("/schema/testImports/Den.xsd").toURI()).absoluteFile
    emd.importedNamespaces.size == 2
  }
}