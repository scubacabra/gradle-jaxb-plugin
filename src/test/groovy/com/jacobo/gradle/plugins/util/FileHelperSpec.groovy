package com.jacobo.gradle.plugins.util

import spock.lang.Specification

class FileHelperSpec extends Specification {
 def "test absolute schema location"() { 
  expect:
  result == FileHelper.getAbsoluteSchemaLocation(schemaLocale, parent)

  where:
  parent             | schemaLocale        | result
  new File("schema") | "../blah/blah/blah" | new File("blah/blah/blah").absoluteFile
  new File("wsdl")   | "../blah/something" | new File("blah/something").absoluteFile
  new File("nothing")| "../blah/blah/blah" | new File("blah/blah/blah").absoluteFile
  }

  def "make sure grab absolute file is working properly" () {

  expect: "have a parent Directory, and a relative schema location to that parent directory, get correct absolute path"
    fileReturn.path == FileHelper.getAbsoluteSchemaLocation(schemaLocation, parentDir).path

  where: "inputs and outputs are according to:"
    schemaLocation               | parentDir                                                                   | fileReturn
    "../testImports/Kitchen.xsd" | new File(this.getClass().getResource("/schema/House").toURI()).absoluteFile | new File("build/resources/test/schema/testImports/Kitchen.xsd").absoluteFile
    "../testIncludes/KitchenSubset.xsd" | new File(this.getClass().getResource("/schema/House").toURI()).absoluteFile | new File("build/resources/test/schema/testIncludes/KitchenSubset.xsd").absoluteFile
  }
}
