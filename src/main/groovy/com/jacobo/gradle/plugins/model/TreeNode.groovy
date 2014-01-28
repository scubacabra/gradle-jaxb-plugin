package com.jacobo.gradle.plugins.model

import org.gradle.api.logging.Logging
import org.gradle.api.logging.Logger

class TreeNode<T> {
  static final Logger log = Logging.getLogger(TreeNode.class)

  T data
  LinkedList<TreeNode<T>> parents
  LinkedList<TreeNode<T>> children
  TreeNode<T> next

  public TreeNode(T data) {
    this.parents = null
    this.children = new LinkedList<TreeNode<T>>()
    this.data = data
  }

  public TreeNode(T data, LinkedList<TreeNode<T>> parents) {
    this.parents = parents
    this.children = new LinkedList<TreeNode<T>>()
    this.data = data
  }

  public TreeNode(T data, TreeNode<T> parent) {
    this.parents = new LinkedList<TreeNode<T>>() {{ add(parent) }}
    this.children = new LinkedList<TreeNode<T>>()
    this.data = data
  }

  public void addChild(TreeNode<T> childNode) {
    log.debug("Adding a child node '{}' to parent '{}'", childNode.data, this.data)
    this.children.add(childNode)
    childNode.addParent(this)
  }

  public TreeNode<T> addChild(T data) {
    log.debug("Adding a new child node '{}' to parent '{}'", data, this.data)
    def childNode = new TreeNode<T>(data, this)
    this.children.add(childNode)
    return childNode
  }

  public void addParent(TreeNode<T> parent) {
    log.debug("Adding parent node '{}' to child Node '{}'", parent.data, this.data)
    this.parents.add(parent)
  }
}