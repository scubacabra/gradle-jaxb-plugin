package com.jacobo.gradle.plugins.structures

import groovy.io.FileType

class FindNamespaces { 
  def baseDir
  def nsMap = [:]
  def schemaList = []

  
  def startFinding() { 
    baseDir = new File(baseDir)
    baseDir.eachFileRecurse(FileType.FILES) {  file -> 
      if(file.name.split("\\.")[-1] == 'xsd') {
	schemaList << file
      }
    }
    schemaList.each gatherXsdTargetNamespaces
  }

  def gatherXsdTargetNamespaces = { schemaDoc ->
    def records = new XmlSlurper().parse(schemaDoc)
    def target = records.@targetNamespace
    target = (!target.isEmpty()) ? target.text() : "null"
    if(nsMap.containsKey(target)) {
      nsMap[target] << schemaDoc
    }
    else { 
      nsMap[target] = [schemaDoc]
    }
  }
}