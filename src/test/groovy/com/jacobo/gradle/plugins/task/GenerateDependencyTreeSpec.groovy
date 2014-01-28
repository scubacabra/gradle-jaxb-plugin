package com.jacobo.gradle.plugins.task

import com.jacobo.gradle.plugins.ProjectTaskSpecification
import com.jacobo.gradle.plugins.structures.NamespaceData
import com.jacobo.gradle.plugins.JaxbPlugin

class GenerateDependencyTreeSpec extends ProjectTaskSpecification {
  // Namespace objects
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

  def setup() {
    task = project.tasks[JaxbPlugin.JAXB_NAMESPACE_GRAPH_TASK] as JaxbNamespaceTask
  }

  def "two elements are the base namespace elements, two depend on one different namespace each, the other 3 depend on each of the 3 first children"() {
    setup:
      def namespaces = [ns1, ns2, ns3, ns4, ns5, ns6, ns7, ns8]
      def graphedNamespaces = ["ns1", "ns2", "ns3", "ns4", "ns5", "ns6", "ns7", "ns8"]
      def dependentNamespaces = ["ns3", "ns4", "ns5", "ns6", "ns7", "ns8"]
      def baseNamespaces = ["ns1", "ns2"]

    when:
      def dependencyRoot = task.generateDependencyTree(namespaces)

    then:
      dependencyRoot.size() == 2
      dependencyRoot.each { it.parents == null }
      dependencyRoot.each { it.children.isEmpty() == false }
      dependencyRoot.each { it.data.namespace.each { baseNamespaces.contains(it) } }
      dependencyRoot.each { it.children.data.namespace.each { dependentNamespaces.contains(it) } }
    }

  def "two elements are the base namespace elements, two others depend on each of the base namespaces"() {
    setup:
    def namespaces = [ns1, ns2, ns3, ns5]
    def graphedNamespaces = ["ns1", "ns2", "ns3", "ns5"]
    def dependentNamespaces = ["ns3", "ns5"]
    def baseNamespaces 	= ["ns1", "ns2"]

    when:
      def dependencyRoot = task.generateDependencyTree(namespaces)

    then:
      dependencyRoot.size() == 2
      dependencyRoot.each { it.parents == null }
      dependencyRoot.each { it.children.isEmpty() == false }
      dependencyRoot.each { it.data.namespace.each { baseNamespaces.contains(it) } }
      dependencyRoot.each { it.children.data.namespace.each { dependentNamespaces.contains(it) } }
  }

  def "Generate the dependency tree, all 4 namespace objects are base object, they depend on no other namespaces"() { 
    setup: 
      def namespaces = [ns1, ns2, new NamespaceData("ns3", []), new NamespaceData("ns4", [])]
      def graphedNamespaces = ["ns1", "ns2", "ns3", "ns4"]

    when:
      def dependencyRoot = task.generateDependencyTree(namespaces)

    then: "order size is 1, but the inner list has 4 elements, all should be of type NamespaceData"
      dependencyRoot.size() == 4
      dependencyRoot.each { it.parents == null }
      dependencyRoot.each { it.children.isEmpty() == true }
      dependencyRoot.each { it.data.namespace.each{ graphedNamespaces.contains(it) } }
  }
}