package org.gradle.jacobo.schema.resolver

/**
 * Service to resolve a relative path to an absolute File.
 */
interface AbsoluteFileResolver {
  
  /**
   * Resolves a relative location to an aboslute path {@link java.io.File}.
   *
   * @param relativeLocation  relative path to be resolved
   * @param parentDirectory  absolute path directory to resolve against
   * @return resolved {@link java.io.File}
   */
  public File resolveToAbsolutePath(String relativeLocation,
				    File parentDirectory)
}