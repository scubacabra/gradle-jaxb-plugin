package org.gradle.jacobo.plugins.task

import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.jacobo.plugins.JaxbPlugin
import org.gradle.jacobo.plugins.ant.AntExecutor
import org.gradle.jacobo.plugins.converter.NamespaceToEpisodeConverter
import org.gradle.jacobo.plugins.tree.TreeManager
import org.gradle.jacobo.plugins.tree.TreeNode
import org.gradle.jacobo.plugins.resolver.EpisodeDependencyResolver
import org.gradle.jacobo.plugins.resolver.XjcResolver
import org.gradle.jacobo.plugins.xsd.XsdNamespace

/**
 * @author jacobono
 * Created: Tue Dec 04 09:01:34 EST 2012
 */

class JaxbXjc extends DefaultTask {
  static final Logger log = Logging.getLogger(JaxbXjc.class)

  TreeManager manager

  /**
   * If an xsd namespace has dependencies, references episode files from
   * this directory. Must write own episode file to this directory
   */
  @OutputDirectory
  File episodeDirectory

  /**
   * directory where the generated java files from xjc would go
   * Usually <pre><project-root>/src/main/java</pre>
   */
  @OutputDirectory
  File generatedFilesDirectory

  /**
   * All schemas to be parsed are under this directory
   */
  @InputDirectory
  File schemasDirectory

  /**
   * jaxb custom binding files to bind with
   */
  @InputFiles
  FileCollection bindings

  AntExecutor xjc

  NamespaceToEpisodeConverter episodeConverter

  XjcResolver xjcResolver

  EpisodeDependencyResolver dependencyResolver

  @TaskAction
  void start() {
    def manager = getManager()
    log.info("jaxb: attempting to parse '{}' nodes in tree, base nodes are '{}'",
	     manager.managedNodes.size(), manager.treeRoot)
    def nodes = manager.treeRoot
    while(nodes) {
      log.info("parsing '{}' nodes '{}'", nodes.size(), nodes)
      nodes.each { node -> parseNode(node) }
      nodes = manager.getNextDescendants(nodes)
    }
  }

  def parseNode(TreeNode<XsdNamespace> node) {
    log.info("resolving necessary information for node '{}'", node)
    def episodes = getDependencyResolver().resolve(node, getEpisodeConverter(),
						   getEpisodeDirectory())
    def xsds = getXjcResolver().resolve(node.data)
    def episode = getEpisodeConverter().convert(node.data.namespace)
    def episodeFile = new File(getEpisodeDirectory(), episode)

    log.info("running ant xjc task on node '{}'", node)
    def jaxbConfig = project.configurations[JaxbPlugin.JAXB_CONFIGURATION_NAME]
    getXjc().execute(ant, project.jaxb.xjc, jaxbConfig.asPath, project.files(xsds),
		     getBindings(), project.files(episodes), episodeFile)
  }
}