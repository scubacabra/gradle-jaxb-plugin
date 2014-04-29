package org.gradle.jacobo.plugins.resolver

import org.gradle.api.GradleException
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.jacobo.plugins.structures.NamespaceData

class XjcResolver {
  static final Logger log = Logging.getLogger(XjcResolver.class)

  // get all included files for this namespace and subtract that set from the
  // total set of files this namespace includes  
  public Set<File> resolve(NamespaceData namespace) {

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