package org.gradle.jacobo.plugins.resolver

import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

import org.gradle.jacobo.plugins.converter.NamespaceToEpisodeConverter
import org.gradle.jacobo.plugins.model.TreeNode
import org.gradle.jacobo.plugins.structures.NamespaceData

class EpisodeDependencyResolver {
  static final Logger log = Logging.getLogger(EpisodeDependencyResolver.class)  

  /**
   * @param Tree Node with NamespaceData as a data object
   * @return List of episode names to bind with.
   * Based off of the current Node looking to parse.  Node's parents
   * form the core episode bindings, but the node, along with each
   * one of its parents can depend on external namespaces, and these
   * can be duplicated, so a Set of dependentNamespaces is constructed
   * and then each namespace is converted to it episode file name.
   */
  public Set<File> resolve(TreeNode<NamespaceData> node,
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