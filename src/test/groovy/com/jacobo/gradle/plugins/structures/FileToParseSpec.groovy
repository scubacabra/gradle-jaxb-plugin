package com.jacobo.gradle.plugins.structures

import com.jacobo.gradle.plugins.model.XsdSlurper

import spock.lang.Specification

import org.gradle.api.logging.Logging
import org.gradle.api.logging.Logger

class FileToParseSpec extends Specification {
  static final Logger log = Logging.getLogger(FileToParseSpec.class)

  def nmd = new NamespaceMetaData()
  
  def "get Files to parse, simple 2 files, one includes the other, should only get one file to parse" () { 
  setup: "setup the Slurper class"
  def result
  def slurped1 = new XsdSlurper()
  slurped1.document = doc
  slurped1.xsdNamespace = "http://www.example.org/Kitchen"
  slurped1.xsdIncludes = [includeFile]
  def slurped2 = new XsdSlurper()
  slurped2.document = includeFile
  slurped2.xsdNamespace = "http://www.example.org/Kitchen"
  nmd.namespace = slurped1.xsdNamespace
  nmd.slurpers = [slurped1, slurped2]

  when: "we process include files, the parse Files should be without the included file"
  result = nmd.filesToParse()

  then: "parse Files should only be of length 1"
  result.size == 1
  result[0] == doc
  
  where:
  doc = new File(this.getClass().getResource("/schema/testIncludes/Kitchen.xsd").toURI()).absoluteFile
  includeFile = new File(this.getClass().getResource("/schema/testIncludes/KitchenSubset.xsd").toURI()).absoluteFile
  }

  def "get Files to parse, 3 files, 1 includes 2 and 2 includes 3, should only get one file to parse" () { 
  setup: "setup the Slurper class"
  def result
  def slurped1 = new XsdSlurper()
  slurped1.document = doc
  slurped1.xsdNamespace = "http://www.example.org/Kitchen"
  slurped1.xsdIncludes = [includeFile]
  def slurped2 = new XsdSlurper()
  slurped2.document = includeFile
  slurped2.xsdNamespace = "http://www.example.org/Kitchen"
  slurped2.xsdIncludes = [secondInclude]
  def slurped3 = new XsdSlurper()
  slurped3.document = secondInclude
  slurped3.xsdNamespace = "http://www.example.org/Kitchen"
  nmd.namespace = slurped1.xsdNamespace
  nmd.slurpers = [slurped1, slurped2, slurped3]

  when: "we process include files, the parse Files should be without the included file"
  result = nmd.filesToParse()

  then: "parse Files should only be of length 1"
  result.size == 1
  result[0] == doc
  
  where:
  doc = new File(this.getClass().getResource("/schema/testIncludes/Kitchen.xsd").toURI()).absoluteFile
  includeFile = new File(this.getClass().getResource("/schema/testIncludes/KitchenSubset.xsd").toURI()).absoluteFile
  secondInclude = new File(this.getClass().getResource("/schema/testIncludes/KitchenSubset2.xsd").toURI()).absoluteFile
  }

  def "get Files to parse, 3 files, 1 includes 2 and 3, should only get one file to parse" () { 
  setup: "setup the Slurper class"
  def result
  def slurped1 = new XsdSlurper()
  slurped1.document = doc
  slurped1.xsdNamespace = "http://www.example.org/Kitchen"
  slurped1.xsdIncludes = [includeFile, secondInclude]
  def slurped2 = new XsdSlurper()
  slurped2.document = includeFile
  slurped2.xsdNamespace = "http://www.example.org/Kitchen"
  slurped2.xsdIncludes = [secondInclude]
  def slurped3 = new XsdSlurper()
  slurped3.document = secondInclude
  slurped3.xsdNamespace = "http://www.example.org/Kitchen"
  nmd.namespace = slurped1.xsdNamespace
  nmd.slurpers = [slurped1, slurped2, slurped3]

  when: "we process include files, the parse Files should be without the included file"
  result = nmd.filesToParse()

  then: "parse Files should only be of length 1"
  result.size == 1
  result[0] == doc
  
  where:
  doc = new File(this.getClass().getResource("/schema/testIncludes/Kitchen.xsd").toURI()).absoluteFile
  includeFile = new File(this.getClass().getResource("/schema/testIncludes/KitchenSubset.xsd").toURI()).absoluteFile
  secondInclude = new File(this.getClass().getResource("/schema/testIncludes/KitchenSubset2.xsd").toURI()).absoluteFile
  }

  def "slurped up includes, circular dependency (error in writing your schema) now, doc includes include1 and include1 includes doc, parseFiles is null : error writing schema should be fixed." () {
  setup:
  def result
  def slurped1 = new XsdSlurper()
  slurped1.document = doc
  slurped1.xsdNamespace = "http://www.example.org/Kitchen"
  slurped1.xsdIncludes = [includeFile]
  def slurped2 = new XsdSlurper()
  slurped2.document = includeFile
  slurped2.xsdNamespace = "http://www.example.org/Kitchen"
  slurped2.xsdIncludes = [doc]

  when:
  nmd.slurpers << slurped1
  nmd.slurpers << slurped2
  nmd.namespace = "http://www.example.org/Kitchen"
  result = nmd.filesToParse()

  then: "include files should be 2 and parse files should be 0" //TODO an error should really be thrown IMO
  result.size == 0
  result.find { it == includeFile } == null
  result.find { it == doc } == null
  
  where:
  doc = new File(this.getClass().getResource("/schema/testIncludes/Kitchen.xsd").toURI()).absoluteFile
  includeFile = new File(this.getClass().getResource("/schema/testIncludes/KitchenSubset.xsd").toURI()).absoluteFile
  }
}