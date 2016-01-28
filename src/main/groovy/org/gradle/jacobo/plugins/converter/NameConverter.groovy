package org.gradle.jacobo.plugins.converter

/**
 * Converts a project name to another string.
 */
interface NameConverter {
  
  /**
   * Converts the project name to another string.
   * @param projectName  the project name
   * @return converted project name
   */
  public String convert(String projectName)
}