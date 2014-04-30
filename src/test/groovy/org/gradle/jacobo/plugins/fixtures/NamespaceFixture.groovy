package org.gradle.jacobo.plugins.fixtures

import org.gradle.jacobo.schema.BaseSchemaDocument
import org.gradle.jacobo.plugins.fixtures.DocumentFixture
import org.gradle.jacobo.plugins.xsd.XsdNamespace

class NamespaceFixture extends DocumentFixture {
  def xsdNamespaces = ["xsd1", "xsd2", "xsd3", "xsd4", "xsd5",
		       "xsd6", "xsd7", "xsd8", "xsd9"];
  def documents
  def namespaces = []

  def setup() {
    documents = xsdNamespaces.collect {
      createXsdDocument(new File(it + ".xsd"), it)
    }
    // xsd1 and xsd 2 will ALWAYS be the base namespaces for these fixtures
    ["xsd1", "xsd2"].each {
      def doc = documents.find { it.xsdNamespace == it }
      namespaces << createNamespace(it, doc)
    }
  }
  
  /**
   * create simple data for a tree "template"
   * all singly linked, all depend on one direct parent
   * 6 total namespaces involved
   * 1 <- 3 <- 5
   * 2 <- 4 <- 6
   * no external namespace dependencies
   * mock slurpers resolved imports
   */
  def createSimpleDependencyData() {
    ["xsd3":["xsd1"], "xsd4":["xsd2"], "xsd5":["xsd3"],"xsd6":["xsd4"]].each {
      k,v ->
      def doc = documents.find { it.xsdNamespace == k }
      namespaces << createNamespace(k, doc, true)
      def imports = documents.findAll { v.contains(it.xsdNamespace) }.documentFile
      xsdSlurper.findResolvedXsdImports(k, _, _) >> imports
    }
  }

  /**
   * create simple data for a tree "template"
   * all singly linked, all depend on one direct parent
   * 6 total namespaces involved
   * 1 <- 3 <- 5
   * 2 <- 4 <- 6
   * external namespace dependencies on a few
   * external dependencies only show up as a Set of Strings, no
   * BaseSchemaDocuments are associated with them
   * mock slurpers resolved imports to document file
   */
  def createSimpleDependencyDataWithExternalDeps() {
    ["xsd3":["xsd1", "e1"], "xsd4":["xsd2"], "xsd5":["xsd3", "e2", "e3"],
     "xsd6":["xsd4"]].each
    { k,v ->
      def doc = documents.find { it.xsdNamespace == k }
      namespaces << createNamespace(k, doc, true)
      def imports = v.collect {
	if (it.contains("e")) {
	  return new File(it + ".xsd")
	}
	documents.find { document -> document.xsdNamespace == it }.documentFile
      }
      xsdSlurper.findResolvedXsdImports(k, _, _) >> imports
    }
  }

  /**
   * create slightly complex data for 'tree' scheme
   * mutli children --  all depend on direct parent, no ancestors
   * 9 total namespaces involved
   * 1 <- 3; 1 <- 4; 3 <- 6; 3 <- 7;
   * 2 <- 4' 2 <- 5; 
   * 4 <- 8;
   * 5 <- 8; 5 <- 9;
   * no external namespace dependencies
   * mock slurpers resolved imports
   */
  def createMultiChildParentData() {
    ["xsd3":["xsd1"], "xsd4":["xsd1", "xsd2"], "xsd5":["xsd2"],"xsd6":["xsd3"],
     "xsd7":["xsd3"], "xsd8":["xsd4", "xsd5"], "xsd9":["xsd5"]].each {
      k,v ->
      def doc = documents.find { it.xsdNamespace == k }
      namespaces << createNamespace(k, doc, true)
      def imports = documents.findAll { v.contains(it.xsdNamespace) }.documentFile
      xsdSlurper.findResolvedXsdImports(k, _, _) >> imports
    }
  }

