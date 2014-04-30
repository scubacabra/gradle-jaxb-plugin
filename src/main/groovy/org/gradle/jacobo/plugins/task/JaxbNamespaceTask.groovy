package org.gradle.jacobo.plugins.task

import org.gradle.api.logging.Logging
import org.gradle.api.logging.Logger
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.InputFiles
import org.gradle.api.file.FileCollection
import org.gradle.api.DefaultTask

import org.gradle.jacobo.schema.factory.DocumentFactory
import org.gradle.jacobo.plugins.factory.XsdDependencyTreeFactory
import org.gradle.jacobo.plugins.resolver.NamespaceResolver
import org.gradle.jacobo.plugins.resolver.ExternalDependencyResolver

/**
 * @author jacobono
 * Created: Tue Dec 04 09:01:34 EST 2012
 */

class JaxbNamespaceTask extends DefaultTask { 
  
  static final Logger log = Logging.getLogger(JaxbNamespaceTask.class)
  
  @InputFiles
  FileCollection xsds

  DocumentFactory docFactory

  NamespaceResolver namespaceResolver

  ExternalDependencyResolver externalDependencyResolver

  XsdDependencyTreeFactory dependencyTreeFactory

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