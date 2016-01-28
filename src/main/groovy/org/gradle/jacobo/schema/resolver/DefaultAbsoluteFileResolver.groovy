package org.gradle.jacobo.schema.resolver

import org.slf4j.Logger
import org.slf4j.LoggerFactory
/**
 * Service to resolve a relative path to an absolute File.
 */
class DefaultAbsoluteFileResolver implements AbsoluteFileResolver {
  private static final Logger log = LoggerFactory.getLogger(DefaultDocumentResolver.class)
  
  /**
   * Resolves a relative location to an aboslute path {@link java.io.File}.
   *
   * @param relativeLocation  relative path to be resolved
   * @param parentDirectory  absolute path directory to resolve against
   * @return resolved {@link java.io.File}
   */
  public File resolveToAbsolutePath(String relativeLocation,
				    File parentDirectory) {
    def relativeFile = new File(parentDirectory, relativeLocation)
    def resolvedFile = new File(relativeFile.canonicalPath)
    log.debug("location '{}' relative to '{}' resolved to '{}'",
	      relativeLocation, parentDirectory, resolvedFile)
    return resolvedFile
  }
}