package org.gradle.jacobo.plugins.model

import org.gradle.jacobo.plugins.DependencyTreeSpecification

class TreeAncestorSpec extends DependencyTreeSpecification {
  def manager = new TreeManager()

  def "get parents of top of the tree"() { 
    when:
    def parents = manager.getParents(node2)

    then:
    parents.size() == 0
    parents.isEmpty() == true
  }

  def "get parents of first row node, one parent only"() { 
    when:
    def parents = manager.getParents(node3)

    then:
    parents.size() == 1
    parents == [node1] as Set
  }

  def "get parents of first row node, this node has two direct parents"() { 
    when:
    def parents = manager.getParents(node4)

    then:
    parents.size() == 2
    parents == [node1, node2] as Set
  }

  def "get parents of last row node, this node has 1 direct parent"() { 
    when:
    def parents = manager.getParents(node6)

    then:
    parents.size() == 2
    parents == [node1, node3] as Set
  }

  def "get parents of last row node, this node has 1 direct parent, 2 direct grandparents"() { 
    when:
    def parents = manager.getParents(node7)

    then:
    parents.size() == 3
    parents == [node1, node2, node4] as Set
  }
}