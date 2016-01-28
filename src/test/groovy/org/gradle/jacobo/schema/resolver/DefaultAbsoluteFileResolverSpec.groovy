package org.gradle.jacobo.schema.resolver

import spock.lang.Specification
import spock.lang.Unroll

class DefaultAbsoluteFileResolverSpec extends Specification {
  
  def absoluteResolver = new DefaultAbsoluteFileResolver()

  @Unroll
  def "resolve '#relativeLocation' relative to '#parent' -- '#result'"() {

  expect:
  result == absoluteResolver.resolveToAbsolutePath(relativeLocation, parent)

  where:
  parent                    | relativeLocation                  || result
  new File("/schema/group") | "../otherGroup/subGroup/type.xsd" || new File("/schema/otherGroup/subGroup/type.xsd")
  new File("/wsdl/concrete")| "../abstract/abstract.wsdl"       || new File("/wsdl/abstract/abstract.wsdl")
  new File("/wsdl/concrete")| "../../equal.xsd"                 || new File("/equal.xsd")
  new File("/someDir")      | "./subDir/sub.xsd"                || new File("/someDir/subDir/sub.xsd")
  new File("/someDir")      | "subDir/other-sub.xsd"            || new File("/someDir/subDir/other-sub.xsd")
  }
}