package org.gradle.jacobo.schema.guice


import com.google.inject.AbstractModule
import com.google.inject.assistedinject.FactoryModuleBuilder
import com.google.inject.name.Names

import org.gradle.jacobo.schema.factory.DefaultDocumentFactory
import org.gradle.jacobo.schema.factory.DocumentFactory
import org.gradle.jacobo.schema.resolver.AbsoluteFileResolver
import org.gradle.jacobo.schema.resolver.DefaultAbsoluteFileResolver
import org.gradle.jacobo.schema.resolver.DefaultDocumentResolver
import org.gradle.jacobo.schema.resolver.DocumentResolver
import org.gradle.jacobo.schema.slurper.DefaultDocumentSlurper
import org.gradle.jacobo.schema.slurper.DefaultXsdSlurper
import org.gradle.jacobo.schema.slurper.DocumentSlurper
import org.gradle.jacobo.schema.slurper.XsdSlurper
import org.gradle.jacobo.schema.XsdDocument
import org.gradle.jacobo.schema.WsdlDocument
import org.gradle.jacobo.schema.BaseSchemaDocument
import org.gradle.jacobo.schema.SchemaDocumentFactory

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