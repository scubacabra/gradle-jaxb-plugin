package org.gradle.jacobo.plugins.resolver

import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

import org.gradle.jacobo.plugins.structures.NamespaceData
import org.gradle.jacobo.schema.BaseSchemaDocument
import org.gradle.jacobo.schema.factory.DocumentFactory

class ExternalDependencyResolver {
  static final Logger log = Logging.getLogger(ExternalDependencyResolver.class)

  DocumentFactory docFactory

  Map<File, BaseSchemaDocument> resolvedHistory = [:]

  // TODO GUICE
  public ExternalDependencyResolver(DocumentFactory docFactory) {
    this.docFactory = docFactory
  }

  public void resolve(Set<File> xsds, List<NamespaceData> namespaces) {
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