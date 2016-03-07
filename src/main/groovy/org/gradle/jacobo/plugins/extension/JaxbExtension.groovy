package org.gradle.jacobo.plugins.extension

import org.gradle.api.Project

/**
 * {@code JaxbPlugin}'s default settings and conventions.
 */
class JaxbExtension {
  
  /**
   * This plugins project.
   */
  private Project project

  /**
   * Folder name (under {@link org.gradle.api.Project#getRootDir()}) containing
   * xsd files to parse.
   */
  String xsdDir

  /**
   * User defined list of RegEx strings to include from the {@code xsdDir}.
   */
  List xsdIncludes

  /**
   * Folder name (under {@link org.gradle.api.Project#getRootDir()}) containing
   * user defined binding files to bind during the {@code xjc} task.
   */
  String bindingsDir

  /**
   * Folder name (under {@link org.gradle.api.Project#getRootDir()}) containing
   * episode files to automatically find and bind during the {@code xjc} task.
   */
  String episodesDir
  
  /**
   * User defined bindings to bind during {@code xjc} task.
   * Must include extension (i.e. {@code user-binding.episode} etc.).
   */
  List bindings

  /**
   * Holds the result of the {@code xsd-dependency-tree} task.
   * Used in the {@code xjc} task to parse sets of namespaces according to
   * their dependency tree hierarchy.
   */
  def dependencyGraph

  /**
   * Creates this extension with associated project.
   */
  JaxbExtension(Project project) { 
    this.project = project
  }
}