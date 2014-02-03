package com.jacobo.gradle.plugins.structures

import com.jacobo.gradle.plugins.model.DocumentSlurper
import com.jacobo.gradle.plugins.reader.DocumentReader

import org.gradle.api.logging.Logging
import org.gradle.api.logging.Logger

/**
 * Model that contains a particular set of unique data for a Namespace for jaxb generation
 * @author jacobono
 * @date 12/19/12
 */
class NamespaceData { 

  static final Logger log = Logging.getLogger(NamespaceData.class)

  /**
   * the namespace for this group of file(s)
   */
  def namespace

  // list of slurped Documents with this namespace
  def slurpedDocuments

  boolean hasDependencies = false
  
  boolean hasExternalDependencies = false

  /**
   * a Set of strings that are the imported namespaces that all @parseFiles files have imported
   */
  def dependentNamespaces = [] as Set

  /**
   * a Set of strings that are externally imported namespaces that this namespace depends on
   */
  def dependentExternalNamespaces = [] as Set

  /**
   * a Set of File objects of the externally imported Namespaces
   */
  def dependentExternalFiles = [] as Set
  
  public NamespaceData() { }
  
  public NamespaceData(String namespace) {
    this.namespace = namespace
  }

  /**
   * basic constructor, need a namespace and a list of slurped documents
   */
  public NamespaceData(String namespace, List<DocumentSlurper> slurpedDocuments) { 
    this.namespace = namespace
    this.slurpedDocuments = slurpedDocuments
  }

  /**
   *
   */
  def findNamespacesDependentOn(Set<String> operatingNamespaces, Map<File, DocumentSlurper> historySlurpedFiles) {
    def importedDependencies = [] as Set
    log.debug("Gathering all the imported dependencies for namespace  '{}'",
	      this.namespace)

    slurpedDocuments.each { slurped ->
      importedDependencies.addAll(slurped.xsdImports)
    }

    // empty imported Dependencies, nothing else to do
    if( importedDependencies.isEmpty() ) 
      return historySlurpedFiles

    // go through each dependency File
    importedDependencies.each { dependencyFile -> 

      // already slurped, don't need to slurp again
      if (historySlurpedFiles.containsKey(dependencyFile)) { 
	this.addDependencyToNamespace(operatingNamespaces,
				      historySlurpedFiles.get(dependencyFile))
	return true 
      }

      def slurpedDependency = DocumentReader.slurpDocument(dependencyFile)
      slurpedDependency.slurpNamespace()
      historySlurpedFiles.put(dependencyFile, slurpedDependency)

      this.addDependencyToNamespace(operatingNamespaces, slurpedDependency)
    }

    if ( !this.dependentNamespaces.isEmpty() )
      hasDependencies = true

    if ( !this.dependentExternalFiles.isEmpty() ) { 
      hasExternalDependencies = true
    }

    return historySlurpedFiles
  }

  // Dependency could be external OR internal, add to the appropriate
  // data structure of this object
  def addDependencyToNamespace(Set<String> operatingNamespaces,
			       DocumentSlurper slurpedDocument) {
    // dependency is in the current namespace tree list (operating namespace)
    if (operatingNamespaces.contains(slurpedDocument.xsdNamespace)) {
      this.dependentNamespaces.add(slurpedDocument.xsdNamespace) 
      return
    }
    
    this.dependentExternalFiles.add(slurpedDocument.documentFile)
  }

  def findAllNamespacesDependedOn(operatingNamespaces, historySlurpedFiles) { 
    def unparsed = [] as Set // set of java.io.File
    dependentExternalFiles.each { dependentFile ->
      def slurper = historySlurpedFiles.get(dependentFile)
      this.dependentExternalNamespaces.add(slurper.xsdNamespace)
      unparsed.addAll(slurper.xsdImports)
    }

    while(!unparsed.isEmpty()) { 
      def dependentFile = unparsed.iterator().next()
      // already parsed, add to dependentExternalNamespaces, might be duplicated
      // must do this anyway, history could contain and save the trouble of parsing
      if (historySlurpedFiles.contains(document)) { 
	def slurper = historySlurpedFiles.get(document)
	this.dependentExternalNamespaces.add(slurper.xsdNamespace)
	unparsed.addAll(slurper.xsdImports)
	break
      }

      def slurper = DocumentReader.slurpDocument(dependentFile)
      historySlurpedFiles.put(dependentFile, slurper)
      this.dependentExternalNamespaces.add(slurper.xsdNamespace)
      unparsed.addAll(slurper.xsdImports)
      // this will allow a dependency to be added and gone thorugh again
      // but if in history of slurped files, breaks anyway, so I'll take this hit
      unparsed.remove(dependentFile)
    }
    
    return historySlurpedFiles
  }

  def checkForExternalCircularDependency(Set<String> operatingNamespaces,
					 DocumentSlurper slurpedDocument) {
    // TODO if circular dependency throw error!
    this.dependentNamespaces.add(slurpedDocument.xsdNamespace)
  }
  
  /**
   * @return List<File> a list of files to parse for this namespace
   * (include files are taken out of this list)
   */
  public List<File> filesToParse() {
    def slurperFiles = slurpedDocuments.documentFile
    def includes = [] as Set
    slurpedDocuments.xsdIncludes.findAll { !it.isEmpty() }.each { includes.addAll(it) }
    if (includes.isEmpty()) {
      log.info("namespace '{}' does not include any slurperFiles")
      return slurperFiles
    }
    log.info("namespace '{}' intends to parse '{}' and includes '{}'",
	     namespace, slurperFiles, includes)
    def filesToParse = slurperFiles.minus(includes)
    if(filesToParse.isEmpty()) { 
      def expectionString = "namespace '${namespace}' contains no files to" +
	"actually parse due to a circular dependency in the includes files." +
	"Check the files and their include statements.  '${namespace}' has " +
	"'${slurperFiles.size()}' slurped files -> '${slurperFiles}' and " +
	"includes '${includes.size()}' files -> '${includes}'."
      throw new RuntimeException(exceptionString)
    }
    return filesToParse
  }

  def String toString() { 
    def out = "${namespace}"
    // out += "files with this namespace:\n ${parseFiles ?: "none" }\n"
    // out += "namespace imports over all the files:\n ${importedNamespaces ?: "none" }\n"
    // out += "namespace includes over all the files:\n ${includeFiles ?: "none" }\n"
    return out
  }
}