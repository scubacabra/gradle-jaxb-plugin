package com.jacobo.gradle.plugins.structures

class XsdNamespaces { 
  List xsdFiles = []
  List fileImports = []
  List fileIncludes = []
  List externalDependencies = []
  String namespace
  
  def addImports(nsImport) { 
    if( !isAlreadyInList(fileImports, nsImport) ) { 
      this.fileImports << nsImport
    }
  }
  
  def addIncludes(nsIncludeFile) { 
    if( !isFileAlreadyInList(fileIncludes, nsIncludeFile) ) { 
      this.fileIncludes << nsIncludeFile
    }
  }

  def addExternalDependency(extDep) { 
    if( !isAlreadyInList(externalDependencies, extDep) ) { 
      this.externalDependencies << extDep
    }
  }

  def boolean isAlreadyInList(List list, String namespace) { 
    return list.contains(namespace)
  }

  def boolean isFileAlreadyInList(List list, File file) { 
    return list.contains(file)
  }
  def isExternalDependency = { collection, ns ->
    if(!collection.find { it.namespace == ns}) { 
      addExternalDependency(ns)
      return true
    }
    return false
  }

  public static String convertNamespaceToEpisodeName(String ns) { 
    def ret = ns.replace("http://", "")
    ret = ret.replace(":", "-")
    ret = ret.replace("/", "-")
  }

  def String toString() { 
    def out = "Namespace: ${namespace} \n"
    out += "files with this namespace:\n ${xsdFiles ?: "none" }\n"
    out += "namespace imports over all the files:\n ${fileImports ?: "none" }\n"
    out += "namespace includes over all the files:\n ${fileIncludes ?: "none" }\n"
    out += "namespace external dependencies over all the files:\n ${externalDependencies ?: "none" }\n"
    return out
  }
}