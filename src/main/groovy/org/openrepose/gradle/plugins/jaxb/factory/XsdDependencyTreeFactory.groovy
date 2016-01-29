package org.openrepose.gradle.plugins.jaxb.factory

import com.google.inject.Inject
import org.gradle.api.logging.Logging
import org.gradle.api.logging.Logger
import org.openrepose.gradle.plugins.jaxb.tree.TreeManager
import org.openrepose.gradle.plugins.jaxb.schema.BaseSchemaDocument
import org.openrepose.gradle.plugins.jaxb.xsd.XsdNamespace

/**
 * Generates an XSD Dependency Tree.
 */
class XsdDependencyTreeFactory {
  static final Logger log = Logging.getLogger(XsdDependencyTreeFactory.class)

  /**
   * Holds and manages the xsd dependency tree.
   */
  TreeManager treeManager

  /**
   * Creates this factory.
   *
   * @param treeManager  holds and manages the xsd dependency tree
   */
  @Inject
  public XsdDependencyTreeFactory(TreeManager treeManager) {
    this.treeManager = treeManager
  }

  /**
   * Creates the xsd dependency tree.
   * Hierarchy from 0 dependencies, to many dependencies.
   * Namespaces with 0 dependencies are put in tree first, followed by namespaces
   * that meet the following criteria:
   * <ul>
   * <li> At <i>least</i> one dependency is on immediate parents
   * <li> All dependencies for a namespace should be managed already
   * </ul>
   * <p>
   * This allows for a namespace to depend on ancestors, but this keeps the hierarchy
   * intact, as a namespace will always get parsed after all of its dependencies have
   * been resolved prior.
   * 
   * @param namespaces  the namespaces to move into a tree structure
   * @param documents  the documents of all xsds (searched for immediate dependencies)
   * @return the dependency tree's manager
   */
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

  /**
   * Resolves dependencies of documents in namespaces that have dependencies.
   * Find all documents that belong to namespaces that have dependencies, then
   * goes through them to find their (immediate) dependencies.
   * <p>
   * External Dependencies (dependencies on xsds that are not located in the
   * user defined xsdDir) are not included in this resolution.  That data need
   * not be present to correctly place the namespace in its dependency tree.
   *
   * @param namesapces  namespaces with dependencies
   * @param documents  schema documents from all xsds being operated on
   * @return map with object key to value of a string of namespaces the object
   *         depends on
   */
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