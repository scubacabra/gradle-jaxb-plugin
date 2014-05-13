package org.gradle.jacobo.plugins.resolver

import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

import org.gradle.jacobo.plugins.converter.NamespaceToEpisodeConverter
import org.gradle.jacobo.plugins.tree.TreeNode
import org.gradle.jacobo.plugins.xsd.XsdNamespace

/**
 * Resolves a {@code TreeNode}'s dependencies to their corresponding episode
 * files in the {@code episodesDir} configured through the {@code JaxbExtension}.
 */
class EpisodeDependencyResolver {
  static final Logger log = Logging.getLogger(EpisodeDependencyResolver.class)  

  /**
   * Resolves the nodes dependencies into episode files.
   * External dependencies are also resolved.
   * <p>
   * Gets a {@code TreeNode}'s ancestors and converts them to episode Files.
   * Gets nodes external dependencies and gets its ancestors external
   * dependencies as well.  Episode Files are not checked for existence.
   * 
   * @param node  tree node to operate on
   * @param converter  converts namespace to an episode name
   * @param episodesDir  the directory where to find episode files
   * @return set of episode Files to bind during the {@code xjc} task
   */
  public Set<File> resolve(TreeNode<XsdNamespace> node,
			   NamespaceToEpisodeConverter converter,
			   File episodesDir) {
    def ancestors = node.getAncestors()
    def dependentNamespaces = ancestors.data.namespace
    // external dependencies is a set of namespace strings as well
    ancestors.data.each {
      dependentNamespaces.addAll(it.externalDependencies)
    }
    // current node may(not) have external dependencies, empty set if not
    dependentNamespaces.addAll(node.data.externalDependencies)
    log.info("node '{}' depends on a total of '{}' namespaces", node,
	     dependentNamespaces.size())

    //empty dependencies, nothing to resolve into episode Files
    if(dependentNamespaces.isEmpty()) return [] as Set

    def episodes = dependentNamespaces.collect { converter.convert(it) }
    def episodeFiles = episodes.collect { episode -> new File(episodesDir, episode) }
    return episodeFiles
  }
}