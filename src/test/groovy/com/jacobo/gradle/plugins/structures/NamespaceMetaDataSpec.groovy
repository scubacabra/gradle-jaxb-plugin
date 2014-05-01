package com.jacobo.gradle.plugins.structures

import com.jacobo.gradle.plugins.slurper.XsdSlurper

import spock.lang.Specification

import org.gradle.api.logging.Logging
import org.gradle.api.logging.Logger

class NamespaceMetaDataSpec extends Specification {
  static final Logger log = Logging.getLogger(NamespaceMetaDataSpec.class)

  def nmd = new NamespaceMetaData()
  
  def "test targetNamespace to episode file name" () {
  setup:
  nmd.namespace = ns

  when:
  nmd.convertNamespaceToEpisodeName()
  
  then:
  nmd.episodeName == episode

  where:
  ns                              | episode
  "http://fake.com/donuts/glazed" | "fake.com-donuts-glazed"
  "urn:real/boy/pinnochio"        | "urn-real-boy-pinnochio"
  "tickle/me/elmo"                | "tickle-me-elmo"
  }

  def "add external imported namespace with no prior external namespace" () { 
  when:
  nmd.externalImportedNamespaces = []
  nmd.addExternalImportedNamespaces(externalFile)

  then:
  nmd.externalImportedNamespaces.size == 1
  nmd.externalImportedNamespaces.externalSchemaLocation == [externalFile]
  
  where:
  externalFile =  new File(this.getClass().getResource("/schema/House/Kitchen.xsd").toURI())
  }

  def "add external imported namespace with one prior external namespace" () { 
  when:
  def externalNamespace = new ExternalNamespaceMetaData(namespace: "http://www.example.org/Kitchen", externalSchemaLocation: new File(this.getClass().getResource("/schema/House/Kitchen.xsd").toURI()).absoluteFile)
  nmd.externalImportedNamespaces << externalNamespace
  nmd.addExternalImportedNamespaces(externalFile)

  then:
  nmd.externalImportedNamespaces.size == 2
  nmd.externalImportedNamespaces.externalSchemaLocation == [new File(this.getClass().getResource("/schema/House/Kitchen.xsd").toURI()), externalFile]

  where:
  externalFile = new File(this.getClass().getResource("/schema/House/LivingRoom.xsd").toURI())
  }

  //TODO should remove this
  /* def "add external imported namespace with one prior external namespace and a different external Schema Location (prob never going to happen)" () { 
  when:
  def externalNamespace = new ExternalNamespaceMetaData(namespace: "http://www.example.org/Kitchen", externalSchemaLocation: new File(this.getClass().getResource("/schema/House/Kitchen.xsd").toURI()))
  nmd.externalImportedNamespaces << externalNamespace
  nmd.addExternalImportedNamespaces(extNamespace, externalFile)

  then:
  nmd.externalImportedNamespaces.size == 1
  nmd.externalImportedNamespaces.namespace == ["http://www.example.org/Kitchen"]
  nmd.externalImportedNamespaces.externalSchemaLocation == [[new File(this.getClass().getResource("/schema/House/Kitchen.xsd").toURI()), externalFile]]

  where:
  extNamespace                        | externalFile
  "http://www.example.org/Kitchen" | new File(this.getClass().getResource("/schema/House/KitchenSubset.xsd").toURI())
  } */

  def "obtain imported namespaces for this namespace data, is not an external namespace, Kitchen imports LivingRoom namespace" () { 
  setup:
  def slurped = new XsdSlurper()
  slurped.xsdNamespace = "http://www.example.org/LivingRoom"
  slurped.document = livingRoomXsd
  def livingRoomMetaData = new NamespaceMetaData(namespace: "http://www.example.org/LivingRoom", slurpers: [slurped])
  slurped.xsdImports = [livingRoomXsd]
  slurped.xsdNamespace = "http://www.example.org/Kitchen"
  nmd.slurpers << slurped
  nmd.namespace = slurped.xsdNamespace

  when:
  def namespaceMetaData = [livingRoomMetaData]
  nmd.obtainImportedNamespaces(namespaceMetaData)

  then:
  nmd.importedNamespaces.size == 1
  nmd.importedNamespaces[0].namespace == "http://www.example.org/LivingRoom"
  nmd.externalImportedNamespaces == []
  
  
  where:
  doc = new File(this.getClass().getResource("/schema/testImports/Kitchen.xsd").toURI()).absoluteFile
  livingRoomXsd = new File(this.getClass().getResource("/schema/testImports/LivingRoom.xsd").toURI()).absoluteFile
  }

  def "obtain imported namespaces for this namespace data, is an external namespace" () { 
  setup:
  def slurped = new XsdSlurper()
  slurped.xsdImports = [doc]
  slurped.xsdNamespace = "http://www.example.org/LivingRoom"
  nmd.namespace = slurped.xsdNamespace
  nmd.slurpers << slurped

  when:
  def namespaceMetaData = []
  nmd.obtainImportedNamespaces(namespaceMetaData)

  then:
  nmd.importedNamespaces.size == 0
  nmd.externalImportedNamespaces.size == 1
  nmd.importedNamespaces == []
  nmd.externalImportedNamespaces[0].externalSchemaLocation == doc
  
  where:
  doc = new File(this.getClass().getResource("/schema/testImports/Kitchen.xsd").toURI()).absoluteFile
  
  }
}