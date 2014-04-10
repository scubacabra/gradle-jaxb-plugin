package org.gradle.jacobo.plugins

import groovy.util.slurpersupport.GPathResult

import org.gradle.jacobo.schema.XsdDocument
import org.gradle.jacobo.schema.factory.DocumentFactory
import org.gradle.jacobo.schema.resolver.DocumentResolver
import org.gradle.jacobo.schema.slurper.DocumentSlurper
import org.gradle.jacobo.schema.slurper.XsdSlurper

class DocumentSpec extends BaseSpecification {

  def xsdSlurper = Mock(XsdSlurper)
  def documentSlurper = Mock(DocumentSlurper)
  def documentResolver = Mock(DocumentResolver)
  def mockSlurper = new XmlSlurper().parseText("<xsd></xsd>")

  def createXsdDocument(File xsdFile) {
    def doc = new XsdDocument(documentSlurper, documentResolver,
			      xsdSlurper, xsdFile, mockSlurper)
    doc.documentDependencies = [:]
    return doc
  }

  def createXsdDocument(File xsdFile, String namespace) {
    def doc = createXsdDocument(xsdFile)
    doc.xsdNamespace = namespace
    return doc
  }
  
  def createXsdDocument(File xsdFile, GPathResult slurper) {
    def doc = new XsdDocument(documentSlurper, documentResolver,
			      xsdSlurper, xsdFile, slurper)
    doc.documentDependencies = [:]
    return doc
  }

  def createXsdDocument(File xsdFile, GPathResult slurper, String namespace) {
    def doc = createXsdDocument(xsdFile, slurper)
    doc.xsdNamespace = namespace
    return doc
  }
}