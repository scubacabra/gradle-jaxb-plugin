package com.jacobo.gradle.plugins.structures

import spock.lang.Specification

class OrderGraphSpec extends Specification {
  
  def og = new OrderGraph()
  def ns1, ns2, ns3, ns4, ns5, namespaceData

  def setup() {
    ns1 = new NamespaceMetaData(namespace: "ns1")
    ns2 = new NamespaceMetaData(namespace: "ns2")
    ns3 = new NamespaceMetaData(namespace: "ns3")
    ns4 = new NamespaceMetaData(namespace: "ns4")
    ns5 = new NamespaceMetaData(namespace: "ns5")
    namespaceData = []
  }

  def "populate namespace data initially when everything is still null" () {
  when:
  og.obtainUniqueNamespaces(xsd)

  then:
  og.namespaceData.size == 1
  og.namespaceData.get(0).namespace == targetNamespace
  og.namespaceData.get(0).slurpers.size == 1
  og.namespaceData.get(0).slurpers.document == xsdFiles*.absoluteFile

  where:
  xsd = new File(this.getClass().getResource("/schema/House/Kitchen.xsd").toURI())
  targetNamespace = "http://www.example.org/Kitchen"
  xsdFiles = [new File("build/resources/test/schema/House/Kitchen.xsd")]

  }
  
  def "try populating namespace data with the same namespace you previously populated" () { 
  when:
  og.obtainUniqueNamespaces(xsd)
  og.obtainUniqueNamespaces(sameNamespace)

  then:
  og.namespaceData.size == 1
  og.namespaceData.get(0).namespace == targetNamespace
  og.namespaceData.get(0).slurpers.size == 2
  og.namespaceData.get(0).slurpers.document == xsdFiles*.absoluteFile

  where:
  xsd = new File(this.getClass().getResource("/schema/House/Kitchen.xsd").toURI())
  sameNamespace = new File(this.getClass().getResource("/schema/House/KitchenSubset.xsd").toURI())
  targetNamespace = "http://www.example.org/Kitchen"
  xsdFiles = [new File("build/resources/test/schema/House/Kitchen.xsd"), new File("build/resources/test/schema/House/KitchenSubset.xsd")]

  }

  def "Populate namespace data with two different namespaces" () { 
  when:
  og.obtainUniqueNamespaces(xsd)
  og.obtainUniqueNamespaces(newNamespace)

  then:
  og.namespaceData.size == 2
  og.namespaceData.namespace == targetNamespace
  og.namespaceData.slurpers.size == 2
  og.namespaceData.slurpers.document == xsdFiles*.absoluteFile

  where:
  xsd = new File(this.getClass().getResource("/schema/House/Kitchen.xsd").toURI())
  newNamespace = new File(this.getClass().getResource("/schema/House/LivingRoom.xsd").toURI())
  targetNamespace = ["http://www.example.org/Kitchen", "http://www.example.org/LivingRoom"]
  xsdFiles = [[new File("build/resources/test/schema/House/Kitchen.xsd")], [new File("build/resources/test/schema/House/LivingRoom.xsd")]]

  }

  def "Grab the base target namespace data and make sure it is of type NamespaceMetaData"() { 
  setup: 
  namespaceData = [ns1, ns2, ns3, ns4]
  namespaceData.each { it.dependsOnAnotherNamespace = true }
    
  when:
  og.namespaceData = namespaceData
  og.gatherIndependentNamespaces()

  then: "order size is 1, but the inner list has 4 elements, all should be of type NamespaceData"
  og.namespaceData.size == 4
  og.orderGraph.size() == 1
  og.orderGraph[0].size() == 4
  og.orderGraph[0].namespace == ["ns1", "ns2", "ns3", "ns4"]
  og.orderGraph[0].each { it instanceof NamespaceMetaData  }
  
  }

