package org.gradle.jacobo.plugins.task

import org.gradle.jacobo.plugins.ProjectIntegrationSpec

import org.gradle.jacobo.plugins.ant.AntXjc

class JaxbXjcSpec extends ProjectIntegrationSpec {

  def xjc = Mock(AntXjc)

  def setup() {
    def rootDir = getFileFromResourcePath("/test-jaxb-plugin")
    setRootProject(rootDir)
    setSubProject(rootProject, "multi-parent-child-schema", "com.github.jacobono.jaxb")
    setupProjectTasks()
  }  

  def "attempt an xjc with a mock, just to check guice bindings and runtime or error free"() {
    given: "mock ant executor and execute dependent tasks"
    xjcTask.xjc = xjc
    dependencyTreeTask.execute()
    
    when:
    xjcTask.execute()

    then:
    9 * xjc.execute(*_)
  }

  def "Mock xjc, have some binding files present, to trigger calling xjc once."() {
    given: "mock ant executor and execute dependent tasks ONCE because a binding file is present."
    xjcTask.with {
      bindings = project.files("binding1.xjb", "binding2.xjb")
    }
    xjcTask.xjc = xjc
    dependencyTreeTask.execute()

    when:
    xjcTask.execute()

    then:
    1 * xjc.execute(*_)
  }
}