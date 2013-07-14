package com.jacobo.gradle.plugins.slurper

import com.jacobo.gradle.plugins.util.FileUtil

import spock.lang.Specification

class XsdSlurperSpec extends Specification {
  
  def slurper = new XsdSlurper()
  
  def "process xsd dependencies" () {
  setup:
  def slurped = new XmlSlurper().parse(new File(file.toURI()))
  slurper.content = slurped
  slurper.document = new File(file.toURI())
  when:
  slurper.processXsdDependencyLocations(slurped.import)

  then:
  slurper.xsdImports == imports.collect {  FileUtil.getAbsoluteSchemaLocation(it, slurper.document.parentFile) }
  
  when: 
  slurper.processXsdDependencyLocations(slurped.include)
  
  then:
  slurper.xsdIncludes == includes.collect {  FileUtil.getAbsoluteSchemaLocation(it, slurper.document.parentFile) }

  when: 
  def result = slurper.gatherAllRelativeLocations()
  
  then: 
  result == relativeLocations.collect {  FileUtil.getAbsoluteSchemaLocation(it, slurper.document.parentFile) }

  where:
  file | imports | includes | relativeLocations
  this.getClass().getResource("/schema/Include/OrderNumber.xsd") | ["Product.xsd"] | ["include.xsd", "include2.xsd"] | ["Product.xsd", "include.xsd", "include2.xsd"]
  this.getClass().getResource("/schema/Include/Product.xsd") | [] | [] | []
  }

  def "make sure that the right type of object is populated correctly coming out of the get Imported Namespaces"() { 
  setup: "slurp schema, grab import node to pass into the imported namespaces, set up the Parent Directory for the function properly"
    def xmlDoc = new XmlSlurper().parse(schemaToSlurp)
    slurper.content = xmlDoc
    slurper.document = schemaToSlurp
    
  when: "you get imported namespaces the external Imported Namespace List should contain 1 element and have the namespace and the file populated"
    def result = slurper.obtainExternalDependencies()

  then: "check the external Import List"
    result.size == 1
    result[0].namespace == xmlDoc.import[0].@namespace.text()
    result[0].externalSlurper.document == externalAbsoluteFile

  where: "inputs and outputs are according to:"
    schemaToSlurp       |  externalAbsoluteFile
    new File(this.getClass().getResource("/schema/testImports/Kitchen.xsd").toURI()).absoluteFile | new File("build/resources/test/schema/testImports/LivingRoom.xsd").absoluteFile
    new File(this.getClass().getResource("/schema/testImports/LivingRoom.xsd").toURI()).absoluteFile | new File("build/resources/test/schema/testImports/Den.xsd").absoluteFile
  }
}