package com.jacobo.gradle.plugins.util

import spock.lang.Specification

import com.jacobo.gradle.plugins.structures.NamespaceMetaData

class XJCInputResolverSpec extends Specification {
  
  def "transfrom list of bindings to a space separated string" () {
  when:
  def bindingsString = XJCInputResolver.transformBindingListToString(bindings)

  then:
  bindingsString == "binding1.xjc binding2.xjc binding3.xjc "

  where:
  bindings = ["binding1.xjc", "binding2.xjc", "binding3.xjc"]
  }

  def "transform xjc parse File to relative path compared to xsd Dir" () { 
  when:
  def nsData = new NamespaceMetaData()
  nsData.parseFiles = xsdFiles
  def relativeIncludes = XJCInputResolver.transformSchemaListToString(nsData)

  then:
  relativeIncludes == "Kitchen.xsd KitchenSubset.xsd LivingRoom.xsd "

  where:
  xsdDir = new File(this.getClass().getResource("/schema/House").toURI()).absolutePath
  xsdFiles = [new File(this.getClass().getResource("/schema/House/Kitchen.xsd").toURI()), new File(this.getClass().getResource("/schema/House/KitchenSubset.xsd").toURI()), new File(this.getClass().getResource("/schema/House/LivingRoom.xsd").toURI())]*.absoluteFile
  }
  
}