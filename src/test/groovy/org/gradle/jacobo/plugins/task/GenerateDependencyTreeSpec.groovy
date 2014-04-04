package org.gradle.jacobo.plugins.task

import org.gradle.testfixtures.ProjectBuilder
import org.gradle.api.Project

import org.gradle.jacobo.plugins.DependencyTreeSpecification
import org.gradle.jacobo.plugins.structures.NamespaceData
import org.gradle.jacobo.plugins.model.TreeNode
import org.gradle.jacobo.plugins.JaxbPlugin

class GenerateDependencyTreeSpec extends DependencyTreeSpecification {

  def project
  def task

  def setup() { 
    project = ProjectBuilder.builder().build()
    project.apply(plugin: "jaxb")
    task = project.tasks[JaxbPlugin.JAXB_NAMESPACE_GRAPH_TASK] as JaxbNamespaceTask
  }

  def "two elements are the base namespace elements, two depend on one different namespace each, the other 3 depend on each of the 3 first children"() {
    setup:
      def namespaces = [ns1, ns2, ns3, ns4, ns5, ns6, ns7, ns8]

    when:
      def dependencyRoot = task.generateDependencyTree(namespaces)
      def firstNode = dependencyRoot.currentNodeRow[0]
      def secondNode = dependencyRoot.currentNodeRow[1]

    then:
      firstNode == node1
      secondNode == node2
      firstNode.children == [node3, node4]
      secondNode.children == [node4, node5]
      firstNode.children[0].children == [node6]
      firstNode.children[1].children == [node7]
      secondNode.children[1].children == [node8]
  }

  def "two elements are the base namespace elements, two others depend on each of the base namespaces"() {
    setup:
      def namespaces = [ns1, ns2, ns3, ns5]
      node1.children = [node3] as LinkedList
      node2.children = [node5] as LinkedList
      node3.children = [] as LinkedList
      node5.children = [] as LinkedList

    when:
      def dependencyRoot = task.generateDependencyTree(namespaces)
      def firstNode = dependencyRoot.currentNodeRow[0]
      def secondNode = dependencyRoot.currentNodeRow[1]

    then:
      firstNode == node1
      secondNode == node2
      firstNode.children == [node3]
      secondNode.children == [node5]
  }

  def "Generate the dependency tree, all 4 namespace objects are base object, they depend on no other namespaces"() { 
    setup: 
      def namespaces = [ns1, ns2, new NamespaceData("ns3", []), new NamespaceData("ns4", [])]
      def nodes = namespaces.collect{ new TreeNode(it) }

    when:
      def dependencyRoot = task.generateDependencyTree(namespaces)

    then:
      dependencyRoot.currentNodeRow == nodes
  }
}