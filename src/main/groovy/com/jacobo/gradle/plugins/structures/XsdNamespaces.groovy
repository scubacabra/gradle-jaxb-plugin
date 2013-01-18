package com.jacobo.gradle.plugins.structures

import com.jacobo.gradle.plugins.util.ListUtil

/**
 * Model that contains a particular set of unique data for a Namespace for jaxb generation
 * @author Daniel Mijares
 * @date 12/19/12
 */
class XsdNamespaces { 

  /**
   * string of this unique namespace
   */
  String namespace

  /**
   * the episode name that will be generated when parsing
   */
  String episodeName

  /**
   * A list of files that are passed in as an input to the xjc task
   */
  List<File> parseFiles = []

  /**
   * a list of strings that are the imported namespaces that all @parseFiles files have imported
   */
  List<String> importedNamespaces = []

  /**
   *a list of files that are included by all the @parseFiles
   */
  List<File> includeFiles = []

  /**
   * a map of String, File pairs that contain unique externally imported namespaces and the @schemaLocation file turned into an absolute File that they import.  only unique external imported namespace and unique absolute file paths are in this Map
   */
  Map<String, List<File>> externalImportedNamespaces = [:]

  /**
   * Method that converts standard @targetNamespace values to an appropriate episode File name that the file system accepts
   */
  public convertNamespaceToEpisodeName() { 
    def convert = namespace.replace("http://", "")
    convert = convert.replace(":", "-")
    convert = convert.replace("/", "-")
    episodeName = convert
  }

  /**
   * add an include file to the @includeFiles list
   */
  def addIncludeFile(File include) { 
    if( !ListUtil.isAlreadyInList(includeFiles, include) ) { 
      includeFiles << include
    }
  }

  /**
   * add an imported namespace to the @importedNamespaces list
   */
  def addImportedNamespace(String importNs) { 
    if( !ListUtil.isAlreadyInList(importedNamespaces, importNs) ) { 
      importedNamespaces << importNs
    }
  }

  /**
   * add a list of File to the parseFiles list
   */
  def addParseFiles(List<File> files) { 
    parseFiles.addAll(files)
  }

  /**
   * add a single File to the parseFiles list
   */
  def addParseFile(File file) { 
    if( !ListUtil.isAlreadyInList(parseFiles, file) ) { 
      parseFiles << file
    }
  }
  
  /**
   * add an externally imported namespace and associated externally imported File to the externalImportedNamespaces map
   */
  def addExternalImportedNamespaces(String externalNamespcae, File externalFile) { 
    if(externalImportedNamespaces.containsKey(externalNamespcae)) { 
      if( !ListUtil.isAlreadyInList(externalImportedNamespaces[externalNamespcae], externalFile) ) { 
	externalImportedNamespaces[externalNamespcae] << externalFile
      }
    } else { 
      externalImportedNamespaces[externalNamespcae] = [externalFile]
    }
  }

  /**
   * checks to see if this namespace is part of the unique namespaces being processed right now, if it isn't it is externally imported
   */
  def isImportedNamespaceExternal = { collection, ns ->
    if(!collection.find { it.namespace == ns } ) { //this namespace not in the collection of namespaces available, is external
      return true
    }
    return false
    //    return (collection.find{it.namespace == namespace}) ? false : true
  }

  def String toString() { 
    def out = "Namespace: ${namespace} \n"
    out += "files with this namespace:\n ${xsdFiles ?: "none" }\n"
    out += "namespace imports over all the files:\n ${fileImports ?: "none" }\n"
    out += "namespace includes over all the files:\n ${fileIncludes ?: "none" }\n"
    out += "namespace external dependencies over all the files:\n ${externalImportedNamespaces ?: "none" }\n"
    return out
  }
}