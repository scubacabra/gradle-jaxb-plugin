package org.gradle.jacobo.plugins.extension
/**
 * {@code JaxbPlugin}'s default settings and and conventions for the
 * {@code xjc} task.
 */
class XjcExtension {

  /**
   * Specifies the classname to use for the xjc task. If specifiying a custom one, be sure to add
   * it to the configuration classpath.
   */
  String taskClassname

  /**
   * Destination directory for the generated output from the {@code xjc} task.
   * If the Path is not absolute, then it is assumed to be relative to this
   * projects root directory.
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

  /**
   * 'package' argument for {@code xjc} task. If specified, generated code will be placed under this Java package.
   * See <a href="https://jaxb.java.net/2.2.4/docs/xjcTask.html">jaxb ant task</a>
   */
  String generatePackage

  /**
   * optional args argument for {@code xjc} task.
   * See <a href="https://jaxb.java.net/2.2.4/docs/xjcTask.html">jaxb ant task</a>
   */
  List<String> args = []

}
