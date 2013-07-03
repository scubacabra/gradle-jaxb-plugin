package com.jacobo.gradle.plugins.reader

import com.jacobo.gradle.plugins.model.XsdSlurper
import spock.lang.Specification

class DocumentReaderSpec extends Specification {
  
  def "slurp Document get the right type of slurper to return" () {

  when:
  def result = DocumentReader.slurpDocument(new File(url.toURI()))

  then:
  result.class in [clazz]
  result.currentDir == dir.absoluteFile
  result.documentName == name
  
  where:
  url | clazz | dir | name
  this.getClass().getResource("/schema/House/Kitchen.xsd") | XsdSlurper | new File("build/resources/test/schema/House") | "Kitchen.xsd"
  }
}