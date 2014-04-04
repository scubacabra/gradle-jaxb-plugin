package org.gradle.jacobo.plugins.task

import org.gradle.jacobo.plugins.DependencyTreeSpecification
import org.gradle.jacobo.plugins.model.TreeManager

class ParseNodesSpec extends DependencyTreeSpecification {

  def treeManager = new TreeManager()
  def task = GroovyMock(JaxbXJCTask)

  // Just copying exactly what the task does in the JaxbXJCTask in parseNodes().
  // Tried to mock that class, but then everything was mocked.  This was my cheap workaround.
  // I just wanted to make sure that from this tree, you would only parse 8 times.
  def "parse Nodes from tree Mangager basenamespaces"() { 
    when:
    def nextRow = [node1, node2] as Set
    while(nextRow) {
      nextRow.each { node ->
	task.parseNode(node)
      }
      nextRow = treeManager.nextNodeRow(nextRow)
    }   

    then:
    8 * task.parseNode(_)
  }
}