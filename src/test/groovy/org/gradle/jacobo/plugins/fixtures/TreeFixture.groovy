package org.gradle.jacobo.plugins.fixtures

import org.gradle.jacobo.plugins.fixtures.NamespaceFixture
import org.gradle.jacobo.plugins.model.TreeNode

class TreeFixture extends NamespaceFixture {

  def nodes
  
  def setup() {
    createMultiChildParentData()
    nodes = namespaces.collect { new TreeNode(it) }
    [[namespace: "xsd1", children: ["xsd3", "xsd4"]],
     [namespace: "xsd2", children: ["xsd4", "xsd5"]],
     [namespace: "xsd3", parents: ["xsd1"], children: ["xsd6", "xsd7"]],
     [namespace: "xsd4", parents: ["xsd1", "xsd2"], children: ["xsd8"]],
     [namespace: "xsd5", parents: ["xsd2"], children: ["xsd8", "xsd9"]],
     [namespace: "xsd6", parents: ["xsd3"]],
     [namespace: "xsd7", parents: ["xsd3"]],
     [namespace: "xsd8", parents: ["xsd4", "xsd5"]],
     [namespace: "xsd9", parents: ["xsd5"]]
    ].each { map ->
      def node = nodes.find { it.data.namespace == map.namespace }
      if (map.parents) {
	def parents =  nodes.findAll { map.parents.contains(it.data.namespace) }
	node.parents = parents
      }
      if (map.children) {
	def chitlins = nodes.findAll { map.children.contains(it.data.namespace) }
	node.children = chitlins
      }
    }
  }
}