  def "two elements are the base namespace elements, two others are dependent. grab the 2 dependent elements"() { 
  setup:
  namespaceData = [ns1, ns2, ns3, ns4]
  [ns1, ns2].each { it.importedNamespaces << "none" }

  when:
  og.namespaceData = namespaceData
  og.orderGraph[0] = [ns1, ns2]
  def dependentNamespaces = og.getDependentNamespaces()

  then:
  og.namespaceData.size == 4
  og.orderGraph.size() == 1
  og.orderGraph[0].size() == 2
  og.orderGraph[0].namespace == ["ns1", "ns2"]
  dependentNamespaces.size == 2
  dependentNamespaces.find{ it == ns3 } == ns3
  dependentNamespaces.find{ it == ns4 } == ns4
  
  }

  def "is Imported Namespace in Order Graph?" () { 
  setup: 
  og.namespaceData = [ns1, ns2, ns3, ns4]
  [ns1, ns2].each { it.importedNamespaces << "none" }
  og.orderGraph[0] = [ns1, ns2]

  expect: "finding an imported ns1 is found, ns4 is not there, so flag it"
  og.findImportedNamespacesInOrderGraph(ns1) == [false, 0]
  og.findImportedNamespacesInOrderGraph(ns4) == [true]
  
  }

  def "add Namespace to the graph with all imports matching what is currently in the graph" () { 
  setup: "ns1 and ns2 are base namespaces and are in the order Graph at level 0 (first level)"
    namespaceData = [ns1, ns2, ns3, ns4]
    [ns1, ns2].each { it.importedNamespaces << "none" }
    ns3.importedNamespaces = [ns1, ns2]
    og.namespaceData = namespaceData
    og.orderGraph[0] = [ns1, ns2]

  when: "ns3 depends on ns1 and ns2, so it has matches at both first levels, all matching, add ns3 to level 2, index 1"
    og.addNamespaceToOrderGraph(ns3, [0,0], [])

  then: "two levels in graph one object in each level"
    og.orderGraph.size == 2
    og.orderGraph[1].size == 1
    og.orderGraph[1] == [ns3]

  }

  def "graph out two namespaces to the graph, check to see that they map out correctly" () {
  setup: "ns1 and ns2 are base namespaces and are in the order Graph at level 0 (first level)"
    namespaceData = [ns1, ns2, ns3, ns4]
    [ns1, ns2].each { it.importedNamespaces << "none" }
    ns3.importedNamespaces = [ns1, ns2]
    ns4.importedNamespaces = [ns3]
    og.namespaceData = namespaceData
    og.orderGraph[0] = [ns1, ns2]

  when: "ns3 depends on ns1 and ns2, so it has matches at both first levels, all matching, add ns3 to level 2, index 1"
    og.arrangeDependentNamespacesInOrder()

  then: "two levels in graph one object in each level"
    og.orderGraph.size == 3
    og.orderGraph[1].size == 1
    og.orderGraph[1] == [ns3]
    og.orderGraph[2].size == 1
    og.orderGraph[2] == [ns4]

  }
  def "add Namespace to the graph with an import that isn't found in the graph" () { 

  setup: "ns1 and ns2 are base namespaces and are in the order Graph at level 0 (first level)"
    og.namespaceData = [ns1, ns2, ns3, ns4]
    [ns1, ns2].each { it.importedNamespaces << "none" }
    ns4.importedNamespaces = [ns1, ns3]
    og.orderGraph[0] = [ns1, ns2]

  when: "ns4 depends on ns3, so add ns3 to level 2 and ns4 to level 3"
    og.addNamespaceToOrderGraph(ns4, [0], [ns3])

  then: "because of missing import, levels are 3, one element in each level"
    og.orderGraph.size == 3
    og.orderGraph[1].size == 1
    og.orderGraph[1] == [ns3]
    og.orderGraph[2].size == 1
    og.orderGraph[2] == [ns4]
    og.notMatchingMap.isEmpty() == false
    og.notMatchingMap.size() == 1
    og.notMatchingMap*.key == ["ns3"]
    og.notMatchingMap*.value == [1]

  }

