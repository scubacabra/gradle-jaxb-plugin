package org.gradle.jacobo.plugins.task

import org.gradle.jacobo.plugins.ProjectIntegrationSpec

class JaxbDependencyTreeSpec extends ProjectIntegrationSpec {

  def setup() {
    def rootDir = getFileFromResourcePath("/test-jaxb-plugin")
    setRootProject(rootDir)
    setSubProject(rootProject, "multi-parent-child-schema", "com.github.jacobono.jaxb")
    setupProjectTasks()
  }  

  def "run task to generate dependency tree"() {
    given:
    def episodePrefix = "http://www.test.plugin/"

    when:
    dependencyTreeTask.execute()

    then:
    ["xsd1":["xsd3", "xsd4"], "xsd2":["xsd4", "xsd5"], "xsd3":["xsd6", "xsd7"],
     "xsd4":["xsd8"], "xsd5":["xsd8", "xsd9"]].each { k,v ->
      def node = project.jaxb.dependencyGraph.managedNodes.find {
	it.data.namespace ==  episodePrefix + k
      }
      def chitlins = project.jaxb.dependencyGraph.managedNodes.findAll {
	v.collect { episodePrefix + it }.contains(it.data.namespace)
      }
      with(node) {
	node.children.size() == chitlins.size()
	node.children.containsAll(chitlins)
      }
    }
    ["xsd3":["xsd1"], "xsd4":["xsd1", "xsd2"], "xsd5":["xsd2"],
     "xsd6":["xsd3"], "xsd7":["xsd3"], "xsd8":["xsd4", "xsd5"],
     "xsd9":["xsd5"]].each { k,v ->
      def node = project.jaxb.dependencyGraph.managedNodes.find {
	it.data.namespace ==  episodePrefix + k
      }
      def parents = project.jaxb.dependencyGraph.managedNodes.findAll { 
	v.collect { episodePrefix + it }.contains(it.data.namespace)
      }
      with(node) {
	node.parents.size() == parents.size()
	node.parents.containsAll(parents)
      }
    }
  }
}