package org.gradle.jacobo.plugins.task

import org.gradle.jacobo.plugins.ProjectTaskSpecification
import org.gradle.jacobo.plugins.JaxbPlugin
import org.gradle.jacobo.plugins.model.TreeManager
import org.gradle.jacobo.plugins.model.TreeNode
import org.gradle.jacobo.plugins.structures.NamespaceData

import spock.lang.*

class JaxbXJCSpec extends ProjectTaskSpecification {
  
  def treeManager = GroovyMock(TreeManager)
  @Shared
  def episodes = ["ns1.episode", "ns2.episode", "ns3.episode", "ns10.episode",
		  "ns11.episode", "ns12.episode", "ns13.episode",
		  "ns14.episode", "ns15.episode", "ns16.episode"
		 ]
  def setup() {
    task = project.tasks[JaxbPlugin.JAXB_NAMESPACE_GENERATE_TASK] as JaxbXJCTask
    task.with { 
      manager = treeManager
      episodeDirectory = getFileFromResourcePath("/episodes")
    }
  }

  // Make episode files in the episode directory so the episode Check doesn't
  // throw errors
  def setupSpec() {
    def resourcesRoot = getFileFromResourcePath("/")
    def episodeFolder = new File(resourcesRoot, "episodes")
    episodeFolder.mkdir()
    episodes.each { episode ->
      def file = new File(episodeFolder, episode)
      file.createNewFile()
    }
  }

  // cleanup episode files and episode folder created
  def cleanupSpec() {
    def episodeFolder = getFileFromResourcePath("/episodes")
    episodes.each { episode ->
      def file = new File(episodeFolder, episode)
      file.delete()
    }
    episodeFolder.delete()
  }

  @Unroll
  def "files '#xsdFiles' to string list conversion '#fileString' for input to xjc task" () {
    when:
    def result = task.xsdFilesListToString(xsdFiles)

    then:
    result == fileString

    where:
    xsdFiles << [[new File("Danny.xsd"), new File("Johnny.xsd"),
		  new File("OBrien.xsd")],
		 [new File("Marge.xsd"), new File("Clay.xsd"),
		  new File("Homer.xsd"), new File("Ethan.xsd")],
		 [new File("/everything/bigger/Texas.xsd"),
		  new File("/biggest/sky/Montana.xsd"),
		  new File("/going/to/sink/California.xsd")]
		]
    fileString << ["Danny.xsd Johnny.xsd OBrien.xsd",
		   "Marge.xsd Clay.xsd Homer.xsd Ethan.xsd",
		   "Texas.xsd Montana.xsd California.xsd"
		  ]
  }

  @Unroll("namespace #ns -> episode file name #episode (w/out extension)")
  def "targetNamespace to episode file name" () {
    when:
    def result = task.convertNamespaceToEpisodeName(ns)

    then:
    result == episode

    where:
    ns                              | episode
    "http://fake.com/donuts/glazed" | "fake.com-donuts-glazed"
    "urn:real/boy/pinnochio"        | "urn-real-boy-pinnochio"
    "tickle/me/elmo"                | "tickle-me-elmo"
    "Alert.xsd"                     | "Alert.xsd"  // when file name is namespace because no namespace present
  }

  @Unroll
  def "list of custom bindings '#bindings' converted to XJC task input '#xjcBindings'" () {
  when:
  def result = task.transformBindingListToString(bindings)

  then:
  result == xjcBindings
  result.isEmpty() == isEmpty

  where:
  bindings << [["binding1.xjc", "binding2.xjc", "binding3.xjc"],
	       ["my_binding.xjc", "your_binding.xjc", "we_all_bind.xjc"],
	       []
	      ]
  xjcBindings << ["binding1.xjc binding2.xjc binding3.xjc",
		  "my_binding.xjc your_binding.xjc we_all_bind.xjc", ""
		 ]
  isEmpty << [false, false, true]
  }

  @Unroll
  def "resolve Dependencies for '#namespace' with parents '#parents' into -> '#episodefilenames', no external dependencies at ALL"() { 
    given:
    treeManager.getParents(_) >> parents.collect{ parent -> new TreeNode(parent) }
    
    and:
    def node = new TreeNode(new NamespaceData(namespace))
    
    when:
    def result = task.resolveEpisodeFiles(node, treeManager)

    then:
    result.size() == episodefilenames.size()
    result == episodefilenames

    where:
    namespace << ["ns3", "ns4", "ns1"]
    episodefilenames << [["ns1", "ns2"], ["ns1", "ns2", "ns3"], []]
    parents << [[new NamespaceData("ns1"), new NamespaceData("ns2")],
		[new NamespaceData("ns1"), new NamespaceData("ns2"),
		 new NamespaceData("ns3")
		], []
	       ]
  }

