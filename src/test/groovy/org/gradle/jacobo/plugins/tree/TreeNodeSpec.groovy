package org.gradle.jacobo.plugins.tree

import org.gradle.jacobo.plugins.fixtures.NamespaceFixture

class TreeNodeSpec extends NamespaceFixture {

  def setup() {
    createSimpleDependencyData()
  }

  def "Create node with data, no parents or children"() { 
    given:
    def xsd1 = namespaces.find { it.namespace == "xsd1" }
    def node = new TreeNode(xsd1)

    expect:
    with(node) {
      data == xsd1
      children.size() == 0
      parents == null
    }
  }

  def "Create node with data, and one parent"() { 
    given:
    def xsd1 = namespaces.find { it.namespace == "xsd1" }
    def parentNode = new TreeNode(xsd1)
    def xsd3 = namespaces.find { it.namespace == "xsd3" }
    
    when:
    def node = new TreeNode(xsd3, parentNode)

    then:
    with(node) {
      data == xsd3
      children.size() == 0
      parents.size() == 1
      parents.contains(parentNode)
    }
    with(parentNode) {
      children.size() == 1
      children.contains(node)
    }
  }

  def "Create node with data, and several parents"() { 
    given:
    def xsd1 = namespaces.find { it.namespace == "xsd1" }
    def xsd2 = namespaces.find { it.namespace == "xsd2" }
    def parentNode1 = new TreeNode(xsd1)
    def parentNode2 = new TreeNode(xsd2)
    def xsd3 = namespaces.find { it.namespace == "xsd3" }
    
    when:
    def node = new TreeNode(xsd3, [parentNode1, parentNode2] as LinkedList)

    then:
    with(node) {
      data == xsd3
      children.size() == 0
      parents.size() == 2
      parents.containsAll([parentNode1, parentNode2])
    }
    [parentNode1, parentNode2].each { parentNode ->
      with(parentNode) {
	children.size() == 1
	children.contains(node)
      }
    }
  }

  def "Add child to a parent, create two-way parent<=>child association"() { 
    given:
    def xsd1 = namespaces.find { it.namespace == "xsd1" }
    def parentNode = new TreeNode(xsd1)
    def xsd3 = namespaces.find { it.namespace == "xsd3" }
    def childNode = new TreeNode(xsd3)
    // necessary so test doesn't fail with parents being null
    // should never happen in code, as a node would either be constructed
    // as a base Node (i.e. parents == null) or as a dependent node, in which case
    // parents would be attached via constructor
    childNode.parents = [] as LinkedList
    
    when:
    parentNode.addChild(childNode, true)

    then:
    with(parentNode) {
      children.size() == 1
      children.contains(childNode)
    }
    with(childNode) {
      parents.size() == 1
      parents.contains(parentNode)
    }
  }

  def "Add child to a parent, create one-way parent=>child association"() { 
    given:
    def xsd1 = namespaces.find { it.namespace == "xsd1" }
    def parentNode = new TreeNode(xsd1)
    def xsd3 = namespaces.find { it.namespace == "xsd3" }
    def childNode = new TreeNode(xsd3)
    
    when:
    parentNode.addChild(childNode, false)

    then:
    with(parentNode) {
      children.size() == 1
      children.contains(childNode)
    }
    with(childNode) {
      parents == null
    }
  }

  def "Add parent to child, create two-way parent<=>child association"() { 
    given:
    def xsd1 = namespaces.find { it.namespace == "xsd1" }
    def parentNode = new TreeNode(xsd1)
    def xsd3 = namespaces.find { it.namespace == "xsd3" }
    def childNode = new TreeNode(xsd3)
    // necessary so test doesn't fail with parents being null
    // should never happen in code, as a node would either be constructed
    // as a base Node (i.e. parents == null) or as a dependent node, in which case
    // parents would be attached via constructor
    childNode.parents = [] as LinkedList

    when:
    childNode.addParent(parentNode, true)

    then:
    with(parentNode) {
      children.size() == 1
      children.contains(childNode)
    }
    with(childNode) {
      parents.size() == 1
      parents.contains(parentNode)
    }
  }

  def "Add parent to child, create one-way child=>parent association"() { 
    given:
    def xsd1 = namespaces.find { it.namespace == "xsd1" }
    def parentNode = new TreeNode(xsd1)
    def xsd3 = namespaces.find { it.namespace == "xsd3" }
    def childNode = new TreeNode(xsd3)
    // necessary so test doesn't fail with parents being null
    // should never happen in code, as a node would either be constructed
    // as a base Node (i.e. parents == null) or as a dependent node, in which case
    // parents would be attached via constructor
    childNode.parents = [] as LinkedList

    when:
    childNode.addParent(parentNode, false)

    then:
    with(parentNode) {
      children.size() == 0
    }
    with(childNode) {
      parents.size() == 1
      parents.contains(parentNode)
    }
  }
}