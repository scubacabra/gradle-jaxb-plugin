package com.jacobo.gradle.plugins.util

/**
 * This class is a helper class for List operations like contains and the like, because it is appearing in more than just one place
 *
 * @author Daniel Mijares
 * @version 1.0
 */

public class ListUtil { 

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

}