package org.openrepose.gradle.plugins.jaxb.tree

import org.openrepose.gradle.plugins.jaxb.fixtures.TreeFixture

class TreeDescendantsSpec extends TreeFixture {
  def manager = new TreeManager()

  def setup() {
    manager.managedNodes = nodes as Set
    manager.currentTreeRow = nodes.findAll {
      ["xsd6", "xsd7", "xsd8", "xsd9"].contains(it.data.namespace)
    }
    manager.treeRoot = nodes.findAll {
      ["xsd1", "xsd2"].contains(it.data.namespace)
    } as LinkedList
  }

  def "get all descendants until there are no more, starting from the tree Root"() {
    when:
    def descendants = manager.getNextDescendants(manager.treeRoot)

    then:
    descendants.size() == 3
    descendants.data.namespace.containsAll(["xsd3", "xsd4", "xsd5"])

    when:
    descendants = manager.getNextDescendants(descendants)

    then:
    descendants.size() == 4
    descendants.data.namespace.containsAll(["xsd6", "xsd7", "xsd8", "xsd9"])
  }
}