package com.jacobo.gradle.plugins.structures

import org.gradle.api.logging.Logging
import org.gradle.api.logging.Logger
import com.jacobo.gradle.plugins.util.ListUtil
import com.jacobo.gradle.plugins.util.FileHelper

/**
 * This class resolves all external namespaces, traversing all schemaLocations and gethering all the imported namespaces
 *
 * @author Daniel Mijares
 * @version 1.0
 */
class ExternalNamespaceResolver { 
  private static final Logger log = Logging.getLogger(ExternalNamespaceResolver.class)

  /**
   * external namespace Data to resolve
   */
  private ExternalNamespaceMetaData externalImport

  /**
   * List of externally imported Namespace data that is returned from method resolve
   */
  List<ExternalNamespaceMetaData> externalImportedNamespaces = []


  /**
   * a list of schema locations left to parse
   */
  List<File> schemaLocationsToParse = []

  /**
   * This is the parent Directory of whatever schema is being slurped
   * I personally don't think this should be part of this class, but eh.
   */
  def parentDirectory


  /**
   * construct this object with the External Namespace Meta Data emd
   */
  public ExternalNamespaceResolver(ExternalNamespaceMetaData emd) { 
    externalImport = emd
  }

  /**
   * @param xmlSlurpedNode is the xml slurper object used to gather schema Locations from
   * Can be import or include slurper objects
   * slurps up the schema location and get's its absolute file, create an #ExternalNamespaceMetaData object with this file and returns it
   * @return #ExternalNamespaceMetaData could return null if this schema location to parse is already on the list
   */
  ExternalNamespaceMetaData gatherSchemaLocations (xmlSlurpedNode) {
    def location = xmlSlurpedNode.@schemaLocation.text()
    log.debug("location of this import is {}", location)
    def absoluteFile = FileHelper.getAbsoluteSchemaLocation(location, parentDirectory) 
    log.debug("absolute File of location {} is {}, schema Location to Parse is {}", location, absoluteFile, schemaLocationsToParse)
    if (!ListUtil.isAlreadyInList( schemaLocationsToParse, absoluteFile)) { 
      log.debug(" schema location is {}, and the parentDirectoryectory (Parent Directory) is {}", absoluteFile, parentDirectory)
      schemaLocationsToParse << absoluteFile
      return new ExternalNamespaceMetaData(externalSchemaLocation: absoluteFile)
    } 
    log.info("didn't find anything returning null")
    return null
  }

  /**
   * @param it is the import Object taken from the #XmlSlurper
   * Adds the imported namespace to the
   * @see #importedNamespaces List
   */
  def getImportedNamespaces = { it ->
    def namespace = it.@namespace.text()
    def externalMetaData = gatherSchemaLocations(it)
    if(externalMetaData) { 
      externalMetaData.namespace = namespace
      addExternalImportedNamespace(externalMetaData)    
    }
  }

  /**
   * @param xmlDoc the xml slurped document to gether data from
   * gathers schema Locations and importedNamespaces
   */
  def gatherSchemaData (xmlDoc) { 
    log.debug("resolving imports")
    xmlDoc?.import?.each getImportedNamespaces    
  }

  /**
   * resolves the external imported namespaces starting at
   * @see #externalImportStartLocation
   * While there are files in
   * @see #schemaLocationsToParse
   * keep slurping documents and gather schema locations
   * @return a list of #ExternalNamespaceMetaData to add to the external namespace that is being resolved
   */
  List<ExternalNamespaceMetaData> resolveExternalImportedNamespaces() {
    parentDirectory = externalImport.externalSchemaLocation.parentFile
    log.info("resolving external dependencies starting at {}", externalImport.externalSchemaLocation)
    def xmlDoc = new XmlSlurper().parse(externalImport.externalSchemaLocation)
    gatherSchemaData(xmlDoc)
    while(schemaLocationsToParse) { 
      def schemaLocale = schemaLocationsToParse.pop()
      def document = externalImportedNamespaces.find { it.externalSchemaLocation.absolutePath == schemaLocale.absolutePath }.externalSchemaLocation
      log.debug("popping {} from schemaLocationsToParse list", document)
      xmlDoc = new XmlSlurper().parse(document)
      parentDirectory = document.parentFile
      gatherSchemaData(xmlDoc)
    }
    log.debug("returning the imported namespaces {}", externalImportedNamespaces)
    return externalImportedNamespaces
  }
  
  /**
   * @param namespace   namespace string that is externally imported to add to externalImportedNamespaces List
   */
  def addExternalImportedNamespace(ExternalNamespaceMetaData namespace) { 
    if(!ListUtil.isAlreadyInList(externalImportedNamespaces, namespace)) { 
      log.debug("adding {} to imported namespace List {}", namespace, externalImportedNamespaces )
      externalImportedNamespaces << namespace
    }
  }

}