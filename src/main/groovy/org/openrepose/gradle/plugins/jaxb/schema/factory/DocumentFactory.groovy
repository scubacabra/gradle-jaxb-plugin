package org.openrepose.gradle.plugins.jaxb.schema.factory

import org.openrepose.gradle.plugins.jaxb.schema.BaseSchemaDocument

/**
 * Handles the creation of a {@code BaseSchemaDocument}.
 */
interface DocumentFactory {

  /**
   * Creates a {@code BaseSchemaDocument}.
   *
   * @param document  absolute path File to schema document
   * @return {@link BaseSchemaDocument}
   */
  public BaseSchemaDocument createDocument(File document)
}