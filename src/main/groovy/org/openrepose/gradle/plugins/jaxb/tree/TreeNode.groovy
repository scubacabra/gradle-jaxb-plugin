package org.openrepose.gradle.plugins.jaxb.tree

import org.gradle.api.logging.Logging
import org.gradle.api.logging.Logger

/**
 * Tree Node for a type T.
 */
class TreeNode<T> {
  static final Logger log = Logging.getLogger(TreeNode.class)

  /**
   * Node's data.
   */
  T data

  // TECHNICALLY, order of these don't matter. could convert to Set.
  /**
   * Nodes parents.
   */
  LinkedList<TreeNode<T>> parents

  /**
   * Nodes children.
   */
  LinkedList<TreeNode<T>> children

  /**
   * Constructs a TreeNode with 0 parents and 0 children.
   * 
   * @param data data for TreeNode
   */
  public TreeNode(T data) {
    this.parents = null
    this.children = new LinkedList<TreeNode<T>>()
    this.data = data
  }

  /**
   * Constructs a TreeNode with parents and 0 children.
   * Sets this tree node as the child of all parents to make a
   * dual sided relationship.
   * 
   * @param data contents for TreeNode
   * @param parents parents of the node-to-be
   */
  public TreeNode(T data, LinkedList<TreeNode<T>> parents) {
    this.parents = parents
    this.children = new LinkedList<TreeNode<T>>()
    this.data = data
    parents.each { parent ->
      parent.addChild(this, false)
    }
  }

  /**
   * Constructs a TreeNode with 1 parent and 0 children.
   * Set this tree node as the parent's child to make a
   * dual sided relationship.
   * 
   * @param data contents for TreeNode
   * @param parents parents of the node-to-be
   */
  public TreeNode(T data, TreeNode<T> parent) {
    this.parents = new LinkedList<TreeNode<T>>() {{ add(parent) }}
    this.children = new LinkedList<TreeNode<T>>()
    this.data = data
    parent.addChild(this, false)
  }

  /**
   * Adds a child to the current node.
   * If a two way relationship is desired, child references this node as a
   * parent, and parent has as reference to this child.  {@code true} creates
   * this dual relationship, false creates a one sided relationship.
   * <p>
   * This comes in handy when a node depends on ancestors of the parent, the child needs to
   * reference the ancestor as a parent, but the reverse would allow the node to
   * be parsed far too early.
   *
   * @param child  child to add to this node
   * @param twoWayRelationship  create a dual sided (two way) relationship
   */
  public void addChild(TreeNode<T> child, boolean twoWayRelationship) {
    log.debug("Adding a child '{}' to node '{}'", child.data, this.data)
    this.children.add(child)
    if (twoWayRelationship) {
      log.debug("creating a two way relationship '{}'[p]<=>'{}'[c]",
		this.data, child.data)
      child.addParent(this, false)
    }
  }

  /**
   * Adds a parent to the current node.
   * If a two way relationship is desired, this node references the parent and
   * the parent has a reference to this node as a child. {@code true} creates
   * this dual relationship, false creates a one sided relationship.
   * <p>
   * This comes in handy when a node depends on ancestors of the parent, the child needs to
   * reference the ancestor as a parent, but the reverse would allow the node to
   * be parsed far too early.
   *
   * @param parent  parent to add to this node
   * @param twoWayRelationship  create a dual sided (two way) relationship
   */
  public void addParent(TreeNode<T> parent, boolean twoWayRelationship) {
    log.debug("Adding a parent '{}' to node '{}'", parent.data, this.data)
    this.parents.add(parent)
    if (twoWayRelationship) {
      log.debug("creating a two way relationship '{}'[p]<=>'{}'[c]",
		parent.data, this.data)
      parent.addChild(this, false)
    }
  }

