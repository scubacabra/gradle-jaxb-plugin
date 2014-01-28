package com.jacobo.gradle.plugins.reader

import groovy.util.slurpersupport.GPathResult
import com.jacobo.gradle.plugins.model.XsdSlurper
import spock.lang.Specification

class DocumentReaderSpec extends Specification {
  
  def "slurp Document get the right type of slurper to return" () {

  when:
  def result = DocumentReader.slurpDocument(file)

  then:
  result instanceof XsdSlurper
  result.documentFile == file
  result.slurpedDocument instanceof GPathResult
  
  where:
  file =  new File(this.getClass().getResource("/schema/House/Kitchen.xsd").toURI())
  }
}