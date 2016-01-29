package org.openrepose.gradle.plugins.jaxb.resolver

import org.gradle.api.GradleException
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.openrepose.gradle.plugins.jaxb.xsd.XsdNamespace

/**
 * Resolves files to be passed into an {@code xjc} task to be parsed.
 * Sometimes a namespace has a bunch of documents associated with it, because a file usually
 * includes another xsd file that contains the same namespace.
 * <p>
 * Resolves the included files by keeping them <b>out</b> of the 'official' list of files to
 * parse in the task.  They will be included anyway, as the xjc task parses the xsd itself.
 * The only benefit of doing this, besides making it clear which xsd(s) are doing the
 * including, is to catch circular dependencies that sometimes arise when all xsd's include
 * each other.  This is nice to catch early, so you don't have to wade through jaxb debug.
 */
class XjcResolver {
  static final Logger log = Logging.getLogger(XjcResolver.class)

  // get all included files for this namespace and subtract that set from the
  // total set of files this namespace includes  
  /**
   * Resolves the files to actually be parsed by the {@code xjc} task.
   *
   * @param namespace  namespace containing the files to resolve
   * @return set of files for xjc task to parse
   * @throws org.gradle.api.GradleException when there is a circular 'includes' dependency.
   *         Needs fixing before continuing.
   */
  public Set<File> resolve(XsdNamespace namespace) {

    def totalXsds = namespace.documents.documentFile as Set
    // included schemas are subset of totalXsds
    def alreadyIncluded = [] as Set
    namespace.documents.each { document ->
      alreadyIncluded.addAll(document.findResolvedXsdIncludes())
    }
    // keep only what is unique to total as alreadyIncluded is subset of that
    def xjcParseFiles = totalXsds - alreadyIncluded

    log.debug("namespace '{}' contains files '{}' and includes '{}'",
	     namespace.namespace, totalXsds, alreadyIncluded)

    if(xjcParseFiles.isEmpty()) { 
      def exceptionString = "namespace '${namespace.namespace}' contains no files to" +
	"actually parse due to a circular dependency in the includes files." +
	"Check the files and their include statements.  Namespace has " +
	"'${totalXsds.size()}' slurped files -> '${totalXsds}' and " +
	"includes '${alreadyIncluded.size()}' files -> '${alreadyIncluded}'."
      throw new GradleException(exceptionString)
    }
    return xjcParseFiles
  }
}