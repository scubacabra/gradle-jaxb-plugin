package org.openrepose.gradle.plugins.jaxb.guice

import com.google.inject.AbstractModule
import org.openrepose.gradle.plugins.jaxb.ant.AntExecutor
import org.openrepose.gradle.plugins.jaxb.ant.AntXjc
import org.openrepose.gradle.plugins.jaxb.converter.NameConverter
import org.openrepose.gradle.plugins.jaxb.converter.NamespaceToEpisodeConverter
import org.openrepose.gradle.plugins.jaxb.factory.XsdDependencyTreeFactory
import org.openrepose.gradle.plugins.jaxb.resolver.EpisodeDependencyResolver
import org.openrepose.gradle.plugins.jaxb.resolver.ExternalDependencyResolver
import org.openrepose.gradle.plugins.jaxb.resolver.NamespaceResolver
import org.openrepose.gradle.plugins.jaxb.resolver.XjcResolver
import org.openrepose.gradle.plugins.jaxb.tree.TreeManager

/**
 * Guice module for binding interfaces to implementation classes.
 * Used to configure everything to be created/run through Guice's
 * {@code createInjector}.
 */
class JaxbPluginModule extends AbstractModule {
  
  /**
   * Configures this module for Guice.
   * The only binding taking place is on {@code NamepsaceToEpisodeConverter},
   * the rest of the entries wouldn't matter either way, it is just for
   * completeness (and OCD) that they are listed.
   */
  @Override
  protected void configure() {
    bind(NamespaceResolver.class)
    bind(ExternalDependencyResolver.class)
    bind(XsdDependencyTreeFactory.class)
    bind(TreeManager.class)
    bind(NameConverter.class).to(NamespaceToEpisodeConverter.class)
    bind(XjcResolver.class)
    bind(EpisodeDependencyResolver.class)
    bind(AntExecutor.class).to(AntXjc.class)
  }
}