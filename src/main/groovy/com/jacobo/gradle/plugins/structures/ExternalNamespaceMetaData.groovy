package com.jacobo.gradle.plugins.structures

class ExternalNamespaceMetaData extends NamespaceMetaData {
  
  /**
   * this is supposed to be the location of the externally imported namespace.
   * TECHNICALLY this should only be one spot it is located in, not multiple.
   */
  File externalSchemaLocation

  

}