package com.jacobo.gradle.plugins.model

import org.gradle.api.logging.Logging
import org.gradle.api.logging.Logger

import com.jacobo.gradle.plugins.structures.NamespaceData

class TreeManager {
  static final Logger log = Logging.getLogger(TreeManager.class)

  // managed set of Nodes (list of nodes already laid out on graph)
  def managedNodes = [] as Set
  // the current row in the tree of laid out Node
  def currentNodeRow = [] // TODO BETTER NAME WOULD BE TREE ROW CURRENT NODES
  // List of base namespaces that each compose their own tree
  // shares node objects if necessary
  def treeBaseNamespaces = [] as LinkedList

  def createTreeRoot(List<NamespaceData> baseNamespaces) { 
    baseNamespaces.each { namespace ->
      def node = new TreeNode<NamespaceData>(namespace)
      managedNodes << node
      treeBaseNamespaces << node
    }
    currentNodeRow = treeBaseNamespaces
  }

  def addChildren(List<NamespaceData> children) { 
    children.each { child ->
      def parentNodes = currentNodeRow.findAll { node ->
	child.dependentNamespaces.contains(node.data.namespace)
      }
      log.debug("Parents of child '{}' are '{}'", child, parentNodes.data)
      // child nodes can be redundant -- check to if this namespace is managed
      def childNode = managedNodes.find{
	node -> node.data.namespace == child.namespace }
      log.debug("Child node for namespace '{}' '{}' found",
		child, "is" + (childNode ?  "": "not"))
      parentNodes.each { parentNode ->
	log.debug("Trying to add child '{}' to Parent Node '{}'",
		  child, parentNode.data)
	if(childNode == null) // new child node, not managed yet
	  childNode = parentNode.addChild(child)
	else // managed child node
	  parentNode.addChild(childNode)
      }
      managedNodes << childNode
    }

    this.newCurrentTreeRowNodes(children)
  }

  // Finds the only possible children namespaces allowed to go on the
  // next tree row.  Of all the dependent Namespaces passed in, it must
  // pass 2 tests to be qualified.  These are:
  //
  // TODO what happens when find no children? error thrown?
  def findNextChildrenNamespaces(List<NamespaceData> namespaces) { 
    def nextChildren = []
    log.debug("Find next children from namespaces '{}'", namespaces)
    namespaces.each { namespace ->
      if (isGraphableAtNextRow(namespace)) {
	nextChildren << namespace	  
      }
    }
    log.debug("Children To layout at next tree row are '{}'", nextChildren)
    return nextChildren
  }

  // is graphable if has at least one dependency on the current row
  // and all other dependencies are managed
  def isGraphableAtNextRow(NamespaceData namespace) {
    def dependentNamespaces = namespace.dependentNamespaces
    def currentRowNamespaces = this.currentNodeRow.data.namespace
    def managedNamespaces = this.managedNodes.data.namespace
    log.debug("'{}' depends on namespaces '{}' -- Current Row is '{}'",
		namespace, dependentNamespaces, currentRowNamespaces)

    // dependencies are only one level up, so can't be bigger than
    // nodes in current row
    if (dependentNamespaces.size() > this.currentNodeRow.size()) {
      return false
    }
    
    for(dependentNamespace in dependentNamespaces) {
      //not in the current Nodes namespaces, not able to graph next
      if (!currentRowNamespaces.contains(dependentNamespace)) {
	return false
      }
    }
    // graphable
    return true
  }

  // reset the row pointer back to the base tree row
  // return the currentNodeRow back up
  def resetRowPointer() { 
    this.currentNodeRow = this.treeBaseNamespaces
    return this.currentNodeRow
  }

  // Takes the newest added namespace Data and finds their nodes from
  // managedNodes and gives currentNodeRow a new value of all those
  // that match
  def newCurrentTreeRowNodes(List<NamespaceData> newestAddedNamespaces) { 
    def newCurrentNodeRow = [] as LinkedList
    def newNamespaces = newestAddedNamespaces.namespace
    this.managedNodes.each { node ->
      if (newNamespaces.contains(node.data.namespace)) {
	newCurrentNodeRow << node
      }
    }
    currentNodeRow = newCurrentNodeRow
  }

  // return nodes in next row, based on nodes in current row
  // children nodes can be duplicated, therefore put in set
  def nextNodeRow(Set<TreeNode<NamespaceData>> currentRow) { 
    def nextRow  = [] as Set
    currentRow.each { node ->
      def children = node.children
      if (children.isEmpty())
	return

      nextRow.addAll(children)
    }

    if (nextRow.isEmpty())
      return null

    return nextRow
  }
  
  // recursively retrieve parent nodes for a given child node
  // parent nodes can be duplicated at some point, therefore put in set
  // Returns a Set of parent Nodes, sorted highest to lowest in the tree Hierarchy
  def getParents(TreeNode<NamespaceData> node) {
    log.debug("getting parents for '{}'", node)
    def ancestors = [] as Set
    if(node.parents == null)
      return []

    node.parents.each { parent ->
      def grandparents = getParents(parent)
      ancestors.addAll(grandparents)
      ancestors.add(parent)
    }
    
    log.debug("'{}' has '{}' ancestors", ancestors.size())
    return ancestors
  }
}