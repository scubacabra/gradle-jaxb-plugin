package org.gradle.jacobo.plugins

import org.gradle.testfixtures.ProjectBuilder
import org.gradle.api.Project

import org.gradle.jacobo.plugins.fixtures.TreeFixture

class ProjectTaskSpecification extends TreeFixture {
  
  def project
  def task

  def setup() { 
    project = ProjectBuilder.builder().build()
    project.apply(plugin: "com.github.jacobono.jaxb")
  }
}