package org.gradle.jacobo.plugins.model

import org.gradle.jacobo.plugins.DependencyTreeSpecification

class TreeNodeSpec extends DependencyTreeSpecification {
  
  def "Get node fields correctly from one arg constructor"() { 
    given:
    def node = new TreeNode(ns1)

    expect:
    node.data == ns1
    node.children.size() == 0
    node.parents == null
  }

  def "Add child data to a node, expecting a child node returned"() { 
    given:
    def node = new TreeNode(ns1)
    
    when:
    def child = node.addChild(ns3)

    then:
    node.children.size() == 1
    node.children[0] == child
    child.data == ns3
    child.parents.size() == 1
    child.parents[0] == node
    child.children.size() == 0
  }

  def "Add parent to child node, manually"() { 
    given:
    def node = new TreeNode(ns1)
    def child = new TreeNode(ns3)
    child.parents = [node] as LinkedList
    def otherParent = new TreeNode(ns2)
    
    when:
    child.addParent(otherParent)

    then:
    child.data == ns3
    child.parents.size() == 2
    child.parents[0] == node
    child.parents[1] == otherParent
  }

  def "Add child node, not data, to parent"() { 
    given:
    def node = new TreeNode(ns1)
    def child = new TreeNode(ns3, node)
    def otherParent = new TreeNode(ns2)
    
    when:
    otherParent.addChild(child)

    then:
    otherParent.children.size() == 1
    otherParent.children[0] == child
    child.data == ns3
    child.parents.size() == 2
    child.parents[0] == node
    child.parents[1] == otherParent
  }
}