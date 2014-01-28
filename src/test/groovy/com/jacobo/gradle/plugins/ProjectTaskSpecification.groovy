package com.jacobo.gradle.plugins

import com.jacobo.gradle.plugins.BaseSpecification

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