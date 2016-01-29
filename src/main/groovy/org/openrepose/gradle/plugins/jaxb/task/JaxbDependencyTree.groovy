package org.openrepose.gradle.plugins.jaxb.task

import org.gradle.api.logging.Logging
import org.gradle.api.logging.Logger
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.InputFiles
import org.gradle.api.file.FileCollection
import org.gradle.api.DefaultTask
import org.openrepose.gradle.plugins.jaxb.factory.XsdDependencyTreeFactory
import org.openrepose.gradle.plugins.jaxb.resolver.ExternalDependencyResolver
import org.openrepose.gradle.plugins.jaxb.resolver.NamespaceResolver
import org.openrepose.gradle.plugins.jaxb.schema.factory.DocumentFactory

/**
 * Plugin's task to generate an xsd dependency tree.
 */
class JaxbDependencyTree extends DefaultTask { 
  
  static final Logger log = Logging.getLogger(JaxbDependencyTree.class)
  
  /**
   * Xsd's under defined {@code xsdDir}.
   */
  @InputFiles
  FileCollection xsds

  /**
   * Generates {@code BaseSchemaDocument}'s.
   */
  DocumentFactory docFactory

  /**
   * Resolves xsds files into namespace containers.
   */
  NamespaceResolver namespaceResolver

  /**
   * Resolves and slurps external dependencies.
   */
  ExternalDependencyResolver externalDependencyResolver

  /**
   * Generates the xsd dependency tree.
   */
  XsdDependencyTreeFactory dependencyTreeFactory

  /**
   * Executes this task.
   */
  @TaskAction
  void start() {
    log.lifecycle("jaxb: starting Namespace Task")
    def xsdFiles = getXsds().files
    def documents = xsdFiles.collect{file -> docFactory.createDocument(file)}
    def namespaces = getNamespaceResolver().resolve(documents)
    getExternalDependencyResolver().resolve(xsdFiles, namespaces)
    def tree = getDependencyTreeFactory().createDependencyTree(namespaces, documents)
    project.jaxb.dependencyGraph = tree
  }
}