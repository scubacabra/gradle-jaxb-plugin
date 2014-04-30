package org.gradle.jacobo.plugins.factory

import com.google.inject.Inject
import org.gradle.api.logging.Logging
import org.gradle.api.logging.Logger

import org.gradle.jacobo.schema.BaseSchemaDocument
import org.gradle.jacobo.plugins.model.TreeManager
import org.gradle.jacobo.plugins.xsd.XsdNamespace

class XsdDependencyTreeFactory {
  static final Logger log = Logging.getLogger(XsdDependencyTreeFactory.class)

  TreeManager treeManager

  @Inject
  public XsdDependencyTreeFactory(TreeManager treeManager) {
    this.treeManager = treeManager
  }

  public TreeManager createDependencyTree(List<XsdNamespace> namespaces,
					  List<BaseSchemaDocument> documents) {
    log.lifecycle("jaxb: generating xsd namespace dependency tree")
    treeManager.createTreeRoot(namespaces.findAll { it.hasDependencies == false })
    def haveDependencies = namespaces.findAll { it.hasDependencies == true }
    def namespaceDependencies = resolveDependentNamespaces(haveDependencies,
							   documents)
    while(!namespaceDependencies.isEmpty()) {
      def managedNamespaces = treeManager.managedNodes.data.namespace
      def currentRowNamespaces = treeManager.currentTreeRow.data.namespace
      def meetsCriteria = namespaceDependencies.findAll {namespace, dependencies ->
	// at least one dependency needs to be in the current Tree Row
	if (!dependencies.any{ currentRowNamespaces.contains(it)} ) return false
	// all dependencies should  be managed already
	if (dependencies.any{ !managedNamespaces.contains(it)} ) return false
	//meets criteria -- should be found
	return true
      }
      meetsCriteria.each { namespaceDependencies.remove(it.key) }
      treeManager.addChildren(meetsCriteria)
    }

    return treeManager
  }

  def Map<XsdNamespace, Set<String>> resolveDependentNamespaces(
    List<XsdNamespace> namespaces, List<BaseSchemaDocument> documents) {
    def namespaceDependencies = [:]
    namespaces.each { namespace ->
      def dependentNamespaces = [] as Set
      def dependencies = []
      namespace.documents.each { document ->
	// could resolve a file of an external dependency
	dependencies.addAll(document.findResolvedXsdImports())
      }
      dependencies.each { dependency ->
	def document = documents.find { doc -> doc.documentFile == dependency}
	// could not find dependency in documents because could be external
	if (document) {
	  dependentNamespaces << document.xsdNamespace	  
	}
      }
      namespaceDependencies[namespace] = dependentNamespaces - namespace.externalDependencies
    }
    return namespaceDependencies
  }
}