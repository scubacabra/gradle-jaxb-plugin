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
   * does this namespace depend on another? (internal OR external)
   */
  boolean dependsOnAnotherNamespace = false

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
   * add an externally imported xsd File (external File) to the externalImportedNamespaces map
   * no duplicate Files, only need one.  If new create the object, if already there, do nothing
   */
  def addExternalImportedNamespaces(File externalFile) { 
    def extNamespace = externalImportedNamespaces.find{ it.externalSchemaLocation == externalFile }
    log.debug("{} external File {} in external Imported Namespaces List {}", (extNamespace ? "Found, don't duplicate" : "No"), externalFile, externalImportedNamespaces)
    if (!extNamespace) { //first external in the externalImportedNamespaces list
      def extNs = new ExternalNamespaceMetaData()
      extNs.externalSchemaLocation = externalFile
      externalImportedNamespaces << extNs
    }
  }

  /**
   * parses the imports information for a schema file
   *
   * @param imports the xml slurper include data for this file
   * @param namespaceData the List of  #NamespaceMetaData object for the whole OrderGraph object
   * @param doc the File object representing the xsd file
   */
  def obtainImportedNamespaces(List currentNamespaces) {
    def totalImports = []
    log.debug("gathering all the imported Files for this namespace : {}", namespace)
    slurpers.xsdImports.findAll { !it.isEmpty() }.each {
      totalImports.addAll(it)
      totalImports?.unique()
    }
    totalImports.each { importedFile ->
      log.debug("checking to see if file : {} is found in any of the namespace slurped documents : {} ", importedFile, currentNamespaces.slurpers.document)
      def matchingImport = currentNamespaces.find { it.slurpers.document.contains(importedFile) }
      if(matchingImport) {
        ListUtil.addElementToList(importedNamespaces, matchingImport)
      } else { //is external to this namespace group, treat differently
        addExternalImportedNamespaces(importedFile)
      }
    }
    if(importedNamespaces) 
      dependsOnAnotherNamespace = true // if a namespace imports something from either internal or external dependency , flag it so it is not grabbed to be the first namespace parsed
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