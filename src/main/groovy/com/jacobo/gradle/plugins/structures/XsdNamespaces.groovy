package com.jacobo.gradle.plugins.structures

class XsdNamespaces { 
  List xsdFiles = []
  List fileImports = []
  List fileIncludes = []
  def externalDependencies = [:]
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

  def addExternalDependency(String extDep, File file) { 
    if(externalDependencies.containsKey(extDep)) { 
      if( !isFileAlreadyInList(externalDependencies[extDep], file) ) { 
	externalDependencies[extDep] << file
      }
    } else { 
      externalDependencies[extDep] = [file]
    }
  }

  def boolean isAlreadyInList(List list, String namespace) { 
    return list.contains(namespace)
  }

  def boolean isFileAlreadyInList(List list, File file) { 
    return list.contains(file)
  }

  def isExternalDependency = { collection, ns ->
    if(!collection.find { it.namespace == ns } ) { //this namespace not in the collection of namespaces available, is external
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