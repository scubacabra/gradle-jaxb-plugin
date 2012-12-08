package com.jacobo.gradle.plugins

class XsdNamespaces { 
  List xsdFiles = []
  List fileImports = []
  List fileIncludes = []
  List externalDependencies = []
  String namespace
  
  // public xsdNamespaces(namespace, files) { 
  //   this.namespace = namespace
  //   this.xsdFiles = files
  // }

  def addImports(nsImport) { 
    if( !isAlreadyInList(fileImports, nsImport) ) { 
      this.fileImports << nsImport
    }
  }
  
  def addInclude(nsInclude) { 
    if( !isAlreadyInList(fileIncludes, nsInclude) ) { 
      this.fileIncludes << nsInclude
    }
  }

  def addExternalDependency(extDep) { 
    if( !isAlreadyInList(fileIncludes, extDep) ) { 
      this.fileIncludes << extDep
    }
  }

  def boolean isAlreadyInList(List list, String namespace) { 
    return list.contains(namespace)
  }

  def isExternalDependency = { collection, ns ->
    if(!collection.find { it.namespace == ns}) { 
      externalDependencies << ns
      return true
    }
    return false
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