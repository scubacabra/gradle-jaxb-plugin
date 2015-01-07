package org.gradle.jacobo.plugins.tree

import java.io.Serializable

import org.gradle.api.logging.Logging
import org.gradle.api.logging.Logger

import org.gradle.jacobo.plugins.xsd.XsdNamespace

/**
 * Manages a depenency tree.  Can add children to the tree, point to tree's "rows"
 * such as the root row or the current row.
 * <p>
 * This is necessary because the tree can have many parents and many children
 * in the same tree.  This leads to basically, every node with 0 dependencies
 * having its own tree in a tree, with nodes being shared across trees.
 * Children need to be added to many parents, and the reverse relationship of
 * children need to handled as well.
 * <p>
 * The manager keeps track of managed Nodes so that nodes do not get duplicated, keeps
 * pointers to the current Row of nodes, and to the root (row with no dependencies).
 * This allows future tasks delegate to this manager to traverse through all the
 * tree's rows.
 */
class TreeManager {
  static final Logger log = Logging.getLogger(TreeManager.class)

  /**
   * Tree nodes that are already manager by this manager.
   */
  def managedNodes = [] as Set

  /**
   * Current row of the dependency tree.
   */
  def currentTreeRow = []

  /**
   * Tree nodes that contain 0 dependencies.  Each node has its own mini
   * dependency tree. With nodes being shared to create the actual dependency
   * tree.
   */
  def treeRoot = [] as LinkedList

  /**
   * Creates the root row of this dependency tree.
   * New nodes are created, one for each namespace passed in.
   * this#currentTreeRow is set with this operation.
   * 
   * @param baseNamespaces  list of namespaces with 0 dependencies
   */
  def createTreeRoot(List<XsdNamespace> baseNamespaces) {
    log.info("creating baseNamespaces '{}' as tree nodes", baseNamespaces.size())
    baseNamespaces.each { namespace ->
      def node = new TreeNode<XsdNamespace>(namespace)
      managedNodes << node
      treeRoot << node
    }
    currentTreeRow = treeRoot
  }

  /**
   * Adds children namespaces to this dependency tree.
   *
   * @param children  map of namespace objects and dependency strings to add to
   *        this dependency tree. Children to add are the keys of the map passed in.
   */
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

  /**
   * Returns the next descendants in the tree from the passed in nodes.
   * Input is assumed to be the a row of nodes in the dependency tree.
   *
   * @param currentNodes  the current row of nodes being worked on
   * @return a set of the next nodes in the tree (if any).  If no more nodes
   *         {@code null} is returned
   */
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