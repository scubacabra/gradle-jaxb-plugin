package com.jacobo.gradle.plugins.util

import com.jacobo.gradle.plugins.structures.NamespaceMetaData

import org.gradle.api.logging.Logging
import org.gradle.api.logging.Logger

class XJCInputResolver {
  
  static final Logger log = Logging.getLogger(XJCInputResolver.class)

  /**
   * @param bindings a list of user defined bindings
   * @return String binding list transformed into a string so the xjc task can accordingly process it
   * Takes a binding list (user defined) and turns it into an appropriate string for the xjc ant task to use
   */
  public static String transformBindingListToString(List bindings) { 
    def bindingsToInclude = ""
    log.debug("Binding list is {}", bindings)
    bindings.each { 
      bindingsToInclude += it + " "
    }
    log.debug("binding list into xjc is {}", bindingsToInclude)
    return bindingsToInclude
  }

  /**
   * @param namespaceMetaData an object of type #NamespaceMetaData
   * @return String represenation of the list of included schemas to undergo parsing via xjc, so that no extra schemas are processed
   * Takes a #NamespaceMetaData object and gathers each of it's parseFiles list and turns it into a relative path and concatenates a string with these new relative paths
   */
  public static String transformSchemaListToString(NamespaceMetaData namespaceMetaData) { 
    def includes = ""
    namespaceMetaData?.parseFiles?.each { path ->
      includes += path.name + " "
    }
    log.info("argument to inludes is {}", includes)
    return includes
  }


  
}