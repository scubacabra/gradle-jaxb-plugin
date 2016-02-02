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
 * Plugin's task to run xsd files through ant {@code xjc} task.
 */
class JaxbXjc extends DefaultTask {
  static final Logger log = Logging.getLogger(JaxbXjc.class)

  /**
   * Contains and manages the xsd dependency tree.
   */
  TreeManager manager

  /**
   * Directory where all episode files are located.
   */
  @OutputDirectory
  File episodeDirectory

  /**
   * Directory where the generated java files from xjc would go
   * Usually {@code <project-root>/src/main/java}
   */
  @OutputDirectory
  File generatedFilesDirectory

  /**
   * Xsd's defined under {@code xsdDir}.
   */
  @InputFiles
  FileCollection xsds

  /**
   * Directory containing all the xsds to be parsed.
   */
  @InputDirectory
  File schemasDirectory

  /**
   * User defined custom bindings to bind in ant task.
   */
  @InputFiles
  FileCollection bindings

  /**
   * Executes the ant {@code xjc} task.
   */
  AntExecutor xjc

  /**
   * Converts xsd namespaces to episode file names.
   */
  NamespaceToEpisodeConverter episodeConverter

  /**
   * Resolves the input files for the ant task to parse.
   */
  XjcResolver xjcResolver

  /**
   * Resolves a node's dependencies to episode files to bind in ant task.
   */
  EpisodeDependencyResolver dependencyResolver

  /**
   * Executes this task.
   * Starts at dependency tree root (no dependencies) and parses each node's
   * information to pass to the ant task until there are no more nodes left to
   * parse.
   */
  @TaskAction
  void start() {
    def manager = getManager()

    if (!getBindings().isEmpty()) {
      // have bindings, can't use Node processing, one xjc run and exit
      log.info("bindings are present, running ant xjc task on all xsds in '{}' and then exiting!",
               getSchemasDirectory())
      def namespace = manager.treeRoot.pop().data.namespace ?: "random-namespace"
      xjc(getXsds(), [], getEpisodeFile(namespace))
      return
    }

    log.info("jaxb: attempting to parse '{}' nodes in tree, base nodes are '{}'",
             manager.managedNodes.size(), manager.treeRoot)
    def nodes = manager.treeRoot
    while(nodes) {
      log.info("parsing '{}' nodes '{}'", nodes.size(), nodes)
      nodes.each { node -> parseNode(node) }
      nodes = manager.getNextDescendants(nodes)
    }
  }

  /**
   * Parses a {@code TreeNode} and passes data through to ant task.
   * 
   * @param node  tree node to run through ant task
   */
  def parseNode(TreeNode<XsdNamespace> node) {
    log.info("resolving necessary information for node '{}'", node)
    def episodes = getDependencyResolver().resolve(node, getEpisodeConverter(),
                                                   getEpisodeDirectory())
    def xsds = getXjcResolver().resolve(node.data)

    log.info("running ant xjc task on node '{}'", node)
    xjc(xsds, episodes, getEpisodeFile(node.data.namespace))
  }

  def xjc(xsds, episodes, episodeFile) {
    def jaxbConfig = project.configurations[JaxbPlugin.JAXB_CONFIGURATION_NAME]
    log.debug("episodes are '{}' is empty '{}'", episodes, episodes.isEmpty())
    new File((String)project.jaxb.xjc.destinationDir).mkdirs()
    getXjc().execute(ant, project.jaxb.xjc, jaxbConfig.asPath, project.files(xsds),
                     getBindings(), project.files(episodes), episodeFile)
  }

  def getEpisodeFile(xsdNamespace) {
    def episode = getEpisodeConverter().convert(xsdNamespace)
    return new File(getEpisodeDirectory(), episode)
  }
}