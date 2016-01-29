package org.openrepose.gradle.plugins.jaxb.fixtures

import groovy.util.slurpersupport.GPathResult

import org.openrepose.gradle.plugins.jaxb.BaseSpecification
import org.openrepose.gradle.plugins.jaxb.schema.XsdDocument
import org.openrepose.gradle.plugins.jaxb.schema.resolver.DocumentResolver
import org.openrepose.gradle.plugins.jaxb.schema.slurper.DocumentSlurper
import org.openrepose.gradle.plugins.jaxb.schema.slurper.XsdSlurper

class DocumentFixture extends BaseSpecification {

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