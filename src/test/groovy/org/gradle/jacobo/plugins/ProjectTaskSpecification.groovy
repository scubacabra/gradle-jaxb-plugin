package org.gradle.jacobo.plugins

import org.gradle.jacobo.plugins.BaseSpecification

import org.gradle.testfixtures.ProjectBuilder
import org.gradle.api.Project

class ProjectTaskSpecification extends BaseSpecification {
  
  def project
  def task

  def setup() { 
    project = ProjectBuilder.builder().build()
    project.apply(plugin: "jaxb")
  }
}