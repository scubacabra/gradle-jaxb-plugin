package com.jacobo.gradle.plugins.util

import spock.lang.Specification

class FileUtilSpec extends Specification {
  
 def "Get all XSD Files in the directory passed as a String (absolute)"() { 
  when:
  def files = FileUtil.findAllXsdFiles(directory)
  
  then:
  files.size == 3
  files == xsdFiles*.absoluteFile
  
  where:
  directory = new File("build/resources/test/schema/House").absolutePath
  xsdFiles = [new File("build/resources/test/schema/House/Kitchen.xsd"), new File("build/resources/test/schema/House/KitchenSubset.xsd"), new File("build/resources/test/schema/House/LivingRoom.xsd")]
  }

 def "Get all XSD Files in the directory passed as a File (absolute)"() { 
  when:
  def files = FileUtil.findAllXsdFiles(directory)
  
  then:
  files.size == 3
  files == xsdFiles*.absoluteFile
  
  where:
  directory = new File("build/resources/test/schema/House").absoluteFile
  xsdFiles = [new File("build/resources/test/schema/House/Kitchen.xsd"), new File("build/resources/test/schema/House/KitchenSubset.xsd"), new File("build/resources/test/schema/House/LivingRoom.xsd")]
  }

 def "test absolute schema location"() { 
  expect:
  result == FileUtil.getAbsoluteSchemaLocation(schemaLocale, parent)

  where:
  parent             | schemaLocale        | result
  new File("schema") | "../blah/blah/blah" | new File("blah/blah/blah").absoluteFile
  new File("wsdl")   | "../blah/something" | new File("blah/something").absoluteFile
  new File("nothing")| "../blah/blah/blah" | new File("blah/blah/blah").absoluteFile
  }

  def "make sure grab absolute file is working properly" () {

  expect: "have a parent Directory, and a relative schema location to that parent directory, get correct absolute path"
    fileReturn.path == FileUtil.getAbsoluteSchemaLocation(schemaLocation, parentDir).path

  where: "inputs and outputs are according to:"
    schemaLocation               | parentDir                                                                   | fileReturn
    "../testImports/Kitchen.xsd" | new File(this.getClass().getResource("/schema/House").toURI()).absoluteFile | new File("build/resources/test/schema/testImports/Kitchen.xsd").absoluteFile
    "../testIncludes/KitchenSubset.xsd" | new File(this.getClass().getResource("/schema/House").toURI()).absoluteFile | new File("build/resources/test/schema/testIncludes/KitchenSubset.xsd").absoluteFile
  }
}
