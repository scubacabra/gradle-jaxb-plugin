package com.jacobo.gradle.plugins.structures

import spock.lang.Specification

class OrderGraphSpec extends Specification {
  
  def og = new OrderGraph()
  
  def "populate namespace data initially when everything is still null" () {
  when:
  og.populateNamespaceMetaData(xsd)

  then:
  og.namespaceData.size == 1
  og.namespaceData.get(0).namespace == targetNamespace
  og.namespaceData.get(0).parseFiles.size == 1
  og.namespaceData.get(0).parseFiles == xsdFiles*.absoluteFile

  where:
  xsd = new File(this.getClass().getResource("/schema/House/Kitchen.xsd").toURI())
  targetNamespace = "http://www.example.org/Kitchen"
  xsdFiles = [new File("build/resources/test/schema/House/Kitchen.xsd")]

  }

  def "try populating namespace data with the same namespace you previously populated" () { 
  when:
  og.populateNamespaceMetaData(xsd)
  og.populateNamespaceMetaData(sameNamespace)

  then:
  og.namespaceData.size == 1
  og.namespaceData.get(0).namespace == targetNamespace
  og.namespaceData.get(0).parseFiles.size == 2
  og.namespaceData.get(0).parseFiles == xsdFiles*.absoluteFile

  where:
  xsd = new File(this.getClass().getResource("/schema/House/Kitchen.xsd").toURI())
  sameNamespace = new File(this.getClass().getResource("/schema/House/KitchenSubset.xsd").toURI())
  targetNamespace = "http://www.example.org/Kitchen"
  xsdFiles = [new File("build/resources/test/schema/House/Kitchen.xsd"), new File("build/resources/test/schema/House/KitchenSubset.xsd")]

  }

  def "Populate namespace data with two different namespaces" () { 
  when:
  og.populateNamespaceMetaData(xsd)
  og.populateNamespaceMetaData(newNamespace)

  then:
  og.namespaceData.size == 2
  og.namespaceData.namespace == targetNamespace
  og.namespaceData.parseFiles.size == 2
  og.namespaceData.parseFiles == xsdFiles*.absoluteFile

  where:
  xsd = new File(this.getClass().getResource("/schema/House/Kitchen.xsd").toURI())
  newNamespace = new File(this.getClass().getResource("/schema/House/LivingRoom.xsd").toURI())
  targetNamespace = ["http://www.example.org/Kitchen", "http://www.example.org/LivingRoom"]
  xsdFiles = [[new File("build/resources/test/schema/House/Kitchen.xsd")], [new File("build/resources/test/schema/House/LivingRoom.xsd")]]

  }

  def "populate namespace meta Data starting from find all xsd files"() { 
  when:
  og.findAllXsdFiles(directory)

  then:
  og.namespaceData.size == 2
  og.namespaceData.namespace == targetNamespace
  og.namespaceData.find{ it.namespace == targetNamespace.get(0)}.parseFiles.size == 2
  og.namespaceData.find{ it.namespace == targetNamespace.get(1)}.parseFiles.size == 1
  og.namespaceData.parseFiles == xsdFiles*.absoluteFile
  
  
  where:
  directory = new File("build/resources/test/schema/House").absolutePath
  targetNamespace = ["http://www.example.org/Kitchen", "http://www.example.org/LivingRoom"]
  xsdFiles = [[new File("build/resources/test/schema/House/Kitchen.xsd"), new File("build/resources/test/schema/House/KitchenSubset.xsd")], [new File("build/resources/test/schema/House/LivingRoom.xsd")]]
  }
}