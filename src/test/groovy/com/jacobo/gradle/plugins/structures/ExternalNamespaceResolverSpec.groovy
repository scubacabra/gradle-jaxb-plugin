package com.jacobo.gradle.plugins.structures

import spock.lang.Specification

class ExternalNamespaceResolverSpec extends Specification {
  
  def enr = new ExternalNamespaceResolver()
  
  def "make sure grab absolute file is working properly" () {

  expect: "have a parent Directory, and a relative schema location to that parent directory, get correct absolute path"
    fileReturn.path == enr.getAbsoluteSchemaLocation(schemaLocation, parentDir).path

  where: "inputs and outputs are according to:"
    schemaLocation               | parentDir                                                                   | fileReturn
    "../testImports/Kitchen.xsd" | new File(this.getClass().getResource("/schema/House").toURI()).absoluteFile | new File("build/resources/test/schema/testImports/Kitchen.xsd").absoluteFile
    "../testIncludes/KitchenSubset.xsd" | new File(this.getClass().getResource("/schema/House").toURI()).absoluteFile | new File("build/resources/test/schema/testIncludes/KitchenSubset.xsd").absoluteFile
  }

  def "make sure that the right type of object is populated correctly coming out of the schema location gatherer"() { 
  setup: "slurp schema, grab import node to pass into gatherSchemaLocations, set up the Parent Directory for the function properly"
    def xmlDoc = new XmlSlurper().parse(schemaToSlurp)
    enr.parentDirectory = schemaToSlurp.parentFile

  when: "import node to gatherer of locations, returns External Meta Data"
    def emd = enr.gatherSchemaLocations(xmlDoc.import[0])

  then: "should get back an object with just the externalSchemaLocation field populated"
    emd.externalSchemaLocation.path == externalAbsoluteFile.path
  
  where: "inputs and outputs are according to:"
    schemaToSlurp       |  externalAbsoluteFile
    new File(this.getClass().getResource("/schema/testImports/Kitchen.xsd").toURI()).absoluteFile | new File("build/resources/test/schema/testImports/LivingRoom.xsd").absoluteFile
    new File(this.getClass().getResource("/schema/testImports/LivingRoom.xsd").toURI()).absoluteFile | new File("build/resources/test/schema/testImports/Den.xsd").absoluteFile
  }

  def "make sure that the right type of object is populated correctly coming out of the get Imported Namespaces"() { 
  setup: "slurp schema, grab import node to pass into the imported namespaces, set up the Parent Directory for the function properly"
    def xmlDoc = new XmlSlurper().parse(schemaToSlurp)
    enr.parentDirectory = schemaToSlurp.parentFile

  when: "you get imported namespaces the external Imported Namespace List should contain 1 element and have the namespace and the file populated"
    enr.getImportedNamespaces(xmlDoc.import[0])

  then: "check the external Import List"
    enr.externalImportedNamespaces.size == 1
    enr.externalImportedNamespaces[0].namespace == xmlDoc.import[0].@namespace.text()
    enr.externalImportedNamespaces[0].externalSchemaLocation.path == externalAbsoluteFile.path

  where: "inputs and outputs are according to:"
    schemaToSlurp       |  externalAbsoluteFile
    new File(this.getClass().getResource("/schema/testImports/Kitchen.xsd").toURI()).absoluteFile | new File("build/resources/test/schema/testImports/LivingRoom.xsd").absoluteFile
    new File(this.getClass().getResource("/schema/testImports/LivingRoom.xsd").toURI()).absoluteFile | new File("build/resources/test/schema/testImports/Den.xsd").absoluteFile
  }

  def "Start with ExternalNamespaceMetaData initially populated and resolve the external imports, check output"() { 
  setup: "setup the external namespace meta data with schema location and namespace"
    def emd = new ExternalNamespaceMetaData()
    emd.namespace = "http://www.example.org/Kitchen"
    emd.externalSchemaLocation = new File(this.getClass().getResource("/schema/testImports/Kitchen.xsd").toURI()).absoluteFile
    enr.externalImport = emd

  when: "resolve external import"
    def importList = enr.resolveExternalImportedNamespaces()
    emd.externalImportedNamespaces = importList

  then: "Should have 2 imported objects Den and Living Room"
    importList.size == 2
    importList[0].namespace == "http://www.example.org/LivingRoom"
    importList[1].namespace == "http://www.example.org/Den"
    importList[0].externalSchemaLocation.path == new File(this.getClass().getResource("/schema/testImports/LivingRoom.xsd").toURI()).absoluteFile.path
    importList[1].externalSchemaLocation.path == new File(this.getClass().getResource("/schema/testImports/Den.xsd").toURI()).absoluteFile.path
    emd.externalImportedNamespaces.size == 2
  }
}