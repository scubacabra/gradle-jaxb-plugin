package org.openrepose.gradle.plugins.jaxb.schema.guice


import com.google.inject.AbstractModule
import com.google.inject.assistedinject.FactoryModuleBuilder
import com.google.inject.name.Names

import org.openrepose.gradle.plugins.jaxb.schema.factory.DefaultDocumentFactory
import org.openrepose.gradle.plugins.jaxb.schema.factory.DocumentFactory
import org.openrepose.gradle.plugins.jaxb.schema.resolver.AbsoluteFileResolver
import org.openrepose.gradle.plugins.jaxb.schema.resolver.DefaultAbsoluteFileResolver
import org.openrepose.gradle.plugins.jaxb.schema.resolver.DefaultDocumentResolver
import org.openrepose.gradle.plugins.jaxb.schema.resolver.DocumentResolver
import org.openrepose.gradle.plugins.jaxb.schema.slurper.DefaultDocumentSlurper
import org.openrepose.gradle.plugins.jaxb.schema.slurper.DefaultXsdSlurper
import org.openrepose.gradle.plugins.jaxb.schema.slurper.DocumentSlurper
import org.openrepose.gradle.plugins.jaxb.schema.WsdlDocument
import org.openrepose.gradle.plugins.jaxb.schema.BaseSchemaDocument
import org.openrepose.gradle.plugins.jaxb.schema.SchemaDocumentFactory
import org.openrepose.gradle.plugins.jaxb.schema.XsdDocument
import org.openrepose.gradle.plugins.jaxb.schema.slurper.XsdSlurper

/**
 * Guice module binding interfaces to implementation classes.
 * Used to configure everything to run through Guice's {@code createInjector}
 */
class DocSlurperModule extends AbstractModule {
  
  /**
   * Configures this Guice's module
   */
  @Override
  protected void configure() {
    // bind ___ interface to ----> ___ implementation
    bind(DocumentResolver).to(DefaultDocumentResolver);
    bind(XsdSlurper).to(DefaultXsdSlurper);
    bind(DocumentSlurper).to(DefaultDocumentSlurper);
    bind(AbsoluteFileResolver).to(DefaultAbsoluteFileResolver);
    bind(DocumentFactory).to(DefaultDocumentFactory);
    // schemadocumentfactory implement factory, return type BaseSchemaDocument
    // Name annotation returns particular implementation
    // build SchemaDocumentFactory <-- look for specifics
    install(new FactoryModuleBuilder()
    	    .implement(BaseSchemaDocument, Names.named("xsd"), XsdDocument)
    	    .implement(BaseSchemaDocument, Names.named("wsdl"), WsdlDocument)
    	    .build(SchemaDocumentFactory)
    	   )
  }
}