package org.gradle.jacobo.plugins.tree

import java.io.Serializable

import org.gradle.api.logging.Logging
import org.gradle.api.logging.Logger

import org.gradle.jacobo.plugins.xsd.XsdNamespace

class TreeManager {
  static final Logger log = Logging.getLogger(TreeManager.class)

  // managed set of Nodes (list of nodes already laid out on graph)
  def managedNodes = [] as Set

  // the current row in the tree of laid out Node
  def currentTreeRow = []

  // List of base namespaces that each compose their own tree
  // shares node objects if necessary
  def treeRoot = [] as LinkedList

  def createTreeRoot(List<XsdNamespace> baseNamespaces) {
    log.info("creating baseNamespaces '{}' as tree nodes", baseNamespaces.size)
    baseNamespaces.each { namespace ->
      def node = new TreeNode<XsdNamespace>(namespace)
      managedNodes << node
      treeRoot << node
    }
    currentTreeRow = treeRoot
  }

  def addChildren(Map<XsdNamespace, Set<String>> children) {
    def addedNodes = []
    children.each { child, dependencies ->
      def parents = null
      def ancestors = null
      def dependsOn = managedNodes.findAll { node ->
	dependencies.contains(node.data.namespace)
      }
      // dependencies aren't just represented in current row
      if (!dependencies.every { currentTreeRow.data.namespace.contains(it) }) {
	parents = dependsOn.findAll { currentTreeRow.contains(it) }
	ancestors = dependsOn.findAll { !currentTreeRow.contains(it) }
	log.debug("Child '{}' depends on parents '{}', AND ancestors '{}'", child, parents, ancestors)
      }
      // all dependent in current row, all dependencies are parents
      if (parents == null) parents = dependsOn
      log.debug("adding child '{}' as a tree leaf, belonging to parents '{}'", child, parents)
      def node = new TreeNode(child, parents as LinkedList)
      // if there are ancestors, need to add a one way parent link to them
      ancestors?.each { ancestor ->
	log.debug("adding a 1-way reationship (child => parent) for child '{}', depending on ancestor '{}'", child, ancestor)
	node.addParent(ancestor, false)
      }
      managedNodes << node
      addedNodes << node
    }
    currentTreeRow = addedNodes
  }

  // return nodes in next row, based on nodes in current row
  // children nodes can be duplicated, therefore put in set
  def Set<TreeNode<XsdNamespace>> getNextDescendants(
    Collection<TreeNode<XsdNamespace>> currentNodes) {
    def descendants  = [] as Set
    currentNodes.each { node ->
      def children = node.children
      if(children.isEmpty()) return true // no children, go no further
      descendants.addAll(children)
    }
    if (descendants.isEmpty()) return null // no descendants, return null
    return descendants
  }
}