  @Unroll
  def "check episode Files existence, 'ns5.episode' not present, will throw error"() {
    given:
    def episodeNames = ["ns1", "ns2", "ns5"]// ns5 not in episode folder error

    when:
    checkEpisodeFilesExist(episodeNames, "ns3")

    then:
    thrown RuntimeException
  }

  @Unroll
  def "resolve Dependencies for '#namespace' with parents '#parents' into -> '#episodefilenames', node has external dependencies, parents have no external dependencies"() { 
    given:
    treeManager.getParents(_) >> parents.collect{ parent -> new TreeNode(parent) }
    
    and:
    def node = new TreeNode(
      new NamespaceData(namespace: namespace,
			dependentExternalNamespaces: nodeExternalDeps,
			hasExternalDependencies: true))
    
    when:
    def result = task.resolveEpisodeFiles(node, treeManager)

    then:
    result.size() == episodefilenames.size()
    result.each { episodefilenames.contains(it) }

    where:
    namespace << ["ns3", "ns3", "ns4", "ns1"]
    nodeExternalDeps << [["ns10"], ["ns10", "ns11"], ["ns10"], ["ns10", "ns11"]]
    episodefilenames << [["ns10"], ["ns10", "ns11"], ["ns1", "ns2", "ns10"],
			 ["ns1", "ns2", "ns3", "ns10", "ns11"]
			]
    parents << [[], [],
		[new NamespaceData("ns1"), new NamespaceData("ns2")],
		[new NamespaceData("ns1"), new NamespaceData("ns2"),
		 new NamespaceData("ns3")
		]
	       ]
  }

  @Unroll
  def "resolve Dependencies for '#namespace' with parents '#parents' into -> '#episodefilenames', EVERYTHING has external dependencies"() {
    setup:
    treeManager.getParents(_) >> parents.collect{ parent -> new TreeNode(parent) }
    
    and:
    def node = new TreeNode(
      new NamespaceData(namespace: namespace,
			dependentExternalNamespaces: nodeExternalDeps,
			hasExternalDependencies: true))
    
    when:
    def result = task.resolveEpisodeFiles(node, treeManager)

    then:
    result.size() == episodefilenames.size()
    result.each { episodefilenames.contains(it) }

    where:
    namespace << ["ns3", "ns3", "ns4", "ns3"]
    nodeExternalDeps << [["ns10"], ["ns10", "ns11"], ["ns10"], ["ns10", "ns11"]]
    episodefilenames << [["ns10", "ns1", "ns2", "ns14", "ns15"],
			 ["ns10", "ns11", "ns1", "ns13", "ns14"],
			 ["ns10", "ns2", "ns14", "ns15"],
			 ["ns1", "ns2", "ns3", "ns10", "ns11", "ns12", "ns13",
			  "ns14", "ns15", "ns16"]
			]
    parents << [[new NamespaceData(namespace: "ns1",
				   dependentExternalNamespaces: ["ns10"],
				   hasExternalDependencies: true),
		new NamespaceData(namespace: "ns2",
				  dependentExternalNamespaces: ["ns14", "ns15"],
				  hasExternalDependencies: true)],
		[new NamespaceData(namespace: "ns1",
				   dependentExternalNamespaces: ["ns11", "ns13", "ns14"],
				   hasExternalDependencies: true)],
		[new NamespaceData(namespace: "ns2",
				   dependentExternalNamespaces: ["ns14", "ns15"],
				   hasExternalDependencies: true)],
		[new NamespaceData(namespace: "ns1",
				   dependentExternalNamespaces: ["ns12", "ns13"],
				   hasExternalDependencies: true),
		 new NamespaceData(namespace: "ns2",
				   dependentExternalNamespaces: ["ns14", "ns15"],
				   hasExternalDependencies: true),
		 new NamespaceData(namespace: "ns3",
				   dependentExternalNamespaces: ["ns12", "ns13", "ns14", "ns15", "ns16"],
				   hasExternalDependencies: true)]
		]
  }
}