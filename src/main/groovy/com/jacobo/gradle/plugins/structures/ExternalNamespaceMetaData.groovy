package com.jacobo.gradle.plugins.structures

import com.jacobo.gradle.plugins.model.XsdSlurper

class ExternalNamespaceMetaData extends NamespaceMetaData {
  
  /**
   * this is supposed to be the location of the externally imported namespace.
   * TECHNICALLY this should only be one spot it is located in, not multiple.
   */
  File externalSchemaLocation

  /**
   * Slurper object for an externally Imported File
   **/
  XsdSlurper externalSlurper

}