  /**
   * Get ancestors, depth first.
   * Nodes could be duplicates depending on the ancestry path -- a set is necessary.
   *
   * @return node ancestors of current node
   */
  public Set<TreeNode<T>> getAncestors() {
    def ancestors = [] as Set
    log.debug("getting ancestors for node '{}'", this)
    if (parents == null) {
      return ancestors
    }
    parents.each { parent ->
      ancestors.addAll(parent.getAncestors())
      ancestors.add(parent)
    }
    log.debug("node '{}' has '{}' ancestors", this, ancestors.size())
    return ancestors
  }

  /**
   * Compares this node to another node.
   * Need to compare children AND parents.  But not delve into them.  Just
   * checking their size is sufficient.  If they are traversed, it leads to
   * an infinite loop in this comparison.
   *
   * @param other node to compare against this one
   */
  public boolean equals(Object other) {
    log.debug("'{}' == '{}' ??", this, other)
    if(!(other instanceof TreeNode)) return false
    if(other == null) return false
    if(this.is(other)) return true
    if(this.data != other.data) return false

    log.debug("compare children of '{}'", this)
    if(this.children.size() != other.children.size()) return false
    for(int i = 0; i < this.children.size(); i++) {
      log.debug("children '{}' --> '{}'", this.children[i], other.children[i])
      if(this.children[i].data != other.children[i].data) return false
      log.debug("children parents size '{}' --> '{}'",
          this.children[i].parents.size(), other.children[i].parents.size())
      if(this.children[i].parents.size() !=
        other.children[i].parents.size()) return false
      log.debug("children parents data '{}' --> '{}'",
          this.children[i].parents.data, other.children[i].parents.data)
      if(this.children[i].parents.data !=
	 other.children[i].parents.data) return false
      log.debug("grandchildren size '{}' --> '{}'", this.children[i].children.size(),
         other.children[i].children.size())
      if(this.children[i].children.size() !=
	 other.children[i].children.size()) return false
      log.debug("grandchildren data '{}' --> '{}'", this.children[i].children.data,
         other.children[i].children.data)
      if(this.children[i].children.data !=
	 other.children[i].children.data) return false
    }

    log.debug("compare parents of '{}'", this)
    //if(other.parents != this.parents) return false
    if(this.parents) {// parents not null
      // other is null, not equal
      if(!other.parents) return false
      if(this.children.size() != other.children.size()) return false
      for(int i = 0; i < this.parents.size(); i++) {
        log.debug("parents '{}' --> '{}'", this.parents[i], other.parents[i])
        if(this.parents[i].data != other.parents[i].data) return false
        if(this.parents[i].parents && other.parents[i].parents) {
	  log.debug("grandparents size '{}' --> '{}'",
              this.parents[i].parents.size(), other.parents[i].parents.size())
          if(this.parents[i].parents.size() !=
	     other.parents[i].parents.size()) return false
	  log.debug("grandparentsparents data '{}' --> '{}'", this.parents[i].parents.data,
             other.parents[i].parents.data)
          if(this.parents[i].parents.data !=
	     other.parents[i].parents.data) return false
        } else { // only keep going if both are null
          if( !(!this.parents[i].parents &&
		!other.parents[i].parents)) return false
        }
	log.debug("parents children size '{}' --> '{}'",
            this.parents[i].children.size(), other.parents[i].children.size())
        if(this.parents[i].children.size() !=
	   other.parents[i].children.size()) return false
	log.debug("parents children data '{}' --> '{}'", this.parents[i].children.data,
            other.parents[i].children.data)
        if(this.parents[i].children.data !=
	   other.parents[i].children.data) return false
      }
    } else { // parents is null
      // other is not null, not equal
      if(other.parents) return false
    }
    return true
  }

  /**
   * Generates the hash code for this node.
   *
   * @return this node's hash cod
   */
  public int hashCode() {
    int hash = 1
    hash = hash*17 + data.hashCode()
    if(parents) {
      def parentHash = 0
      for (parent in parents) {
        parentHash += parent.data.hashCode()
      }
      hash = hash*31 + parents.size() + parentHash
    }
    def childHash = 0
    for(child in children) {
      childHash += child.data.hashCode()
    }
    hash = hash*11 + children.size() + childHash
    return hash
  }

  String toString() {
    return data
  }

}