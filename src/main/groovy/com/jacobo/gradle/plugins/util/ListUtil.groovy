package com.jacobo.gradle.plugins.util

import com.jacobo.gradle.plugins.structures.NamespaceMetaData

import org.gradle.api.logging.Logging
import org.gradle.api.logging.Logger

/**
 * This class is a helper class for List operations like contains and the like, because it is appearing in more than just one place
 *
 * @author Daniel Mijares
 * @version 1.0
 */

public class ListUtil { 

  private static final Logger log = Logging.getLogger(ListUtil.class)

  /**
   * Utility function that checks if a @List has the value @input
   */
  static boolean isAlreadyInList(List list, String input) { 
    return list.contains(input)
  }

  /**
   * Utility function that checks if a @List has the value @input
   */
  static  boolean isAlreadyInList(List list, File input) { 
    return list.contains(input)
  }

  /**
   * Utility function that checks if a @List has the value @input
   */
  static  boolean isAlreadyInList(List list, NamespaceMetaData input) { 
    return list.contains(input)
  }

  /**
   * @param collection the list of namespace meta data to check against for external dependencies, i.e. not in this meta data set
   * @param namespace the namespace #String to compare against for external dependencies
   * checks to see if this namespace is part of the unique namespaces being processed right now, if it isn't it is externally imported
   */
  static boolean isImportedNamespaceExternal(collection, namespace) {
    return (collection.find{it.namespace == namespace}) ? false : true
  }

  /**
   * Adds an element to the list, if the element does not already contain that element
   * @param list is the list to add an element to
   * @param input is the element to add to the list
   */
  static addElementToList(List list, input) { 
    log.debug("trying to add element {} to list {}", input, list)
    if(!isAlreadyInList(list, input)) { 
      log.debug("Input {} is not in list,  adding to it", input)
      list << input
    }
  }
}