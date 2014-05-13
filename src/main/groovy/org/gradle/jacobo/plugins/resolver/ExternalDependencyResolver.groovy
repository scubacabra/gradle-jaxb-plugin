package org.gradle.jacobo.plugins.resolver

import com.google.inject.Inject
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

import org.gradle.jacobo.plugins.xsd.XsdNamespace
import org.gradle.jacobo.schema.BaseSchemaDocument
import org.gradle.jacobo.schema.factory.DocumentFactory

class ExternalDependencyResolver {
  static final Logger log = Logging.getLogger(ExternalDependencyResolver.class)
  
  /**
   * Generates {@code BaseSchemaDocument}'s
   */
  DocumentFactory docFactory

  /**
   * History of files in a file system to their corresponding
   * {@code BaseSchemaDocument}.
   */
  Map<File, BaseSchemaDocument> resolvedHistory = [:]

  /**
   * Creates this resolver.
   *
   * @param docFactory  generates {@code BaseSchemaDocument}'s
   */
  @Inject
  public ExternalDependencyResolver(DocumentFactory docFactory) {
    this.docFactory = docFactory
  }

  /**
   * Resolves the external dependencies for xsds.
   * An external dependency is a dependency an xsd has that is contained in a
   * different parent folder than the xsd.  This check is done by seeing if a
   * dependency is in the set of xsds found recursively from the {@code xsdDir}
   * defined in {@code JaxbExtension}.
   * <p>
   * This method returns nothing, but the flag {@code hasDependencies} is set
   * on an {@code XsdNamespace} if a namespace contains <b>only</b> internal (not
   * external) dependencies or if a namespaces total dependencies do not equal its
   * total external dependencies.
   *
   * @param xsds  all xsds under {@code xsdDir}
   * @param namespaces  list of namespaces xsds are grouped by
   */
  public void resolve(Set<File> xsds, List<XsdNamespace> namespaces) {
    log.info("resolving external dependencies for namespaces '{}'", namespaces)
    namespaces.each { namespace ->
      def dependencies = []
      namespace.documents.each {
	dependencies.addAll(it.findResolvedXsdImports())
      }
      log.debug("total number of dependencies '{}'", dependencies.size)
      // break to next iteration, nothing to do: hasDeps default to none
      if (dependencies.isEmpty()) return true 
      def externalDeps = dependencies.findAll { file -> !xsds.contains(file) }
      // no external dependencies, all internal, set flag and break iteration
      if (!externalDeps) {
	namespace.hasDependencies = true
	return true
      }

      log.debug("'{}' has '{}' external dependencies", namespace,
		externalDeps.size)
      externalDeps.each { externalFile ->
	def resolved = resolveDependencies(externalFile)
	log.debug("found '{}' dependencies, including '{}'",
		  resolved.size(), externalFile)
	namespace.externalDependencies.addAll(resolved.xsdNamespace)
      }
      if (dependencies.size != externalDeps.size) namespace.hasDependencies = true
    }
  }

  /**
   * Resolves dependencies of an external dependency.
   * Recursively slurps and searches for a files dependencies, until there are
   * none left to search for.
   *
   * @param document  external xsd file that needs to be process and slurped 
   * @return set of documents to add as external namespaces for an
   * {@code XsdNamespace}
   */
  public Set<BaseSchemaDocument> resolveDependencies(File document) {
    log.info("resolving dependencies on xsd '{}'", document)
    // dependencies INCLUDING input document
    Set<BaseSchemaDocument> dependencies = [] as Set
    def unresolvedDependencies = [document] as Set
    while(!unresolvedDependencies.isEmpty()) {
      def file = unresolvedDependencies.iterator().next()
      def xsd = docFactory.createDocument(file)
      // file now resolved, remove from unsresolved Set
      unresolvedDependencies.remove(file)
      // this.resolvedHistory.add(file)
      dependencies.add(xsd)
      log.debug("retrieving dependencies from document '{}'", xsd)
      xsd.findResolvedXsdImports().each { dependentFile ->
	// def inHistory = this.resolvedDependencies.contains(dependentFile)
	// if (inHistory) {
	//   log.debug("dependency '{}' was resolved prior", dependentFile)
	//   return true // break this iteration -- dep in history, move to next dep
	// }
	unresolvedDependencies.add(dependentFile)
      }
    }
    log.debug("'{}' has '{}' dependencies", document, dependencies.size() - 1)
    return dependencies
  }
}