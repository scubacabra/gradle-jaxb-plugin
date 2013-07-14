package com.jacobo.gradle.plugins.resolver

import com.jacobo.gradle.plugins.slurper.XsdSlurper
import com.jacobo.gradle.plugins.structures.ExternalNamespaceMetaData

import spock.lang.Specification

class ExternalNamespaceResolverSpec extends Specification {
  
  def enr = new ExternalNamespaceResolver()
  
  def "Start with ExternalNamespaceMetaData initially populated and resolve the external imports, check output"() { 
  setup: "setup the external namespace meta data with schema location and namespace"
    def emd = new ExternalNamespaceMetaData()
    emd.externalSchemaLocation = new File(this.getClass().getResource("/schema/testImports/Kitchen.xsd").toURI()).absoluteFile
    def slurper = new XsdSlurper()
    slurper.document = emd.externalSchemaLocation
    emd.externalSlurper = slurper
    enr.externalImport = emd

  when: "resolve external import"
    emd = enr.resolveExternalImportedNamespaces()

  then: "Should have 2 imported objects Den and Living Room"
    emd.importedNamespaces.size == 2
    emd.importedNamespaces[0].namespace == "http://www.example.org/LivingRoom"
    emd.importedNamespaces[1].namespace == "http://www.example.org/Den"
    emd.importedNamespaces[0].externalSlurper.document == new File(this.getClass().getResource("/schema/testImports/LivingRoom.xsd").toURI()).absoluteFile
    emd.importedNamespaces[1].externalSlurper.document == new File(this.getClass().getResource("/schema/testImports/Den.xsd").toURI()).absoluteFile
    emd.importedNamespaces.size == 2
  }
}