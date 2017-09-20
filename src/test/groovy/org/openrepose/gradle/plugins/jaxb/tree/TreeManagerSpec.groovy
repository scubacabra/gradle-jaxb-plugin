package org.openrepose.gradle.plugins.jaxb.tree

import org.openrepose.gradle.plugins.jaxb.fixtures.NamespaceFixture
import spock.lang.Unroll

class TreeManagerSpec extends NamespaceFixture {
  def manager = new TreeManager()

  @Unroll
  def "create '#baseNamespaces.size' root Nodes for tree"() { 
    given:
    def root = getBaseNamespaces(baseNamespaces)

    when:
    manager.createTreeRoot(root)

    then:
    with(manager) {
      managedNodes.size() == rootSize
      managedNodes.data.containsAll(root)
      currentTreeRow.size() == rootSize
      currentTreeRow.data.containsAll(root)
      treeRoot.size() == rootSize
      treeRoot.data.containsAll(root)
    }
    
    where:
    baseNamespaces   || rootSize
    ["xsd1"]	     || 1
    ["xsd1", "xsd2"] || 2
  }

  def "add one-to-one and many-to-one children, including a skipped related ancestor dependency, with a one sided parent only relationship (no child)"() {
    given:
    createRelatedAncestorDependencyScheme()
    manager.createTreeRoot(getBaseNamespaces(["xsd1", "xsd2"]))

    and:
    def children = getChildren(["xsd3":["xsd1"], "xsd4":["xsd1", "xsd2"],
				"xsd5":["xsd2"]])
    
    when:
    manager.addChildren(children)
    
    then: "managed Nodes and Current Row have certain expectation"
    with(manager) {
      managedNodes.size() == 5
      managedNodes.data.namespace.containsAll(["xsd1", "xsd2", "xsd3", "xsd4",
					       "xsd5"])
      currentTreeRow.size() == 3
      currentTreeRow.data.namespace.containsAll(["xsd3", "xsd4", "xsd5"])
    }

    when:
    children = getChildren(["xsd6":["xsd3","xsd1"], "xsd7":["xsd3"],
			    "xsd8":["xsd4", "xsd5"], "xsd9":["xsd5"]])
    manager.addChildren(children)
    
    then: "managed Nodes and Current Row have certain expectation"
    with(manager) {
      managedNodes.size() == 9
      managedNodes.data.namespace.containsAll(["xsd1", "xsd2", "xsd3", "xsd4",
					       "xsd5", "xsd6", "xsd7", "xsd8", "xsd9"])
      currentTreeRow.size() == 4
      currentTreeRow.data.namespace.containsAll(["xsd6", "xsd7", "xsd8", "xsd9"])
    }

    and: "Tree Node relationships should be"
    ["xsd1":["xsd3", "xsd4"], "xsd2":["xsd4", "xsd5"], "xsd3":["xsd6", "xsd7"],
     "xsd4":["xsd8"], "xsd5":["xsd8", "xsd9"]].each { k,v ->
      def node = manager.managedNodes.find { it.data.namespace ==  k}
      def chitlins = manager.managedNodes.findAll { v.contains(it.data.namespace) }
      with(node) {
	node.children.size() == chitlins.size()
	node.children.containsAll(chitlins)
      }
    }
    ["xsd3":["xsd1"], "xsd4":["xsd1", "xsd2"], "xsd5":["xsd2"],
     "xsd6":["xsd3", "xsd1"], "xsd7":["xsd3"], "xsd8":["xsd4", "xsd5"],
     "xsd9":["xsd5"]].each { k,v ->
      def node = manager.managedNodes.find { it.data.namespace ==  k}
      def parents = manager.managedNodes.findAll { v.contains(it.data.namespace) }
      with(node) {
	node.parents.size() == parents.size()
	node.parents.containsAll(parents)
      }
    }
  }

  def "add one-to-one and many-to-one children, including a skipped un-related ancestor dependency, with a one sided parent only relationship (no child)"() {
    given:
    createUnRelatedAncestorDependencyScheme()
    manager.createTreeRoot(getBaseNamespaces(["xsd1", "xsd2"]))

    and:
    def children = getChildren(["xsd3":["xsd1"], "xsd4":["xsd1", "xsd2"],
				"xsd5":["xsd2"]])
    
    when:
    manager.addChildren(children)
    
    then: "managed Nodes and Current Row have certain expectation"
    with(manager) {
      managedNodes.size() == 5
      managedNodes.data.namespace.containsAll(["xsd1", "xsd2", "xsd3", "xsd4",
					       "xsd5"])
      currentTreeRow.size() == 3
      currentTreeRow.data.namespace.containsAll(["xsd3", "xsd4", "xsd5"])
    }

    when:
    children = getChildren(["xsd6":["xsd2","xsd3"], "xsd7":["xsd3"],
			    "xsd8":["xsd4", "xsd5"], "xsd9":["xsd5"]])
    manager.addChildren(children)
    
    then: "managed Nodes and Current Row have certain expectation"
    with(manager) {
      managedNodes.size() == 9
      managedNodes.data.namespace.containsAll(["xsd1", "xsd2", "xsd3", "xsd4",
					       "xsd5", "xsd6", "xsd7", "xsd8", "xsd9"])
      currentTreeRow.size() == 4
      currentTreeRow.data.namespace.containsAll(["xsd6", "xsd7", "xsd8", "xsd9"])
    }

    and: "Tree Node relationships should be"
    ["xsd1":["xsd3", "xsd4"], "xsd2":["xsd4", "xsd5"], "xsd3":["xsd6", "xsd7"],
     "xsd4":["xsd8"], "xsd5":["xsd8", "xsd9"]].each { k,v ->
      def node = manager.managedNodes.find { it.data.namespace ==  k}
      def chitlins = manager.managedNodes.findAll { v.contains(it.data.namespace) }
      with(node) {
	node.children.size() == chitlins.size()
	node.children.containsAll(chitlins)
      }
    }
    ["xsd3":["xsd1"], "xsd4":["xsd1", "xsd2"], "xsd5":["xsd2"],
     "xsd6":["xsd2", "xsd3"], "xsd7":["xsd3"], "xsd8":["xsd4", "xsd5"],
     "xsd9":["xsd5"]].each { k,v ->
      def node = manager.managedNodes.find { it.data.namespace ==  k}
      def parents = manager.managedNodes.findAll { v.contains(it.data.namespace) }
      with(node) {
	node.parents.size() == parents.size()
	node.parents.containsAll(parents)
      }
    }
  }

  /**
   * get a map of Namespaces to a set of namespace Strings (dependencies)
   * to pass to treeManager.addChildren()
   * @param expectation - map of namespace keys to list of namespaces
   * keys are to find the namespace that is put as the key of the input
   * to addChildren().  list of namespaces or the values associated with those
   * keys, as a Set of Strings
   * @return expectations - list of maps
   */
  def getChildren(def children) {
    def meetsCriteria = [:]
    children.each { k,v ->
      def dependentNamespace = namespaces.find { namespace ->
	namespace.namespace == k
      }
      meetsCriteria[dependentNamespace] = v as Set
    }
    return meetsCriteria
  }
}