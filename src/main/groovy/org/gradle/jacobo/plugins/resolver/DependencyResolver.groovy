package org.gradle.jacobo.plugins.resolver

/**
 * Resolves {@code SchemaDocument}'s dependencies.
 */
interface DependencyResolver {
  
  /**
   * Resolves dependencies of {@code File}.

   * @param document  file to resolve
   * @return Set of files this document depends on
   */
  public Set<File> resolveDependencies(File document)
}