  /**
   * create fixture for a situation where a namespace is dependent
   * on an ancestor (at least one level above parent).  The parent
   * or some ancestor of the parent depends on this jump.  So it would
   * in the dependency chain.
   * 9 total namespaces involved
   * 1 <- 3; 1 <- 4;
   * 1 <- 6; <-- this is dependent on an ancestor
   * 3 <- 6; 3 <- 7;
   * 2 <- 4' 2 <- 5; 
   * 4 <- 8;
   * 5 <- 8; 5 <- 9;
   * no external namespace dependencies
   * mock slurpers resolved imports
   */
  def createRelatedAncestorDependencyScheme() {
    ["xsd3":["xsd1"], "xsd4":["xsd1", "xsd2"], "xsd5":["xsd2"],
     "xsd6":["xsd1", "xsd3"], "xsd7":["xsd3"], "xsd8":["xsd4", "xsd5"],
     "xsd9":["xsd5"]].each {
      k,v ->
      def doc = documents.find { it.xsdNamespace == k }
      namespaces << createNamespace(k, doc, true)
      def imports = documents.findAll { v.contains(it.xsdNamespace) }.documentFile
      xsdSlurper.findResolvedXsdImports(k, _, _) >> imports
    }
  }

  /**
   * create fixture for a situation where a namespace is dependent
   * on an unrelated ancestor (above the parent tree row) but not in the dependency
   * chain.
   * 9 total namespaces involved
   * 1 <- 3; 1 <- 4;
   * 2 <- 6; <-- this is dependent on an un-related ancestor
   * 3 <- 6; 3 <- 7;
   * 2 <- 4' 2 <- 5; 
   * 4 <- 8;
   * 5 <- 8; 5 <- 9;
   * no external namespace dependencies
   * mock slurpers resolved imports
   */
  def createUnRelatedAncestorDependencyScheme() {
    ["xsd3":["xsd1"], "xsd4":["xsd1", "xsd2"], "xsd5":["xsd2"],
     "xsd6":["xsd2", "xsd3"], "xsd7":["xsd3"], "xsd8":["xsd4", "xsd5"],
     "xsd9":["xsd5"]].each {
      k,v ->
      def doc = documents.find { it.xsdNamespace == k }
      namespaces << createNamespace(k, doc, true)
      def imports = documents.findAll { v.contains(it.xsdNamespace) }.documentFile
      xsdSlurper.findResolvedXsdImports(k, _, _) >> imports
    }
  }

  def createNamespace(String namespace, BaseSchemaDocument document) {
    return new XsdNamespace(namespace, [document])
  }

  def createNamespace(String namespace, BaseSchemaDocument document,
		      Set<String> externalDependencies) {
    def ns = new XsdNamespace(namespace, [document])
    ns.externalDependencies = externalDependencies
    return ns
  }

  def createNamespace(String namespace, BaseSchemaDocument document,
		      boolean hasDependencies) {
    def ns = new XsdNamespace(namespace, [document])
    ns.hasDependencies = hasDependencies
    return ns
  }

  def createNamespace(String namespace, BaseSchemaDocument document,
		      boolean hasDependencies, Set<String> externalDependencies) {
    def ns = new XsdNamespace(namespace, [document])
    ns.hasDependencies = hasDependencies
    ns.externalDependencies = externalDependencies
    return ns
  }

  def createNamespace(String namespace, List<BaseSchemaDocument> documents) {
    return new XsdNamespace(namespace, documents)
  }

  def createNamespace(String namespace, List<BaseSchemaDocument> documents,
		      boolean hasDependencies) {
    def ns = new XsdNamespace(namespace, documents)
    ns.hasDependencies = hasDependencies
    return ns
  }

  def createNamespace(String namespace, List<BaseSchemaDocument> documents,
		      Set<String> externalDependencies) {
    def ns = new XsdNamespace(namespace, documents)
    ns.externalDependencies = externalDependencies
    return ns
  }

  def createNamespace(String namespace, List<BaseSchemaDocument> documents,
		      boolean hasDependencies, Set<String> externalDependencies) {
    def ns = new XsdNamespace(namespace, documents)
    ns.hasDependencies = hasDependencies
    ns.externalDependencies = externalDependencies
    return ns
  }

  /**
   * get the baseNamespaces passed in to the treeManager
   * @param baseNamespaces - list of namespace strings that are used to
   * find the namespace objects that are passed to treeManager.createTreeRoot
   * @return list of namespaces to expect as input to createTreeRoot
   */
  def getBaseNamespaces(baseNamespaces) {
    baseNamespaces.collect { baseNamespace ->
      namespaces.find { it.namespace == baseNamespace }
    }
  }
}