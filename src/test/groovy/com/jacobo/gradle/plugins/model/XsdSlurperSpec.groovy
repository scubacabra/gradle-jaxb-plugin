package com.jacobo.gradle.plugins.model

import spock.lang.Specification

class XsdSlurperSpec extends Specification {
  
  def slurper = new XsdSlurper()
  
  def "process xsd dependencies" () {
  setup:
  def slurped = new XmlSlurper().parse(new File(file.toURI()))

  when:
  slurper.processXsdDependencyLocations(slurped.import)

  then:
  slurper.xsdImports == imports
  
  when: 
  slurper.processXsdDependencyLocations(slurped.include)
  
  then:
  slurper.xsdIncludes == includes

  when: 
  def result = slurper.gatherAllRelativeLocations()
  
  then: 
  result == relativeLocations

  where:
  file | imports | includes | relativeLocations
  this.getClass().getResource("/schema/Include/OrderNumber.xsd") | ["Product.xsd"] | ["include.xsd", "include2.xsd"] | ["Product.xsd", "include.xsd", "include2.xsd"]
  this.getClass().getResource("/schema/Include/Product.xsd") | [] | [] | []
  }
}