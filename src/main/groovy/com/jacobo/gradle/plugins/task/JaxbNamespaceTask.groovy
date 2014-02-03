package com.jacobo.gradle.plugins.task

import org.gradle.api.logging.Logging
import org.gradle.api.logging.Logger
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.DefaultTask

import com.jacobo.gradle.plugins.structures.NamespaceData
import com.jacobo.gradle.plugins.JaxbPlugin
import com.jacobo.gradle.plugins.model.DocumentSlurper
import com.jacobo.gradle.plugins.reader.DocumentReader
import com.jacobo.gradle.plugins.model.TreeNode
import com.jacobo.gradle.plugins.model.TreeManager
import groovy.io.FileType
import com.google.common.annotations.VisibleForTesting

/**
 * @author jacobono
 * Created: Tue Dec 04 09:01:34 EST 2012
 */

class JaxbNamespaceTask extends DefaultTask { 
  
  static final Logger log = Logging.getLogger(JaxbNamespaceTask.class)
  
  @InputDirectory
  File xsdDirectory

  @TaskAction
  void start() {
    log.lifecycle("jaxb: finding and slurping xsd files in '{}'", xsdDirectory)
    def xsdFiles = this.findAllXsdFiles( getXsdDirectory() )
    def slurpedDocuments = this.slurpXsdFiles(xsdFiles)
    log.lifecycle("jaxb: grouping files by individual namespace", xsdDirectory)
    def groupedByNamespace = this.groupSlurpedDocumentsByNamespace(
      slurpedDocuments)
    def groupedNamespaces = this.groupNamespaces(groupedByNamespace)
    log.lifecycle("jaxb: resolving individual namespace dependencies", xsdDirectory)
    def slurpedFileHistory = this.resolveNamespaceDependencies(
      slurpedDocuments, groupedNamespaces)
    def dependencyTree = this.generateDependencyTree(groupedNamespaces)
    this.resolveExternalDependencies(groupByNamespace.keySet(),
				     groupedNamespaces, slurpedFileHistory)
    project.jaxb.dependencyGraph = dependencyTree
    log.info( "all {} (unique) namespaces have been ordered and saved for parsing",
	      treeManager.managedNodes.size())
  }

  /**
   * @param operatingDirectory --> the directory to search for all *.xsd files
   * @return xsdFiles --> a list of all xsd Files
   * Finds all the xsd files in #operatingDirectory
   */
  @VisibleForTesting
  public List<File> findAllXsdFiles(File operatingDirectory) {
    if ( !operatingDirectory.exists() ) // operating Directory does not exist
      throw new RuntimeException(
	"Configured operating directory '{}' does not exist",
	operatingDirectory)

    log.info("Finding all XSD files in '{}'", operatingDirectory)
    def xsdFiles = []
    operatingDirectory.eachFileRecurse(FileType.FILES) {  xsdFile -> 
      if(xsdFile.name.split("\\.")[-1] == 'xsd') {
	xsdFiles << xsdFile
      }
    }
    return xsdFiles
  }

  /**
   * @param xsdFiles the xsd files to slurp data from (only namespace slurping)
   * @return slurpedDocuments --> a list of slurped documents
   * Takes each xsd file and slurps important data from it
   * (namespace, includes, and imports)
   */
  @VisibleForTesting
  def slurpXsdFiles(List<File> xsdFiles) {
    log.info("slurping '{}' files", xsdFiles.size())
    def slurpedDocuments = []
    xsdFiles.each { xsdFile ->
      def slurpedDocument = DocumentReader.slurpDocument(xsdFile)
      slurpedDocument.slurpNamespace()
      slurpedDocuments << slurpedDocument
    }
    return slurpedDocuments
  }

  /**
   * @param slurpedDocuments --> the slurped documents to group by namespace
   * @return groupedNamespaces --> a map of grouped namespaces with the namespace string as key and a list of #DocumentSlurpers as a value
   * Go through each slurped document and group documents by their namespace
   */
  @VisibleForTesting    
  def groupSlurpedDocumentsByNamespace(List<DocumentSlurper> slurpedDocuments) {
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
  def groupNamespaces(Map<String, List<DocumentSlurper>> groupedByNamespaces) { 
    def groupedNamespaces = []
    groupedByNamespaces.each { namespace, slurpedDocuments -> //key, value
      def namespaceData = new NamespaceData(namespace, slurpedDocuments)
      groupedNamespaces << namespaceData
    }
    return groupedNamespaces
  }

  @VisibleForTesting
  def resolveNamespaceDependencies(List<DocumentSlurper> slurpedDocuments,
				   List<NamespaceData> groupedNamespaces) {
    // was going to use a Set, but the Namespace Data needs the element
    // not really a way to do that with a set, but Map<File, DocumentSlurper>
    // good enough.  Take slurped objects and get their doc Files, put in hashmap
    def historySlurpedFiles = [:]
    slurpedDocuments.each { slurped ->
      historySlurpedFiles.put(slurped.documentFile, slurped)
    }
    log.info("previously slurped '{}' documents", historySlurpedFiles.size())
    groupedNamespaces.each { namespace -> 
      historySlurpedFiles = namespace.findDependedNamespaces(
	groupByNamespace.keySet(), historySlurpedFiles)
    }
    return historySlurpedFiles
  }
    log.info("'{}' slurped documents total", historySlurpedFiles.size())

  @VisibleForTesting
  def resolveExternalDependencies(Set<String> operatingNamespaces,
				  List<NamespaceData> groupedNamespaces,
				  Map<File, DocumentSlurper> slurpedFileHistory) { 
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
    // done laying out treeNodes, reset pointer to base, return base row
    return treeManager.resetRowPointer()
  }
}