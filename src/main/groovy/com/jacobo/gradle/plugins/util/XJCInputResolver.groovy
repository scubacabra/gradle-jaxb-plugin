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
    namespaceMetaData?.filesToParse().each { path ->
      includes += path.name + " "
    }
    log.info("argument to inludes is {}", includes)
    return includes
  }

  /**
   * @param namespaceMetaData an object of type #NamespaceMetaData
   * @return String represenation of the list of included schemas to undergo parsing via xjc, so that no extra schemas are processed
   * Takes a #NamespaceMetaData object and gathers each of it's parseFiles list and turns it into a relative path and concatenates a string with these new relative paths
   */
  public static List gatherAllDependencies(NamespaceMetaData ns) { 
    def allDeps = []
    log.info("Traversing the Graph up to the top for dependencies")
    allDeps = findDependenciesUpGraph(ns, allDeps)
    log.info("all dependencies are {}", allDeps)
    return allDeps*.episodeName
  }

  /**
   * @param namespaceMetaData an object of type #NamespaceMetaData
   * @return String represenation of the list of included schemas to undergo parsing via xjc, so that no extra schemas are processed
   * Takes a #NamespaceMetaData object and gathers each of it's parseFiles list and turns it into a relative path and concatenates a string with these new relative paths
   */
  private static addToDependencyList (List depList, NamespaceMetaData ns) {
    if(!ListUtil.isAlreadyInList(depList, ns)) { 
      log.info("adding {} to dep List {}", ns, depList)
      depList << ns
    }
    log.info("return dependency list is {}", depList)
    return depList
  }

  /**
   * @param namespaceMetaData an object of type #NamespaceMetaData
   * @return String represenation of the list of included schemas to undergo parsing via xjc, so that no extra schemas are processed
   * Takes a #NamespaceMetaData object and gathers each of it's parseFiles list and turns it into a relative path and concatenates a string with these new relative paths
   */
  private static List findDependenciesUpGraph(NamespaceMetaData xsdData, List depList) {
    log.info("dep list is {}, xsd namespace is {}", depList, xsdData.namespace)

    if (xsdData.importedNamespaces == ["none"]) {
      log.info("target Namespace {} doesn't import any other namespace, no episode binding necessary, need to check external dependencies", xsdData.namespace)
      if (xsdData.externalImportedNamespaces) {
        log.info("going through namespace {} possible external imports {}", xsdData.namespace, xsdData.externalImportedNamespaces)
	xsdData.externalImportedNamespaces.each { external ->
	  log.info("external import {} is trying to be added to the list {}", external.namespace, depList)
	  depList = addToDependencyList(depList, external)
	  external.externalImportedNamespaces.each { externalImports ->
            log.info("external import {} externally imports {} -- trying to add this import to the list {}", external.namespace, externalImports.namespace, depList)
	    depList = addToDependencyList(depList, externalImports)
	  }
	}
      }
      return depList
    }

    log.info("adding the first level imported namespace for {}", xsdData.namespace)
    xsdData.importedNamespaces.each { importedNamespace ->
      log.info("Import namespace is {}, dependency List is {}", importedNamespace, depList)
      depList = addToDependencyList(depList, importedNamespace)
    }

    log.info("checking to see if there are any higher level imported namespace for {}", xsdData.namespace)
    xsdData.importedNamespaces.each { ns ->
      log.info("diving into namespace {} to see if it imports anything else that we will need to bind to", ns.namespace)
      depList = findDependenciesUpGraph(ns, depList)
      log.info("current dependency List {}", depList)
    }

    log.info("going through namespace {} possible external imports {}", xsdData.namespace, xsdData.externalImportedNamespaces)
    xsdData.externalImportedNamespaces.each { external ->
      log.info("external import {} is trying to be added to the list {}", external.namespace, depList)
      depList = addToDependencyList(depList, external)
      external.externalImportedNamespaces.each { externalImports ->
	log.info("external import {} externally imports {} -- trying to add this import to the list {}", external.namespace, externalImports.namespace, depList)
	depList = addToDependencyList(depList, externalImports)
      }
    }

    log.info("current dependency List {}", depList)

    return depList
  }

  
}