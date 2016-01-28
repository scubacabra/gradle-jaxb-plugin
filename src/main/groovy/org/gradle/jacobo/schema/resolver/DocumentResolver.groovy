package org.gradle.jacobo.schema.resolver

/**
 *  Service object to resolve a {@code SchemaDocument}'s dependencies (which are
 *  usually relative paths) into absolute paths.
 */
interface DocumentResolver {
  
  /**
   * Resolves relative path dependencies into their absolute path
   * {@link java.io.File} object (relative to the document's current directory).
   *
   * @param relativeDependencies  relative path dependencies of a
   * {@link org.gradle.jacobo.schema.SchemaDocument}.
   * @param documentDirectory  the parent directory of the {@code SchemaDocument}
   * that is being resolved.
   * @return map of relative dependecy Strings as keys and their absolute File
   * as values
   */
  public Map<String, File> resolveRelativePaths(
    Set<String> relativeDependencies,
    File documentDirectory)
}