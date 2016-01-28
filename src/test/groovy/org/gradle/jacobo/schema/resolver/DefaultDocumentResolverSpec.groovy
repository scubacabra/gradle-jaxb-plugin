package org.gradle.jacobo.schema.resolver

import spock.lang.Specification
import spock.lang.Unroll

class DefaultDocumentResolverSpec extends Specification {
  
  def absoluteFileResolver = Mock(AbsoluteFileResolver)
  def docResolver = new DefaultDocumentResolver(absoluteFileResolver)

  def "resolve '#relativeDependencies.size' relative dependencies relative to directory '#docDir'"() {
    given:
    def size = relativeDependencies.size

    when:
    def result = docResolver.resolveRelativePaths(relativeDependencies as Set,
						  docDir)

    then:
    size * absoluteFileResolver.resolveToAbsolutePath(_, docDir) >> new File("/")
    result.size() == size
    result.each { k,v ->
      k instanceof String
      v instanceof File
    }

    where:
    docDir = new File("/parentDirectory")
    relativeDependencies << [["./relative1.xsd", "../dir/relative2.xsd",
			      "dir/relative3.xsd", "../relative4.xsd"],
			     ["./relative1.xsd", "../dir/relative2.xsd",
			      "dir/relative3.xsd"]
			    ]
  }
  
}