package com.jacobo.gradle.plugins.structures

import org.gradle.api.logging.Logging
import org.gradle.api.logging.Logger

class ExternalNamespaceResolver { 
  private static final Logger log = Logging.getLogger(ExternalNamespaceResolver.class)

  def rootLocation
  def diveDeeper = []
  def importedNamespaces = []
  def targetDir

  def notAlreadyInImportList(location) { 
    def result = diveDeeper.findAll { it.name == new File(location)}
    return result ? false : true
  }

  def addToImportedNamespaces(String namespace) { 
    if(!isAlreadyInList(namespace)) { 
      log.debug("adding {} to imported namespace List {}", namespace, importedNamespaces )
      importedNamespaces << namespace
    }
  }

  boolean isAlreadyInList(String namespace) { 
    return importedNamespaces.contains(namespace)
  }

  def addToDiveDeeper(File file) { 
    if(!diveDeeper.contains(file)) { 
      log.debug("added {} to dive Deeper List", file)
      diveDeeper << file
    }
  }

  def locationClosure = { it ->
    def location = it.@schemaLocation.text()
    if (notAlreadyInImportList(location)) { 
      log.debug(" schema location is {}, and the targetDirectory (Parent Directory) is {}", location, targetDir)
      addToDiveDeeper(getAbsoluteSchemaLocation(location, targetDir))
    } 
  }

  def getImportedNamespaces = { it ->
    def namespace = it.@namespace.text()
    addToImportedNamespaces(namespace)
    locationClosure(it)
  }

  def getDependencies (xmlDoc) { 
    log.debug("resolving imports")
    xmlDoc?.import?.each getImportedNamespaces
    log.debug("resolving includes of")
    xmlDoc?.include?.each locationClosure
  }

  def resolveExternalDependencies() {
    targetDir = rootLocation.parentFile
    log.info("resolving external dependencies starting at {}", rootLocation)
    def xmlDoc = new XmlSlurper().parse(rootLocation)
    getDependencies(xmlDoc)
    while(diveDeeper) { 
      def document = diveDeeper.pop()
      log.debug("popping {} from diveDeeper list", document)
      xmlDoc = new XmlSlurper().parse(document)
      targetDir = document.parentFile
      getDependencies(xmlDoc)
    }
  }

  def getAbsoluteSchemaLocation(String schemaLocation, File parentDir) { 
    def relPath = new File(parentDir, schemaLocation)
    return new File(relPath.canonicalPath)
  }
}