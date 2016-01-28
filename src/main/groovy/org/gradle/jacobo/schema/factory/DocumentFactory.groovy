package org.gradle.jacobo.schema.factory

import org.gradle.jacobo.schema.BaseSchemaDocument

/**
 * Handles the creation of a {@code BaseSchemaDocument}.
 */
interface DocumentFactory {

  /**
   * Creates a {@code BaseSchemaDocument}.
   *
   * @param document  absolute path File to schema document
   * @return {@link org.gradle.jacobo.schema.BaseSchemaDocument}
   */
  public BaseSchemaDocument createDocument(File document)
}