  def "add Namespace to the graph that was already added as an unmatching import, must remove the whole row, only entry" () { 
  setup: "four level order graph"
    og.namespaceData = [ns1, ns2, ns3, ns4, ns5]
    [ns1, ns2].each { it.importedNamespaces << "none" }
    ns3.importedNamespaces = [ns2]
    ns5.importedNamespaces = [ns1]
    og.orderGraph = [[ns1, ns2],
		     [ns5], 
		     [ns3],
		     [ns4]]
    og.notMatchingMap = ["ns5" : 1]

  when: "adding namespace that is in notMatching map, process parent namespace already in graph"
    og.processParentNamespaceAlreadyInGraph(ns5, 0)
   

  then: "since you removed a level, now only three levels, two elements in 1 and and one element in levels 2 and 3, ns5 is removed"
    og.orderGraph.size == 3
    og.orderGraph[1].size == 1
    og.orderGraph[1] == [ns3]
    og.orderGraph[2].size == 1
    og.orderGraph[2] == [ns4]
    og.notMatchingMap.isEmpty() == true
  }

  def "add Namespaces that was added before without a match, process the namespace and remove it from the graph, but make sure it only removes that element, and you still have 3 levels of dependency ordering" () { 
  setup: "three level order graph"
    og.namespaceData = [ns1, ns2, ns3, ns4, ns5]
    [ns1, ns2].each { it.importedNamespaces << "none" }
    ns3.importedNamespaces = [ns5]
    ns4.importedNamespaces = [ns2]
    ns5.importedNamespaces = [ns4]
    og.orderGraph = [[ns1, ns2],
		     [ns4, ns5], 
		     [ns3]]
    og.notMatchingMap = ["ns5" : 1]

  when: "adding namespace that is in notMatching map, process parent namespace already in graph"
    og.processParentNamespaceAlreadyInGraph(ns5, 1)
   

  then: "since you only removed an element at that level, still three levels, two elements in 1 and and one element in levels 2 and 3, ns5 is removed from level 1"
    og.orderGraph.size == 3
    og.orderGraph[1].size == 1
    og.orderGraph[1] == [ns4]
    og.orderGraph[2].size == 1
    og.orderGraph[2] == [ns3]
    og.notMatchingMap.isEmpty() == true
  }

  def "add Namespace to the graph that was already added as an unmatching import, must remove the whole row, only entry, add to the max plus one level" () { 
  setup: "four level order graph"
    og.namespaceData = [ns1, ns2, ns3, ns4, ns5]
    [ns1, ns2].each { it.importedNamespaces << "none" }
    ns3.importedNamespaces = [ns2]
    ns4.importedNamespaces = [ns3]
    ns5.importedNamespaces = [ns1]
    og.orderGraph = [[ns1, ns2],
		     [ns5], 
		     [ns3],
		     [ns4]]
    og.notMatchingMap = ["ns5" : 1]

  when: "adding namespace that is in notMatching map, process parent namespace already in graph"
    og.addNamespaceToOrderGraph(ns5, [0], [])
   

  then: "since you removed a level, now only three levels, two elements in 1 and and one element in levels 2 and 3, ns5 is removed"
    og.orderGraph.size == 3
    og.orderGraph[1].size == 2
    og.orderGraph[1] == [ns3, ns5]
    og.orderGraph[2].size == 1
    og.orderGraph[2] == [ns4]
    og.notMatchingMap.isEmpty() == true
  }

