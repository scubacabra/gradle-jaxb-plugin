package org.openrepose.gradle.plugins.jaxb

import org.gradle.testfixtures.ProjectBuilder
import org.openrepose.gradle.plugins.jaxb.fixtures.TreeFixture

class ProjectTaskSpecification extends TreeFixture {
  
  def project
  def task

  def setup() { 
    project = ProjectBuilder.builder().build()
    project.apply(plugin: "org.openrepose.gradle.plugins.jaxb")
  }
}