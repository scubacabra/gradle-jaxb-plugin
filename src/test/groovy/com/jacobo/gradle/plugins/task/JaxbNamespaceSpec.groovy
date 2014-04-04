package com.jacobo.gradle.plugins.task

import org.gradle.jacobo.schema.XsdDocument
import org.gradle.jacobo.schema.slurper.XsdSlurper
import org.gradle.jacobo.schema.slurper.DocumentSlurper
import org.gradle.jacobo.schema.resolver.DocumentResolver

import com.jacobo.gradle.plugins.ProjectTaskSpecification
import com.jacobo.gradle.plugins.JaxbPlugin
import com.jacobo.gradle.plugins.structures.NamespaceData

class JaxbNamespaceSpec extends ProjectTaskSpecification {

  def xsdSlurper = Mock(XsdSlurper)
  def documentSlurper = Mock(DocumentSlurper)
  def documentResolver = Mock(DocumentResolver)
  def mockSlurper = new XmlSlurper().parseText("<xsd></xsd>")

  def setup() {
    task = project.tasks[JaxbPlugin.JAXB_NAMESPACE_GRAPH_TASK] as JaxbNamespaceTask
  }

  def "Get all XSD Files in the directory passed as a File (absolute)"() { 
    when:
    task.with { 
      xsdDirectory = directory
    }

    def files = task.findAllXsdFiles(directory)
  
    then:
    files.size == 3
    files.each { file -> xsdFiles.contains(file) == true }
  
    where:
    directory = new File("build/resources/test/schema/House").absoluteFile
    xsdFiles = [new File("build/resources/test/schema/House/Kitchen.xsd"),
		new File("build/resources/test/schema/House/KitchenSubset.xsd"),
		new File("build/resources/test/schema/House/LivingRoom.xsd")
	       ]
  }

  def "Group slurpers according to their namespaces"() { 
    when:
    task.with { 
      xsdDirectory = directory
    }

    def slurpedUp = []
    xsdFiles.each { k,v ->
      v.each { file ->
	def document = new XsdDocument(documentSlurper, documentResolver,
				       xsdSlurper, file, mockSlurper)
	document.xsdNamespace = k
	slurpedUp << document
      }
    }

    def groupedByNamespace = task.groupSlurpedDocumentsByNamespace(slurpedUp)

    then:
    groupedByNamespace.size() == 2
    namespaces.each { groupedByNamespace.containsKey(it) == true }
    namespaces.each { namespace ->
      groupedByNamespace[namespace].size == xsdFiles[namespace].size
      groupedByNamespace[namespace].documentFile.each {
	xsdFiles[namespace].contains(it) }
    }

    where:
    directory = getFileFromResourcePath("/schema/House")
    xsdFiles = ["http://www.example.org/Kitchen":
		["/schema/House/Kitchen.xsd",
		 "/schema/House/KitchenSubset.xsd"].collect{
		  getFileFromResourcePath(it) },
		"http://www.example.org/LivingRoom":
		["/schema/House/LivingRoom.xsd"].collect{
		  getFileFromResourcePath(it) }]
    namespaces = ["http://www.example.org/Kitchen",
		  "http://www.example.org/LivingRoom"]
  }

  def "Group Namespace Objects from namespace map"() { 
    when:
    task.with { 
      xsdDirectory = directory
    }

    def groupedMap = [:]
    xsdFiles.each { k,v ->
      def slurpedUp = []
      v.each { file ->
	def document = new XsdDocument(documentSlurper, documentResolver,
				       xsdSlurper, file, mockSlurper)
	document.xsdNamespace = k
	slurpedUp << document
      }
      groupedMap[k] = slurpedUp
    }

    def groupedNamespaces = task.groupNamespaces(groupedMap)

    then:
    groupedNamespaces.each{ it instanceof NamespaceData }
    groupedNamespaces.size() == 2
    groupedNamespaces.namespace.each { namespaces.contains(it) == true }
    namespaces.each { namespace ->
      def groupedNamespace = groupedNamespaces.find{ it.namespace == namespace }
      groupedNamespace.slurpedDocuments.size == xsdFiles[namespace].size
      xsdFiles[namespace].each{ groupedNamespace.slurpedDocuments.contains(it) }
    }

    where:
    directory = getFileFromResourcePath("/schema/House")
    xsdFiles = ["http://www.example.org/Kitchen":
		["/schema/House/Kitchen.xsd",
		 "/schema/House/KitchenSubset.xsd"].collect{
		  getFileFromResourcePath(it) },
		"http://www.example.org/LivingRoom":
		["/schema/House/LivingRoom.xsd"].collect{
		  getFileFromResourcePath(it) }]
    namespaces = ["http://www.example.org/Kitchen",
		  "http://www.example.org/LivingRoom"]
  }
}
