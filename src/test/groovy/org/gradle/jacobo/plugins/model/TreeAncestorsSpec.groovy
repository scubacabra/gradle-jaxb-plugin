package org.gradle.jacobo.plugins.model

import spock.lang.Unroll
import org.gradle.jacobo.plugins.fixtures.TreeFixture

class TreeAncestorSpec extends TreeFixture {

  def "get parents of tree root node"() { 
    given:
    def node = nodes.find { it.data.namespace == "xsd1" }
    
    when:
    def parents = node.getAncestors()

    then:
    parents.size() == 0
    parents.isEmpty() == true
  }

  @Unroll
  def "get parents of '#namespace'"() {
    given:
    def node = nodes.find { it.data.namespace == namespace }
    
    and:
    def expectation = nodes.findAll { ancestors.contains(it.data.namespace) }

    when:
    def parents = node.getAncestors()

    then:
    parents.size() == expectation.size()
    parents.containsAll(expectation)
    
    where:
    namespace | ancestors
    "xsd3"    | ["xsd1"]
    "xsd4"    | ["xsd1", "xsd2"]
    "xsd5"    | ["xsd2"]
    "xsd6"    | ["xsd3", "xsd1"]
    "xsd7"    | ["xsd3", "xsd1"]
    "xsd8"    | ["xsd4", "xsd5", "xsd1", "xsd2"]
    "xsd9"    | ["xsd5", "xsd2"]
  }
}