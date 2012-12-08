package com.jacobo.gradle.plugins

class orderGraph { 
  List orderGraph = []
  
  def String toString() { 
    def out = "The order to parse is:"
    orderGraph.each { 
      out += it
    }
    return out
  }
}