  def "add Namespace to the graph that was already added as an unmatching import, unmatching import is imported by namespace 3 so if ns5 shifts anywhere, ns3 should be followed right behind it" () { 
  setup: "three level order graph"
    og.namespaceData = [ns1, ns2, ns3, ns4, ns5]
    [ns1, ns2].each { it.importedNamespaces << "none" }
    ns3.importedNamespaces = [ns5]
    ns4.importedNamespaces = [ns2]
    ns5.importedNamespaces = [ns4]
    og.orderGraph = [[ns1, ns2],
		     [ns4, ns5], 
		     [ns3]]
    og.notMatchingMap = ["ns5" : 1]

  when: "adding namespace that is in notMatching map, process parent namespace already in graph"
    og.addNamespaceToOrderGraph(ns5, [1], [])
   

  then: "since you removed a level, now only three levels, two elements in 1 and and one element in levels 2 and 3, ns5 is removed"
    og.orderGraph.size == 4
    og.orderGraph[1].size == 1
    og.orderGraph[1] == [ns4]
    og.orderGraph[2].size == 1
    og.orderGraph[2] == [ns5]
    og.orderGraph[3].size == 1
    og.orderGraph[3] == [ns3]
    og.notMatchingMap.isEmpty() == true
  }

  def "add Namespace to the graph that was already added as an unmatching import, ns3 and ns5 import ns4, so move ns5 to the 3rd level with ns3" () { 
  setup: "three level order graph"
    og.namespaceData = [ns1, ns2, ns3, ns4, ns5]
    [ns1, ns2].each { it.importedNamespaces << "none" }
    ns3.importedNamespaces = [ns4]
    ns4.importedNamespaces = [ns2]
    ns5.importedNamespaces = [ns4]
    og.orderGraph = [[ns1, ns2],
		     [ns4, ns5], 
		     [ns3]]
    og.notMatchingMap = ["ns5" : 1]

  when: "adding namespace that is in notMatching map, process parent namespace already in graph"
    og.addNamespaceToOrderGraph(ns5, [1], [])
   

  then: "since you removed a level, now only three levels, two elements in 1 and and one element in levels 2 and 3, ns5 is removed"
    og.orderGraph.size == 3
    og.orderGraph[1].size == 1
    og.orderGraph[1] == [ns4]
    og.orderGraph[2].size == 2
    og.orderGraph[2] == [ns3, ns5]
    og.notMatchingMap.isEmpty() == true
  }

  def "process external imports, each of the three NamespaceMetaData has the same external import" () { 
  setup: "make the external import"
    def emd = new ExternalNamespaceMetaData()
    emd.namespace = "http://www.example.org/Kitchen"
    emd.externalSchemaLocation = new File(this.getClass().getResource("/schema/testImports/Kitchen.xsd").toURI()).absoluteFile
    [ns1, ns2, ns3].each { it.externalImportedNamespaces << emd }
    og.namespaceData = [ns1, ns2, ns3, ns4, ns5]

  when: "process the external imports"
    og.obtainExternalImportsDependencies()

  then: "order graph external dependencies should be of length one, and the ns ext imports should be populated"
    og.externalDependencies.size == 1
    og.externalDependencies[0].externalImportedNamespaces.size == 2
    og.externalDependencies[0].externalImportedNamespaces*.namespace == ["http://www.example.org/LivingRoom", "http://www.example.org/Den"]
    og.externalDependencies[0] == emd
    ns1.externalImportedNamespaces.size == 1
    ns1.externalImportedNamespaces[0].externalImportedNamespaces.size() == 2
    ns1.externalImportedNamespaces[0].externalImportedNamespaces*.namespace == ["http://www.example.org/LivingRoom", "http://www.example.org/Den"]
    ns1.externalImportedNamespaces[0] == emd
    ns2.externalImportedNamespaces.size == 1
    ns2.externalImportedNamespaces[0].externalImportedNamespaces.size() == 2
    ns2.externalImportedNamespaces[0].externalImportedNamespaces*.namespace == ["http://www.example.org/LivingRoom", "http://www.example.org/Den"]
    ns2.externalImportedNamespaces[0] == emd
    ns3.externalImportedNamespaces.size == 1
    ns3.externalImportedNamespaces[0].externalImportedNamespaces.size() == 2
    ns3.externalImportedNamespaces[0].externalImportedNamespaces*.namespace == ["http://www.example.org/LivingRoom", "http://www.example.org/Den"]
    ns3.externalImportedNamespaces[0] == emd
    
  }


}