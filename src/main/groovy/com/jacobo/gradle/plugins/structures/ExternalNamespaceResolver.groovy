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
    def result = diveDeeper.findAll { new File(it).name == new File(location).name}
    return result ? false : true
  }

  def addToImportedNamespaces(String namespace) { 
    if(!isAlreadyInList(namespace)) { 
      log.debug("adding {} to imported namespace List {}", namespace, importedNamespace )
      importedNamespace << namespace
    }
  }

  boolean isAlreadyInList(String namespace) { 
    return importedNamespaces.contains(namespace)
  }

  def locationClosure = { it ->
    def location = it.@schemaLocation.text()
    if (notAlreadyInImportList(location)) { 
      diveDeeper << parsePaths(location, targetDir)
      log.debug("added a path to the Dive Deeper list {}", diveDeeper)
    } 
  }

  def getImportedNamespaces { it ->
    def namespace = it.@namespace.text()
    addToImportedNamespaces(namespace)
    locationClosure(it)
  }

  def getDependencies (xmlDoc) { 
    xmlDoc?.import?.each getImportedNamespaces
    xmlDoc?.include?.each locationClosure
  }

  def startDependencyResolution() {
    def targetDir = rootLocation.parent
    log.info("resolving external dependencies starting at {}", rootLocation)
    def xmlDoc = new XmlSlurper().parse(rootLocation)
    getDependencies(xmlDoc)
    while(diveDeeper) { 
      def docLocation = diveDeeper.pop()
      log.debug("popping {} from diveDeeper list", docLocation)
      def document = new File(docLocation)
      xmlDoc = new XmlSlurper().parse(document)
      targetDir = document.parent
      getDependencies(xmlDoc)
    }
  }

  def parsePaths(String relPath, File callerParentDir) { 
    if (relPath ==~ /[.][\/][A-Za-z]*.*/ ) { 
      return callerParentDir.path + "/" + relPath.tokenize("/")[1]
    }

    def grabDirsUpPortion = /([.\/]*)(.*)/
    def dirsUp = (relPath =~ grabDirsUpPortion)
    def dirsUpList = dirsUp[0][1].tokenize("/")
    def restOfPath = dirsUp[0][2]
    dirsUpList.each { 
      callerParentDir = new File(callerParentDir.parent)
    }
    return callerParentDir.path + "/" + restOfPath
  }
}