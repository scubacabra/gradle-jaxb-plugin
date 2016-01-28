package org.gradle.jacobo.schema.slurper

import groovy.util.slurpersupport.GPathResult

import spock.lang.Specification
import spock.lang.Unroll

class DefaultXsdSlurperSpec extends Specification {

  def xsdSlurper = new DefaultXsdSlurper()
  def namespace = "some-namespace"

  def getFileFromResourcePath(path) {
    return new File(this.getClass().getResource(path).toURI())
  }

  @Unroll
  def "slurping namespace from '#file.name', namespace is '#namespace'"() {
    setup:
    def slurped = new XmlSlurper().parse(file)
    
    when:
    def result = xsdSlurper.slurpNamespace(slurped, file)

    then:
    result == namespace

    where:
    file << ["/schema/xsd-with-namespace.xsd", "/schema/xsd-no-namespace.xsd"].collect { getFileFromResourcePath(it) }
    namespace << ["some-namespace", "xsd-no-namespace.xsd"]
  }

  @Unroll
  def "resolved imports for '#imports' = '#resolved'"() { 
    given:
    def absoluteDependencies = [:]
    imports.each { 
      def file = new File(it)
      absoluteDependencies[it] = file
    }

    when:
    def results = xsdSlurper.findResolvedXsdImports(namespace, imports,
						   absoluteDependencies)

    then:
    results.size() == resolved.size()
    results == resolved

    where:
    imports = ["./sub/package.xsd", "../import/outside-import.xsd",
	       "../other/import.xsd"] as Set
    resolved = [new File("./sub/package.xsd"),
		new File("../import/outside-import.xsd"),
		new File("../other/import.xsd")] as Set
    
  }

  @Unroll
  def "resolved includes for '#includes' = '#resolved'"() { 
    given:
    def absoluteDependencies = [:]
    includes.each { 
      def file = new File(it)
      absoluteDependencies[it] = file
    }

    when:
    def results = xsdSlurper.findResolvedXsdIncludes(namespace,
						     "some-filename.xsd",
						     includes,
						     absoluteDependencies)

    then:
    results.size() == resolved.size()
    results == resolved

    where:
    includes = ["./include.xsd", "./sub/other-include.xsd"] as Set
    resolved = [new File("./include.xsd"),
		new File("./sub/other-include.xsd")] as Set
  }
}