package org.gradle.jacobo.plugins.guice

import com.google.inject.AbstractModule

import org.gradle.jacobo.plugins.ant.AntExecutor
import org.gradle.jacobo.plugins.ant.AntXjc
import org.gradle.jacobo.plugins.converter.NameConverter
import org.gradle.jacobo.plugins.converter.NamespaceToEpisodeConverter
import org.gradle.jacobo.plugins.factory.XsdDependencyTreeFactory
import org.gradle.jacobo.plugins.resolver.EpisodeDependencyResolver
import org.gradle.jacobo.plugins.resolver.ExternalDependencyResolver
import org.gradle.jacobo.plugins.resolver.NamespaceResolver
import org.gradle.jacobo.plugins.resolver.XjcResolver
import org.gradle.jacobo.plugins.model.TreeManager

class JaxbPluginModule extends AbstractModule {
  
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