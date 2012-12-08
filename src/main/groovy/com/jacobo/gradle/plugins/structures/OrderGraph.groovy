package com.jacobo.gradle.plugins.structures

class OrderGraph { 
  List orderGraph = []
  List dependentNamespaces = []

  //  def isAlreadyInList = nsCollection[0].&isAlreadyInList
  def boolean isAlreadyInList(List list, String namespace) { 
    return list.contains(namespace)
  }

  def addToDependencyLevel = { list, ns ->
    if(list) { 
      if(!isAlreadyInList(list, ns)) { 
	list << ns
      } 
    } else { 
      list = [ns]
    }
    return list
  }

  def String toString() { 
    def out = "The order to parse is:"
    orderGraph.each { 
      out += it
    }
    return out
  }
}