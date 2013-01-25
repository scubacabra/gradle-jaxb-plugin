package com.jacobo.gradle.plugins.structures

import spock.lang.Specification

import org.gradle.api.logging.Logging
import org.gradle.api.logging.Logger

class NamespaceMetaDataSpec extends Specification {
  static final Logger log = Logging.getLogger(NamespaceMetaDataSpec.class)

  def nmd = new NamespaceMetaData()
  
  def "test targetNamespace to episode file name" () {
  when:
  nmd.namespace = ns
  
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
  nmd.addExternalImportedNamespaces(extNamespace, externalFile)

  then:
  nmd.externalImportedNamespaces.size == 1
  nmd.externalImportedNamespaces.namespace == [extNamespace]
  nmd.externalImportedNamespaces.externalSchemaLocation == [externalFile]
  
  where:
  extNamespace                     | externalFile
  "http://www.example.org/Kitchen" | new File(this.getClass().getResource("/schema/House/Kitchen.xsd").toURI())
  }

  def "add external imported namespace with one prior external namespace" () { 
  when:
  def externalNamespace = new ExternalNamespaceMetaData(namespace: "http://www.example.org/Kitchen", externalSchemaLocation: new File(this.getClass().getResource("/schema/House/Kitchen.xsd").toURI()).absoluteFile)
  nmd.externalImportedNamespaces << externalNamespace
  nmd.addExternalImportedNamespaces(extNamespace, externalFile)

  then:
  nmd.externalImportedNamespaces.size == 2
  nmd.externalImportedNamespaces.namespace == ["http://www.example.org/Kitchen", extNamespace]
  nmd.externalImportedNamespaces.externalSchemaLocation == [new File(this.getClass().getResource("/schema/House/Kitchen.xsd").toURI()), externalFile]

  where:
  extNamespace                        | externalFile
  "http://www.example.org/LivingRoom" | new File(this.getClass().getResource("/schema/House/LivingRoom.xsd").toURI())
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

  def "slurped up imports for this namespace data, not external namespace" () { 
  setup:
  def livingRoomMetaData = new NamespaceMetaData(namespace: "http://www.example.org/LivingRoom")


  when:
  def schema = new XmlSlurper().parse(doc)
  def imports = schema.import
  def namespaceMetaData = [livingRoomMetaData]
  nmd.slurpImports(imports, namespaceMetaData, doc)

  then:
  nmd.importedNamespaces.size == 1
  nmd.importedNamespaces[0].namespace == "http://www.example.org/LivingRoom"
  
  where:
  doc = new File(this.getClass().getResource("/schema/testImports/Kitchen.xsd").toURI()).absoluteFile
  
  }

  def "slurped up imports for this namespace data, is an external namespace" () { 
  when:
  def namespaceMetaData = []
  def schema = new XmlSlurper().parse(doc)
  def imports = schema.import
  nmd.slurpImports(imports, namespaceMetaData, doc)

  then:
  nmd.importedNamespaces.size == 0
  nmd.externalImportedNamespaces.size == 1
  nmd.importedNamespaces == []
  nmd.externalImportedNamespaces[0].namespace == "http://www.example.org/LivingRoom"
  
  where:
  doc = new File(this.getClass().getResource("/schema/testImports/Kitchen.xsd").toURI()).absoluteFile
  
  }

  def "slurped up includes, make sure to subtract from parseFiles" () { 
  when: "already gathered xsd for this folder, slurping the file includes (that's all it has)"
  def schema = new XmlSlurper().parse(doc)
  def includes = schema.include
  nmd.parseFiles << doc
  nmd.parseFiles << includeFile
  nmd.namespace = "http://www.example.org/Kitchen"
  nmd.slurpIncludes(includes, doc)

  then: "include files should be 1"
  nmd.includeFiles.size == 1
  nmd.includeFiles[0] == includeFile

  when: "we process include files, the parse Files should be without the included file"
  nmd.processIncludeFiles()

  then: "parse Files should only be of length 1"
  nmd.parseFiles.size == 1
  nmd.parseFiles[0] == doc
  
  where:
  doc = new File(this.getClass().getResource("/schema/testIncludes/Kitchen.xsd").toURI()).absoluteFile
  includeFile = new File(this.getClass().getResource("/schema/testIncludes/KitchenSubset.xsd").toURI()).absoluteFile
  }

  def "slurped up includes, circular dependency (error in writing your schema) now parseFiles is null" () { 
  when:
  nmd.parseFiles << doc
  nmd.parseFiles << includeFile
  nmd.includeFiles << doc
  nmd.includeFiles << includeFile
  nmd.namespace = "http://www.example.org/Kitchen"
  nmd.processIncludeFiles()

  then: "include files should be 2 and parse files should be 0" //TODO an error should really be thrown IMO
  nmd.includeFiles.size == 2
  nmd.includeFiles.find { it == includeFile } == includeFile
  nmd.includeFiles.find { it == doc } == doc
  nmd.parseFiles.size == 0
  nmd.parseFiles == []
  
  where:
  doc = new File(this.getClass().getResource("/schema/testIncludes/Kitchen.xsd").toURI()).absoluteFile
  includeFile = new File(this.getClass().getResource("/schema/testIncludes/KitchenSubset.xsd").toURI()).absoluteFile
  }
}