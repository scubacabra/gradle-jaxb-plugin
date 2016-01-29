package org.openrepose.gradle.plugins.jaxb

import spock.lang.Specification

/**
 * utility methods that most every test uses.
 */
class BaseSpecification extends Specification {

  def getFileFromResourcePath(path) {
    return new File(this.getClass().getResource(path).toURI())
  }

}