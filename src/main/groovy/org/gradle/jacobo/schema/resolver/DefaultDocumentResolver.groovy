package org.gradle.jacobo.schema.resolver

import com.google.inject.Inject

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 *  Service object to resolve a {@code SchemaDocument}'s dependencies (which are
 *  usually relative paths) into absolute paths.
 */
class DefaultDocumentResolver implements DocumentResolver {
  private static final Logger log = LoggerFactory.getLogger(DefaultDocumentResolver.class)

  /**
   *  Performs the relative to absolute path resolution
   */
  AbsoluteFileResolver fileResolver

  /**
   *  Construct this object with a {@code AbsoluteFileResolver} service delegate
   */
  @Inject
  DefaultDocumentResolver(AbsoluteFileResolver afr) { 
    this.fileResolver = afr
  }

  /**
   * Resolves relative path dependencies into their absolute path
   * {@link java.io.File} object (relative to the document's current directory).
   * <p>
   * if parsed file {@code /some/dir/file.xsd} has dependencies of
   * <ul>
   * <li> {@code ../otherDir/other.xsd}
   * <li> {@code subDir/sub.xsd}
   * </ul>
   * they will be resolved as
   * <ul>
   * <li> {@code /some/otherDir/other.xsd}
   * <li> {@code /some/dir/subDir/sub.xsd}
   * </ul>
   * 
   * @param relativeDependencies  relative path dependencies of a
   * {@link org.gradle.jacobo.schema.SchemaDocument}.
   * @param documentDirectory  the parent directory of the {@code SchemaDocument}
   * that is being resolved.
   * @return map of relative dependecy Strings as keys and their absolute File
   * as values
   */
  @Override
  public Map<String, File> resolveRelativePaths(
    Set<String> relativeDependencies, File documentDirectory) {
    def resolvedPaths = [:]
    relativeDependencies?.each { relativePath ->
      def file = fileResolver.resolveToAbsolutePath(relativePath,
						    documentDirectory)
      resolvedPaths.put(relativePath, file)
    }
    return resolvedPaths
  }
}