package com.jacobo.gradle.plugins

import spock.lang.Specification
import com.jacobo.gradle.plugins.structures.NamespaceData
import com.jacobo.gradle.plugins.model.TreeNode

/**
 * utility methods that tests dealing with DependencyTree deal with
 */
class DependencyTreeSpecification extends BaseSpecification {

  // tree node data object
  def ns1 = new NamespaceData("ns1", []);
  def ns2 = new NamespaceData("ns2", []);
  def ns3 = new NamespaceData(namespace: "ns3",
			      dependentNamespaces: ["ns1"],
			      hasDependencies: true);
  def ns4 = new NamespaceData(namespace: "ns4", 
			      dependentNamespaces: ["ns1", "ns2"],
			      hasDependencies: true);
  def ns5 = new NamespaceData(namespace: "ns5",
			      dependentNamespaces: ["ns2"],
			      hasDependencies: true);
  def ns6 = new NamespaceData(namespace: "ns6",
			      dependentNamespaces: ["ns3"],
			      hasDependencies: true);
  def ns7 = new NamespaceData(namespace: "ns7",
			      dependentNamespaces: ["ns4"],
			      hasDependencies: true);
  def ns8 = new NamespaceData(namespace: "ns8",
			      dependentNamespaces: ["ns5"],
			      hasDependencies: true);

  // tree nodes
  def node1, node2, node3, node4, node5, node6, node7, node8

  def setup() {
    node1 = new TreeNode(ns1)
    node2 = new TreeNode(ns2)
    node3 = new TreeNode(ns3)
    node4 = new TreeNode(ns4)
    node5 = new TreeNode(ns5)
    node6 = new TreeNode(ns6)
    node7 = new TreeNode(ns7)
    node8 = new TreeNode(ns8)
    node1.children = [node3, node4] as LinkedList
    node2.children = [node4, node5] as LinkedList
    node3.children = [node6] as LinkedList
    node4.children = [node7] as LinkedList
    node5.children = [node8] as LinkedList
    node3.parents = [node1] as LinkedList
    node4.parents = [node1, node2] as LinkedList
    node5.parents = [node2] as LinkedList
    node6.parents = [node3] as LinkedList
    node7.parents = [node4] as LinkedList
    node8.parents = [node5] as LinkedList
  }
}