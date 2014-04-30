package org.gradle.jacobo.plugins.task

import org.gradle.jacobo.schema.factory.DocumentFactory
import org.gradle.jacobo.plugins.JaxbPlugin
import org.gradle.jacobo.plugins.factory.XsdDependencyTreeFactory
import org.gradle.jacobo.plugins.ProjectTaskSpecification
import org.gradle.jacobo.plugins.tree.TreeManager
import org.gradle.jacobo.plugins.resolver.NamespaceResolver
import org.gradle.jacobo.plugins.resolver.ExternalDependencyResolver

class JaxbDependencyTreeSpec extends ProjectTaskSpecification {

  def documentFactory = Mock(DocumentFactory)
  def nsResolver = Mock(NamespaceResolver)
  def externalResolver = Mock(ExternalDependencyResolver)
  def treeFactory = Mock(XsdDependencyTreeFactory)
  def manager = new TreeManager()

  def setup() {
    task = project.tasks[JaxbPlugin.JAXB_XSD_DEPENDENCY_TREE_TASK] as JaxbDependencyTree
    task.with {
      xsds = project.files(documents.documentFile)
      docFactory = documentFactory
      namespaceResolver = nsResolver
      externalDependencyResolver = externalResolver
      dependencyTreeFactory = treeFactory
    }
    task.xsds.files.each { file ->
      def document = documents.find { doc ->
      	doc.documentFile.name == file.name
      }
      1 * documentFactory.createDocument(file) >> document
    }
    1 * treeFactory.createDependencyTree(namespaces, documents) >> manager
    1 * nsResolver.resolve(documents) >> namespaces
    1 * externalResolver.resolve(task.xsds.files, namespaces)
  }

  def "generate a dependency tree from a set of xsd Files"() {
    when:
    task.start()

    then:
    project.jaxb.dependencyGraph instanceof TreeManager
    project.jaxb.dependencyGraph == manager
  }
}