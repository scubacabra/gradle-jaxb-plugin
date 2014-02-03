package com.jacobo.gradle.plugins.structures

import com.jacobo.gradle.plugins.model.XsdSlurper

import spock.lang.Specification

import org.gradle.api.logging.Logging
import org.gradle.api.logging.Logger

class FileToParseSpec extends Specification {
  static final Logger log = Logging.getLogger(FileToParseSpec.class)

  def nd = new NamespaceData()
  def namespace = "same-namespace"
  def s1 = new XsdSlurper()
  def s2 = new XsdSlurper()
  def s3 = new XsdSlurper()
  def mainXsd = new File("main-document.xsd")

  def setup() {
    nd.namespace = namespace
    s1.with {
      documentFile = mainXsd
      xsdNamespace = namespace
    }
  }

  def "get files to parse, main doc includes another doc, should only get 1, main doc to parse"() {
    given:
    s1.with {
      xsdIncludes = [relPath] as Set
      documentDependencies.put(relPath,includeFile)
    }
    s2.with {
      xsdNamespace = namespace
      documentFile = includeFile
    }
    nd.slurpedDocuments = [s1, s2]

    when:
    def result = nd.filesToParse()

    then:
    result.size == 1
    result[0] == mainXsd

    where:
    relPath = "s2.xsd"
    includeFile = new File("s2.xsd")

  }
  

  def "get files to parse, main document includes a sub, that includes a sub -- only the main document should be parsed"() {
    given:
    s1.with {
      xsdIncludes = [relPath] as Set
      documentDependencies.put(relPath,includeFile)
    }
    s2.with {
      xsdNamespace = namespace
      documentFile = includeFile
      documentDependencies.put(subRelPath, subIncludeFile)
      xsdIncludes = [subRelPath] as Set
    }
    s3.with {
      xsdNamespace = namespace
      documentFile = subIncludeFile
    }
    nd.slurpedDocuments = [s1, s2, s3]

    when:
    def result = nd.filesToParse()

    then:
    result.size == 1
    result[0] == mainXsd

    where:
    relPath = "s2.xsd"
    includeFile = new File("s2.xsd")
    subRelPath = "s3.xsd"
    subIncludeFile = new File("s3.xsd")
  }

  def "get files to parse, main document includes a sub AND another sub, sub  includes the last sub -- only the main document should be parsed"() {
    given:
    s1.with {
      xsdIncludes = [relPath, subRelPath] as Set
      documentDependencies.put(relPath,includeFile)
      documentDependencies.put(subRelPath, subIncludeFile)
    }
    s2.with {
      xsdNamespace = namespace
      documentFile = includeFile
      documentDependencies.put(subRelPath, subIncludeFile)
      xsdIncludes = [subRelPath] as Set
    }
    s3.with {
      xsdNamespace = namespace
      documentFile = subIncludeFile
    }
    nd.slurpedDocuments = [s1, s2, s3]

    when:
    def result = nd.filesToParse()

    then:
    result.size == 1
    result[0] == mainXsd

    where:
    relPath = "s2.xsd"
    includeFile = new File("s2.xsd")
    subRelPath = "s3.xsd"
    subIncludeFile = new File("s3.xsd")
  }

  def "get files to parse, main doc includes another doc, but there is a circular dependency.  the sub also includes the main."() {
    given:
    s1.with {
      xsdIncludes = [relPath] as Set
      documentDependencies.put(relPath,includeFile)
    }
    s2.with {
      xsdNamespace = namespace
      documentFile = includeFile
      xsdIncludes = [mainXsd.name] as Set
      documentDependencies.put(mainXsd.name, mainXsd)
    }
    nd.slurpedDocuments = [s1, s2]

    when:
    def result = nd.filesToParse()

    then:
    thrown RuntimeException

    where:
    relPath = "s2.xsd"
    includeFile = new File("s2.xsd")
  }
}