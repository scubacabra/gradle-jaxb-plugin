package org.openrepose.gradle.plugins.jaxb.converter

import spock.lang.Specification
import spock.lang.Unroll

class NamespaceToEpisodeConverterSpec extends Specification {
  
  def converter = new NamespaceToEpisodeConverter()

  @Unroll
  def "convert '#namespace ' to episode name '#episodeName '" () {
    when:
    def result = converter.convert(namespace)

    then:
    result == episodeName

    where:
    namespace                       | episodeName
    "http://fake.com/donuts/glazed" | "fake.com-donuts-glazed.episode"
    "urn:real/boy/pinnochio"        | "urn-real-boy-pinnochio.episode"
    "tickle/me/elmo"                | "tickle-me-elmo.episode"
    "Alert.xsd"                     | "Alert.xsd.episode"  // when file name is namespace because no namespace present
  }
}