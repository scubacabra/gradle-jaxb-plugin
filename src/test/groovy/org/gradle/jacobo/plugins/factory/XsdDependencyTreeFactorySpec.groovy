package org.gradle.jacobo.plugins.factory

import org.gradle.jacobo.plugins.fixtures.NamespaceFixture
import org.gradle.jacobo.plugins.xsd.XsdNamespace
import org.gradle.jacobo.plugins.tree.TreeNode
import org.gradle.jacobo.plugins.tree.TreeManager

import spock.lang.Unroll

class XsdDependencyTreeFactorySpec extends NamespaceFixture {

  def treeManager = Mock(TreeManager)  
  def factory = new XsdDependencyTreeFactory(treeManager)

  def "resolve Dependent Namespaces with a simple dependency scheme"() {
    given: "simple case -- no external dependencies"
    createSimpleDependencyData()
    def haveDependencies = namespaces.findAll { it.hasDependencies == true }

    when:
    def result = factory.resolveDependentNamespaces(haveDependencies, documents)

    then:
    result.size() == haveDependencies.size
    ["xsd3":["xsd1"], "xsd4":["xsd2"], "xsd5":["xsd3"],"xsd6":["xsd4"]].each {
      k,v ->
	def namespace = namespaces.find { it.namespace == k }
	with(result[namespace]) {
	  it.size() == v.size
	  it.containsAll(v as Set) == true
	}
    }
  }

  def "resolve Dependent Namespaces with a multi child and parent dependency scheme"() {
    given: "generate dependency scheme"
    createMultiChildParentData()
    def haveDependencies = namespaces.findAll { it.hasDependencies == true }

    when:
    def result = factory.resolveDependentNamespaces(haveDependencies, documents)

    then:
    result.size() == haveDependencies.size
    ["xsd3":["xsd1"], "xsd4":["xsd1", "xsd2"], "xsd5":["xsd2"],"xsd6":["xsd3"],
     "xsd7":["xsd3"], "xsd8":["xsd4", "xsd5"], "xsd9":["xsd5"]].each {
      k,v ->
	def namespace = namespaces.find { it.namespace == k }
	with(result[namespace]) {
	  it.size() == v.size
	  it.containsAll(v as Set) == true
	}
    }
  }

  def "resolve Dependent Namespaces with a simple model, but containing external dependencies"() {
    given: "generate dependency scheme"
    createSimpleDependencyDataWithExternalDeps()
    def haveDependencies = namespaces.findAll { it.hasDependencies == true }

    when:
    def result = factory.resolveDependentNamespaces(haveDependencies, documents)

    then:
    result.size() == haveDependencies.size
    ["xsd3":["xsd1"], "xsd4":["xsd2"], "xsd5":["xsd3"],"xsd6":["xsd4"]].each {
      k,v ->
	def namespace = namespaces.find { it.namespace == k }
	with(result[namespace]) {
	  it.size() == v.size
	  it.containsAll(v as Set) == true
	}
    }
  }

