package com.jacobo.gradle.plugins.structures

import spock.lang.Specification

import org.gradle.api.logging.Logging
import org.gradle.api.logging.Logger

class NamespaceMetaDataSpec extends Specification {
  static final Logger log = Logging.getLogger(NamespaceMetaDataSpec.class)
  def nmd = new NamespaceMetaData()
  
  def "test targetNamespace to episode file name" () {
  when:
  nmd.namespace = ns
  nmd.convertNamespaceToEpisodeName()
  
  then:
  nmd.episodeName == episode

  where:
  ns                              | episode
  "http://fake.com/donuts/glazed" | "fake.com-donuts-glazed"
  "urn:real/boy/pinnochio"        | "urn-real-boy-pinnochio"
  "tickle/me/elmo"                | "tickle-me-elmo"
  }
}