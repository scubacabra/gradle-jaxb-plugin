package org.gradle.jacobo.plugins.task

import org.gradle.api.logging.Logging
import org.gradle.api.logging.Logger
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.InputFiles
import org.gradle.api.file.FileTree
import org.gradle.api.DefaultTask

import org.gradle.jacobo.schema.factory.DocumentFactory
import org.gradle.jacobo.schema.BaseSchemaDocument
import org.gradle.jacobo.plugins.structures.NamespaceData
import org.gradle.jacobo.plugins.JaxbPlugin
import org.gradle.jacobo.plugins.model.TreeNode
import org.gradle.jacobo.plugins.model.TreeManager
import com.google.common.annotations.VisibleForTesting

/**
 * @author jacobono
 * Created: Tue Dec 04 09:01:34 EST 2012
 */

class JaxbNamespaceTask extends DefaultTask { 
  
  static final Logger log = Logging.getLogger(JaxbNamespaceTask.class)
  
  @InputFiles
  FileTree xsds

  DocumentFactory docFactory

  @TaskAction
  void start() {
    log.lifecycle("jaxb: starting Namespace Task")
    def xsdFiles = getXsds().files
    def documents = xsdFiles.collect{file -> docFactory.createDocument(file)}
    log.info("jaxb: grouping '{}' files by individual namespace", xsdFiles.size())
    def groupedByNamespace = this.groupSlurpedDocumentsByNamespace(documents)
    def groupedNamespaces = this.groupNamespaces(groupedByNamespace)
    log.info("jaxb: resolving '{}' individual namespace dependencies", groupedNamespaces.size())
    def slurpedFileHistory = this.resolveNamespaceDependencies(
      slurpedDocuments, groupedNamespaces, groupedByNamespace.keySet())
    log.lifecycle("jaxb: generating xsd namespace dependency tree")
    def dependencyTreeManager = this.generateDependencyTree(groupedNamespaces)
    log.lifecycle("jaxb: resolving indiviual namespace external dependencies")
    this.resolveExternalDependencies(groupedByNamespace.keySet(),
				     groupedNamespaces, slurpedFileHistory)
    project.jaxb.dependencyGraph = dependencyTreeManager
  }

  /**
   * @param slurpedDocuments --> the slurped documents to group by namespace
   * @return groupedNamespaces --> a map of grouped namespaces with the namespace string as key and a list of #BaseSchemaDocuments as a value
   * Go through each slurped document and group documents by their namespace
   */
  @VisibleForTesting    
  def groupSlurpedDocumentsByNamespace(List<BaseSchemaDocument> slurpedDocuments) {
    log.info("grouping '{}' documents by their unique namespaces", slurpedDocuments.size())
    // key is namepsace string, value is List of DocumentSlurped objects
    def groupedNamespaces = [:]
    slurpedDocuments.each { slurpedDocument ->
      //already in map, just add to list
      if (groupedNamespaces.containsKey(slurpedDocument.xsdNamespace)) {
	groupedNamespaces[slurpedDocument.xsdNamespace] << slurpedDocument
	return true
      }

      // new to map, need a new list as well
      groupedNamespaces[slurpedDocument.xsdNamespace] = [slurpedDocument]
    }
    return groupedNamespaces
  }


  @VisibleForTesting
  def groupNamespaces(Map<String, List<BaseSchemaDocument>> groupedByNamespaces) {
    log.info("creating '{}' NamespaceData objects", groupedByNamespaces.size())
    def groupedNamespaces = []
    groupedByNamespaces.each { namespace, slurpedDocuments -> //key, value
      def namespaceData = new NamespaceData(namespace, slurpedDocuments)
      groupedNamespaces << namespaceData
    }
    return groupedNamespaces
  }

  @VisibleForTesting
  def resolveNamespaceDependencies(List<BaseSchemaDocument> slurpedDocuments,
				   List<NamespaceData> groupedNamespaces,
				   Set<String> availableNamespaces) {
    // was going to use a Set, but the Namespace Data needs the element
    // not really a way to do that with a set, but Map<File, BaseSchemaDocument>
    // good enough.  Take slurped objects and get their doc Files, put in hashmap
    def historySlurpedFiles = [:]
    slurpedDocuments.each { slurped ->
      historySlurpedFiles.put(slurped.documentFile, slurped)
    }
    log.info("previously slurped '{}' documents", historySlurpedFiles.size())
    groupedNamespaces.each { namespace -> 
      historySlurpedFiles = namespace.findNamespacesDependentOn(
	availableNamespaces, historySlurpedFiles)
    }
    log.info("'{}' slurped documents total", historySlurpedFiles.size())
    return historySlurpedFiles
  }

  @VisibleForTesting
  def resolveExternalDependencies(Set<String> operatingNamespaces,
				  List<NamespaceData> groupedNamespaces,
				  Map<File, BaseSchemaDocument> slurpedFileHistory) {
    def namespacesWithExternalDeps = groupedNamespaces.findAll { namespace ->
      namespace.hasExternalDependencies
    }
    log.info("namespaces with external dependencies are '{}'",
	      namespacesWithExternalDeps)

    namespacesWithExternalDeps.each { namespace ->
      slurpedFileHistory = namespace.findAllNamespacesDependedOn(
	operatingNamespaces, slurpedFileHistory)
    }
  }
  
  /**
   * @param List<NamespaceData> groupedNamespaces
   * @return TreeManager
   * generates the dependency Tree and returns the manager of the tree
   */
  @VisibleForTesting
  def generateDependencyTree(List<NamespaceData> groupedNamespaces) { 
    def treeManager = new TreeManager()
    def noDependencies = groupedNamespaces.findAll{
      namespace -> namespace.hasDependencies == false }
    log.info("generating dependency tree, the '{}' base namespaces are '{}'",
	      noDependencies.size, noDependencies)
    treeManager.createTreeRoot(noDependencies)
    def dependentNamespaces = groupedNamespaces.findAll {
      namespace -> namespace.hasDependencies == true }
    log.info("attempting to layout '{}' namespaces on this graph --> '{}'",
	      dependentNamespaces.size, dependentNamespaces)
    // still have some namespaces left to layout!
    while(groupedNamespaces.size != treeManager.managedNodes.size()) {
      def nextChildren = treeManager.findNextChildrenNamespaces(dependentNamespaces)
      treeManager.addChildren(nextChildren)
    }
    log.info("dependencies layed out, total tree nodes of '{}'", treeManager.managedNodes.size())
    // done laying out treeNodes, reset pointer to base, return base row
    treeManager.resetRowPointer()
    return treeManager
  }
}