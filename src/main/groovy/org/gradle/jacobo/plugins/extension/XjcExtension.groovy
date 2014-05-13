package org.gradle.jacobo.plugins.extension

import org.gradle.api.Project

/**
 * {@code JaxbPlugin}'s default settings and and conventions for the
 * {@code xjc} task.
 */
class XjcExtension {

  /**
   * Destination directory for the generated output from the {@code xjc} task.
   * Path is relative to this individual projects directory <b>NOT</b> the root
   * directory.
   * See <a href="https://jaxb.java.net/2.2.4/docs/xjcTask.html">jaxb ant task</a>
   */
  String destinationDir

  /**
   * Produces directory for the generated output from the {@code xjc} task.
   * <b>NOT</b> used at any point in this processing.  Possible to include sometime.
   * See <a href="https://jaxb.java.net/2.2.4/docs/xjcTask.html">jaxb ant task</a>
   */
  String producesDir

  /**
   * boolean argument for {@code xjc} task.
   * See <a href="https://jaxb.java.net/2.2.4/docs/xjcTask.html">jaxb ant task</a>
   */
  String extension

  /**
   * boolean argument for {@code xjc} task.
   * See <a href="https://jaxb.java.net/2.2.4/docs/xjcTask.html">jaxb ant task</a>
   */
  String removeOldOutput

  /**
   * boolean argument for {@code xjc} task.
   * See <a href="https://jaxb.java.net/2.2.4/docs/xjcTask.html">jaxb ant task</a>
   */
  boolean header

}