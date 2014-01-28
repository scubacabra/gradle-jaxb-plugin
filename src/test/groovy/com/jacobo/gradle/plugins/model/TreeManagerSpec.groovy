package com.jacobo.gradle.plugins.model

import com.jacobo.gradle.plugins.DependencyTreeSpecification
import com.jacobo.gradle.plugins.structures.NamespaceData
import spock.lang.Unroll

class TreeManagerSpec extends DependencyTreeSpecification {
  def manager = new TreeManager()

  def "create tree Roots"() { 
    given:
    def namespaceData = [ns1, ns2, new NamespaceData("ns3", []),
			 new NamespaceData("ns4", [])
			]
    when:
    manager.createTreeRoot(namespaceData)

    then:
    manager.managedNodes.size() == 4
    manager.managedNodes.each { node -> namespaceData.contains(node.data) }
    manager.currentNodeRow instanceof LinkedList == true
    manager.currentNodeRow.size == 4
    manager.currentNodeRow.each { node -> namespaceData.contains(node.data) }
    manager.treeBaseNamespaces.size == 4
    manager.treeBaseNamespaces.each { node -> 
      namespaceData.contains(node.data) }
  }

  def "add children to the base namespace data, immediate next row"() { 
    given:
    def baseNamespaceData = [ns1, ns2]
    def childNamespaces = [ns3, ns4, ns5]

    when:
    manager.createTreeRoot(baseNamespaceData)
    manager.addChildren(childNamespaces)
    def nsNode1 = manager.treeBaseNamespaces[0]
    def nsNode2 = manager.treeBaseNamespaces[1]

    then:
    manager.managedNodes.size() == 5
    manager.managedNodes.each { node -> 
      baseNamespaceData.contains(node.data) ||
      childNamespaces.contains(node.data)
    }
    manager.currentNodeRow instanceof LinkedList == true
    manager.currentNodeRow.size == 3
    manager.currentNodeRow.each { node -> childNamespaces.contains(node.data) }
    manager.treeBaseNamespaces.size == 2
    manager.treeBaseNamespaces.each { node -> 
      baseNamespaceData.contains(node.data)
    }
    nsNode1.children.size == 2
    nsNode1.parents == null
    nsNode1.children.each { child ->
      ["ns3", "ns4"].contains(child.data.namespace) }
    nsNode2.children.size == 2
    nsNode2.parents == null
    nsNode2.children.each { child ->
      ["ns4", "ns5"].contains(child.data.namespace) }
  }

  def "add two rows of children"() { 
    given:
    def baseNamespaceData = [ns1, ns2]
    def childNamespacesFirstRow = [ns3, ns4, ns5]
    def childNamespacesSecondRow = [ns6, ns7, ns8]

    when:
    manager.createTreeRoot(baseNamespaceData)
    manager.addChildren(childNamespacesFirstRow)
    manager.addChildren(childNamespacesSecondRow)
    def nsNode1 = manager.treeBaseNamespaces[0]
    def nsNode2 = manager.treeBaseNamespaces[1]
    def nsNode3 = nsNode1.children.find { node -> node.data.namespace == "ns3" }
    def nsNode4 = nsNode1.children.find { node -> node.data.namespace == "ns4" }
    def nsNode5 = nsNode2.children.find { node -> node.data.namespace == "ns5" }
    def nsNode6 = nsNode3.children.find { node -> node.data.namespace == "ns6" }
    def nsNode7 = nsNode4.children.find { node -> node.data.namespace == "ns7" }
    def nsNode8 = nsNode5.children.find { node -> node.data.namespace == "ns8" }

    then:
    manager.managedNodes.size() == 8
    manager.managedNodes.each { node -> 
      baseNamespaceData.contains(node.data) ||
      childNamespacesFirstRow.contains(node.data) ||
      childNamespacesSecondRow.contains(node.data)
    }
    manager.currentNodeRow instanceof LinkedList == true
    manager.currentNodeRow.size == 3
    manager.currentNodeRow.each { node ->
      childNamespacesSecondRow.contains(node.data) }
    manager.treeBaseNamespaces.size == 2
    manager.treeBaseNamespaces.each { node -> 
      baseNamespaceData.contains(node.data)
    }
    nsNode1.children.size == 2
    nsNode1.parents == null
    nsNode1.children.each { child -> ["ns3", "ns4"].contains(child.data.namespace) }
    nsNode2.children.size == 2
    nsNode2.parents == null
    nsNode2.children.each { child -> ["ns4", "ns5"].contains(child.data.namespace) }
    namespaceMatchesParameters(nsNode3, ["ns1"], ["ns6"])
    namespaceMatchesParameters(nsNode4, ["ns1", "ns2"], ["ns7"])
    namespaceMatchesParameters(nsNode5, ["ns2"], ["ns8"])
    namespaceMatchesParameters(nsNode6, ["ns3"], [])
    namespaceMatchesParameters(nsNode7, ["ns4"], [])
    namespaceMatchesParameters(nsNode8, ["ns5"], [])
  }

  def "get next node row, start with parents go down 3 until the tree has reached it's maximum height"() { 
    setup:
    manager.treeBaseNamespaces = [node1, node2] as LinkedList
    manager.managedNodes = [node1, node2, node3, node4, node5, node6, node7, node8] as Set

    when:
    def nextRow = manager.nextNodeRow(manager.treeBaseNamespaces as Set)

    then:
    nextRow.size() == 3
    ["ns3", "ns4", "ns5"].each { nextRow.data.namespace.contains(it) }

    when:
    nextRow = manager.nextNodeRow(nextRow)
    
    then:
    nextRow.size() == 3
    ["ns6", "ns7", "ns8"].each { nextRow.data.namespace.contains(it) }

    when:
    nextRow = manager.nextNodeRow(nextRow)

    then:
    nextRow == null
  }

  @Unroll
  def "is namespace with dependencies #dependentNamespaces graphable at next Tree Row ? #isGraphable"() { 
    setup:
    manager.currentNodeRow = [node3, node4, node5]
    manager.managedNodes = [node1, node2, node3, node4, node5, node6, node7,
			    node8
			   ]
    def namespace = new NamespaceData(dependentNamespaces: dependentNamespaces)

    when:
    def result = manager.isGraphableAtNextRow(namespace)

    then:
    result == isGraphable
    
    where:
    isGraphable << [true, true, false, false]
    // dependent on 1 in current, on all 3 in current, on more than what is in current, on something not managed yet
    dependentNamespaces << [["ns3"], ["ns3", "ns4", "ns5"], ["ns3", "ns4", "ns5", "ns6"], ["ns10"]]
  }

  void namespaceMatchesParameters(namespace, parentNamespaces, childrenNamespaces) { 
    assert namespace.parents.size() == parentNamespaces.size
    parentNamespaces.each { parent ->
      assert namespace.parents.data.namespace.contains(parent)
    }
    assert namespace.children.size() == childrenNamespaces.size
    childrenNamespaces.each { child ->
      assert namespace.children.data.namespace.contains(child)
    }
  }
}