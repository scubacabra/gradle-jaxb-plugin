package com.jacobo.gradle.plugins.structures

import org.gradle.api.logging.Logging
import org.gradle.api.logging.Logger
import com.jacobo.gradle.plugins.util.ListUtil

/**
 * This class resolves all external namespaces, traversing all schemaLocations and gethering all the imported namespaces
 *
 * @author Daniel Mijares
 * @version 1.0
 */
class ExternalNamespaceResolver { 
  private static final Logger log = Logging.getLogger(ExternalNamespaceResolver.class)

  /**
   * Start File location (absolute Path) to parse external schemas and gether imported namespaces from this starting schema file
   */
  File externalImportStartLocation

  /**
   * List of any schema locations left to parse and go through, when this is empty, the processing can return
   */
  def schemaLocationsToParse = []
  
  /**
   * List of externally imported Namespaces that is eventually returned to the called
   */
  def externalImportedNamespaces = []
  
  /**
   * This is the parent Directory of whatever schema is being slurped
   * I personally don't think this should be part of this class, but eh.
   */
  def parentDirectory

  /**
   * @param it is the xml slurper object used to gather schema Locations from
   * Can be import or include slurper objects
   */
  def gatherSchemaLocations = { it ->
    def location = it.@schemaLocation.text()
    def absoluteFile = getAbsoluteSchemaLocation(location, parentDirectory) 
    if (ListUtil.isAlreadyInList( schemaLocationsToParse, absoluteFile)) { 
      log.debug(" schema location is {}, and the parentDirectoryectory (Parent Directory) is {}", absoluteFile, parentDirectory)
      addSchemaLocationToParse(absoluteFile)
    } 
  }

  /**
   * @param it is the import Object taken from the #XmlSlurper
   * Adds the imported namespace to the
   * @see #importedNamespaces List
   */
  def getImportedNamespaces = { it ->
    def namespace = it.@namespace.text()
    addToImportedNamespaces(namespace)
    locationClosure(it)
  }

  /**
   * @param xmlDoc the xml slurped document to gether data from
   * gathers schema Locations and importedNamespaces
   */
  def gatherSchemaData (xmlDoc) { 
    log.debug("resolving imports")
    xmlDoc?.import?.each getImportedNamespaces
    log.debug("resolving includes of")
    xmlDoc?.include?.each locationClosure
  }

  /**
   * resolves the external imported namespaces starting at
   * @see #externalImportStartLocation
   * While there are files in
   * @see #schemaLocationsToParse
   * keep slurping documents and gather schema locations
   */
  def resolveExternalImportedNamespaces() {
    parentDirectory = externalImportStartLocation.parentFile
    log.info("resolving external dependencies starting at {}", externalImportStartLocation)
    def xmlDoc = new XmlSlurper().parse(externalImportStartLocation)
    gatherSchemaData(xmlDoc)
    while(schemaLocationsToParse) { 
      def document = schemaLocationsToParse.pop()
      log.debug("popping {} from schemaLocationsToParse list", document)
      xmlDoc = new XmlSlurper().parse(document)
      parentDirectory = document.parentFile
      gatherSchemaData(xmlDoc)
    }
  }
  
  /**
   * @param schemaLocation is the relative path of the schema location being called in eith the xsd:import or xsd:includes call
   * @param parentDir is the parent directory of the schema file that is currently being Xml Slurped
   * @return File absolute File path to schema Location
   */
  File getAbsoluteSchemaLocation(String schemaLocation, File parentDir) { 
    def relPath = new File(parentDir, schemaLocation)
    return new File(relPath.canonicalPath)
  }

  /**
   * @param namespace   namespace string that is externally imported to add to externalImportedNamespaces List
   */
  def addExternalImportedNamespace(String namespace) { 
    if(!ListUtil.isAlreadyInList(externalImportedNamespaces, namespace)) { 
      log.debug("adding {} to imported namespace List {}", namespace, externalImportedNamespaces )
      externalImportedNamespaces << namespace
    }
  }

  /**
   * @param file is the absolute file path to add to the @scheaLocationsToParse list
   */
  def addSchemaLocationToParse(File file) { 
    if(!ListUtil.isAlreadyInList(schemaLocationsToParse, file)) { 
      log.debug("added {} to dive Deeper List", file)
      schemaLocationsToParse << file
    }
  }
}