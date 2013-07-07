package com.jacobo.gradle.plugins.structures

import com.jacobo.gradle.plugins.util.ListUtil

import com.jacobo.gradle.plugins.structures.NamespaceMetaData
import com.jacobo.gradle.plugins.structures.ExternalNamespaceMetaData

import com.jacobo.gradle.plugins.model.XsdSlurper

import org.gradle.api.logging.Logging
import org.gradle.api.logging.Logger

/**
 * Model that contains a particular set of unique data for a Namespace for jaxb generation
 * @author Daniel Mijares
 * @date 12/19/12
 */
class NamespaceMetaData { 

  static final Logger log = Logging.getLogger(NamespaceMetaData.class)

  /**
   * the namespace for this group of file(s)
   */
  String namespace

  /**
   * the episode name that will be generated when parsing
   */
  String episodeName

  /**
   * A list of files that are passed in as an input to the xjc task
   */
  List<XsdSlurper> slurpers = []

  /**
   * a list of strings that are the imported namespaces that all @parseFiles files have imported
   */
  List<NamespaceMetaData> importedNamespaces = []

  /**
   * a map of String, File pairs that contain unique externally imported namespaces and the @schemaLocation file turned into an absolute File that they import.  only unique external imported namespace and unique absolute file paths are in this Map
   */
  List<ExternalNamespaceMetaData> externalImportedNamespaces = []

  /**
   * Method that converts the namespace into an appropriate episode File name that the file system accepts
   * TODO: conventions could be better
   */
  public convertNamespaceToEpisodeName() { 
    def convert = namespace.replace("http://", "")
    convert = convert.replace(":", "-")
    convert = convert.replace("/", "-")
    this.episodeName = convert
  }


  /**
   * add an imported namespace to the @importedNamespaces list
   */
  def addImportedNamespace(NamespaceMetaData importedNamespaceData) { 
    if( !ListUtil.isAlreadyInList(importedNamespaces, importedNamespaceData) ) { 
      importedNamespaces << importedNamespaceData
    }
  }
  
  /**
   * add an externally imported namespace and associated externally imported File to the externalImportedNamespaces map
   */
  def addExternalImportedNamespaces(String externalNamespace, File externalFile) { 
    def extNamespace = externalImportedNamespaces.find{ it.namespace == externalNamespace }
    if (extNamespace) { //already in this external imported namespaces list
      if (!ListUtil.isAlreadyInList(extNamespace.externalSchemaLocation, externalFile )) { //TODO I think every external namespace should only have one entry point, but I guess I could be wrong, planning for multiple as a worst case
	extNamespace.externalSchemaLocation << externalFile
      }
    } else { //first external in the externalImportedNamespaces list, create object, populate fields, add to external Import Namespace List
      def extNs = new ExternalNamespaceMetaData()
      extNs.namespace = externalNamespace
      extNs.externalSchemaLocation = externalFile
      externalImportedNamespaces << extNs
    }
  }

  /**
   * slurps the xsd files for import and include data
   */
  def slurpXsdFiles(List namespacesData) {
    if(!importedNamespaces) importedNamespaces << "none" // if a namespace imports nothing, flag it for being parse first
  }

  /**
   * parses the imports information for a schema file
   *
   * @param imports the xml slurper include data for this file
   * @param namespaceData the List of  #NamespaceMetaData object for the whole OrderGraph object
   * @param doc the File object representing the xsd file
   */
  def slurpImports = { imports, List namespaceData, doc ->
    log.debug("Starting to slurp the import statements for {}", doc)
    if(!imports.isEmpty()) { 
      imports.each { imp ->
	def namespace = imp.@namespace.text()
	if(!ListUtil.isImportedNamespaceExternal(namespaceData, namespace)) {
	  def nsData = namespaceData.find { it.namespace == namespace }
	  addImportedNamespace(nsData)
	} else { 
	  def externalSchemaLocale = imp.@schemaLocation.text()
	  def externalSchemaPath = new File(doc.parent, externalSchemaLocale).canonicalPath
	  def externalSchemaFile = new File(externalSchemaPath)
	  addExternalImportedNamespaces(namespace, externalSchemaFile)
	}
      }
    }
  }

  /**
   * @return List<File> a list of files to parse for this namespace (include files are taken out of this list)
   *
   */
  public List<File> filesToParse() { 
    def slurperFiles = slurpers.document
    def includes = []
    slurpers.xsdIncludes.findAll { !it.isEmpty() }.each { includes.addAll(it) }
    if (includes.isEmpty()) {
      log.info("this namespace {} does not include any slurperFiles")
      return slurperFiles
    }
    log.info("This namespace {} intends to parse {} but includes {}", namespace, slurperFiles, includes)
    def filesToParse = slurperFiles.minus(includes)
    if(filesToParse.isEmpty()) { 
      log.warn("namespace {} has empty an empty parse files list, most likely there is a circular dependency for includes files", namespace)
    }
    return filesToParse
  }

  def String toString() { 
    def out = "${namespace}"
    // out += "files with this namespace:\n ${parseFiles ?: "none" }\n"
    // out += "namespace imports over all the files:\n ${importedNamespaces ?: "none" }\n"
    // out += "namespace includes over all the files:\n ${includeFiles ?: "none" }\n"
    // out += "namespace external dependencies over all the files:\n ${externalImportedNamespaces ?: "none" }\n"
    return out
  }
}