  @Unroll
  def "#testDescription"() {
    given: "setup test data"
    this."create$dependencyType"()
    
    and: "get expecations for tree root and addChildren"
    def noDeps = getBaseNamespaces(baseNamespaces)
    def expectations = addChildrenExpectations(addChildrenExpectations)

    and: "stub out manager getter calls"
    stubManagerGetCurrentRow(currentRows)
    stubManagerGetManagedNodes(managedNodes)
    
    when:
    def result = factory.createDependencyTree(namespaces, documents)

    then:
    1 * treeManager.createTreeRoot(noDeps)
    expectations.each { expectation ->
      with(treeManager) {
	1 * addChildren(expectation)
      }
    }

    where:
    testDescription << [
      "create Xsd Namespace Tree  with a simple dependency scheme",
      "create Xsd Namespace Tree with a multi child and parent dependency scheme",
      "create Xsd Namespace Tree with a multi child <=> parent and related ancestor dependency scheme",
      "create Xsd Namespace Tree with a multi child <=> parent and un-related ancestor dependency scheme"     
    ]
    dependencyType << [
      "SimpleDependencyData",
      "MultiChildParentData",
      "RelatedAncestorDependencyScheme",
      "UnRelatedAncestorDependencyScheme"]
    baseNamespaces = ["xsd1", "xsd2"]
    addChildrenExpectations << [
      [["xsd3":["xsd1"],"xsd4":["xsd2"]], ["xsd5":["xsd3"],"xsd6":["xsd4"]]],
      [["xsd3":["xsd1"], "xsd4":["xsd1", "xsd2"], "xsd5":["xsd2"]],
       ["xsd6":["xsd3"], "xsd7":["xsd3"], "xsd8":["xsd4", "xsd5"],
	"xsd9":["xsd5"]]
      ],
      [
      	["xsd3":["xsd1"], "xsd4":["xsd1", "xsd2"], "xsd5":["xsd2"]],
      	["xsd6":["xsd1", "xsd3"], "xsd7":["xsd3"], "xsd8":["xsd4", "xsd5"],
      	 "xsd9":["xsd5"]]
      ],
      [
	["xsd3":["xsd1"], "xsd4":["xsd1", "xsd2"], "xsd5":["xsd2"]],
	["xsd6":["xsd3", "xsd2"], "xsd7":["xsd3"], "xsd8":["xsd4", "xsd5"],
	 "xsd9":["xsd5"]]
      ]
    ]
    currentRows << [
      [["xsd1", "xsd2"], ["xsd3", "xsd4"]],
      [["xsd1", "xsd2"], ["xsd3", "xsd4", "xsd5"]],
      [["xsd1", "xsd2"], ["xsd3", "xsd4", "xsd5"]],
      [["xsd1", "xsd2"], ["xsd3", "xsd4", "xsd5"]]
    ]
    managedNodes << [
      [["xsd1", "xsd2"], ["xsd1", "xsd2", "xsd3", "xsd4"]],
      [["xsd1", "xsd2"], ["xsd1", "xsd2", "xsd3", "xsd4", "xsd5"]],
      [["xsd1", "xsd2"], ["xsd1", "xsd2", "xsd3", "xsd4", "xsd5"]],
      [["xsd1", "xsd2"], ["xsd1", "xsd2", "xsd3", "xsd4", "xsd5"]]
    ]
  }

  /**
   * get a list of map expectations that should be passed to
   * treeManager.addChildren()
   * @param expecation - map of namespace keys to list of namespaces
   * keys are to find the namespace that is put as the key of the input
   * to addChildren().  list of namespaces or the values associated with those
   * keys, as a Set of Strings
   * @return expectations - list of maps
   */
  def addChildrenExpectations(def expectation) {
    def expectations = []
    expectation.each { map ->
      def meetsCriteria = [:]
      map.each { k,v ->
	def dependentNamespace = namespaces.find { namespace ->
	  namespace.namespace == k
	}
	meetsCriteria[dependentNamespace] = v as Set
      }
      expectations << meetsCriteria
    }
    return expectations
  }
  
  /**
   * stub the calls to treeManager.currentTreeRow
   * 'mock' treenode so to speak, with the appropriate namespace it would hold
   * stub out calls in order of input, after the only allowed calls, return null 
   * for this operation
   * Finds namespace associated with input strings, mocks a tree node with found
   * namespace data object, and populates a list of lists to mock as return values
   * @param currentRows - list of lists (namespace strings) to iterate over
   */
  def stubManagerGetCurrentRow(def currentRows) {
    def currentRowReturns = []
    currentRows.each { currentRow ->
      def nodes = []
      currentRow.each { currentNamespace ->
	def data = namespaces.find { namespace ->
	  namespace.namespace == currentNamespace
	}
	def node = new TreeNode(data)
	nodes << node
      }
      currentRowReturns << nodes      
    }
    treeManager.getCurrentTreeRow() >>> currentRowReturns >> null
  }

  /**
   * stub the calls to treeManager.managedNodes
   * 'mock' treenode so to speak, with the appropriate namespace it would hold
   * stub out calls in order of input, after the only allowed calls, return null 
   * for this operation
   * Finds namespace associated with input strings, mocks a tree node with found
   * namespace data object, and populates a list of lists to mock as return values
   * @param managedNodes - list of lists (namespace strings) to iterate over
   */
  def stubManagerGetManagedNodes(def managedNodes) {
    def managedNodesReturns = []
    managedNodes.each { managed ->
      def nodes = [] as Set
      managed.each { managedNamespace ->
	def data = namespaces.find { namespace ->
	  namespace.namespace == managedNamespace
	}
	def node = new TreeNode(data)
	nodes << node
      }
      managedNodesReturns << nodes
    }
    // only return values defined, else null
    treeManager.getManagedNodes() >>> managedNodesReturns >> null
  }
}