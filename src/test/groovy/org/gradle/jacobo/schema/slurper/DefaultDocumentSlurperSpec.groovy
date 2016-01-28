package org.gradle.jacobo.schema.slurper

import groovy.util.slurpersupport.GPathResult

import spock.lang.Specification
import spock.lang.Unroll

class DefaultDocumentSlurperSpec extends Specification {

  def documentSlurper = new DefaultDocumentSlurper()

  def getFileFromResourcePath(path) {
    return new File(this.getClass().getResource(path).toURI())
  }

  @Unroll
  def "slurp '#file' for xsd imports '#imports'"() { 
    given:
    def xsd = new XmlSlurper().parse(file)
    
    when:
    def result = documentSlurper.slurpDependencies(xsd.import, file, "imports")

    then:
    imports.size() == result.size()
    imports == result

    where:
    file << ["/schema/xsd-all-dependencies.xsd", "/schema/xsd-no-includes.xsd",
	    "/schema/xsd-no-dependencies.xsd"].collect { 
      getFileFromResourcePath(it)
    }
    imports << [["../schema/imported.xsd", "../schema/other-imported.xsd"],
		["../schema/imported.xsd", "../schema/other-imported.xsd"],
		[]].collect { it as Set }
  }

  @Unroll
  def "slurp '#file' for xsd includes '#includes'"() { 
    given:
    def xsd = new XmlSlurper().parse(file)
    
    when:
    def result = documentSlurper.slurpDependencies(xsd.include, file, "includes")

    then:
    includes.size() == result.size()
    includes == result

    where:
    file << ["/schema/xsd-all-dependencies.xsd", "/schema/xsd-no-dependencies.xsd"].collect {
      getFileFromResourcePath(it)
    }
    includes << [["../schema/included.xsd", "../schema/other-included.xsd"],
		 []].collect { it as Set }
  }

  @Unroll
  def "slurp '#file' for wsdl imports '#imports'"() { 
    given:
    def wsdl = new XmlSlurper().parse(file)
    
    when:
    def result = documentSlurper.slurpDependencies(wsdl.import, file,
						   "wsdl imports")

    then:
    imports.size() == result.size()
    imports == result

    where:
    file << ["/wsdl/wsdl-no-xsd.wsdl", "/wsdl/wsdl-all-dependencies.wsdl",
	    "/wsdl/wsdl-no-dependencies.wsdl"].collect {
      getFileFromResourcePath(it)
    }
    imports << [["./abstract.wsdl"], ["./abstract.wsdl"], []].collect { it as Set }
  }

  @Unroll
  def "slurp '#file' for wsdls xsd imports '#xsdImports'"() { 
    given:
    def wsdl = new XmlSlurper().parse(file)
    
    when:
    def result = documentSlurper.slurpDependencies(wsdl.types?.schema?.import,
						   file, "xsd imports")

    then:
    xsdImports.size() == result.size()
    xsdImports == result

    where:
    file << ["/wsdl/wsdl-no-xsd.wsdl", "/wsdl/wsdl-no-xsd-includes.wsdl",
	    "/wsdl/wsdl-all-dependencies.wsdl", "/wsdl/wsdl-no-dependencies.wsdl"].collect {
      getFileFromResourcePath(it)
    }
    xsdImports << [[], ["../schema/some-schema.xsd", "../schema/other-schema.xsd"],
		   ["../schema/some-schema.xsd", "../schema/other-schema.xsd"],
		   []].collect { it as Set }
  }

  @Unroll
  def "slurp '#file' for wsdls xsd includes '#xsdIncludes'"() { 
    given:
    def wsdl = new XmlSlurper().parse(file)
    
    when:
    def result = documentSlurper.slurpDependencies(wsdl.types?.schema?.include,
						   file, "xsd includes")

    then:
    xsdIncludes.size() == result.size()
    xsdIncludes == result

    where:
    file << ["/wsdl/wsdl-no-xsd.wsdl","/wsdl/wsdl-all-dependencies.wsdl",
	     "/wsdl/wsdl-no-dependencies.wsdl"].collect {
      getFileFromResourcePath(it)
    }
    xsdIncludes << [[],
		    ["../schema/some-include.xsd",
		     "../schema/some-other-include.xsd"],
		    []].collect { it as Set }
  }
}