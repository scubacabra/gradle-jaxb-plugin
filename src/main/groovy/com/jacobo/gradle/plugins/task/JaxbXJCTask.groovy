package com.jacobo.gradle.plugins.task

import org.gradle.api.logging.Logging
import org.gradle.api.logging.Logger
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.DefaultTask
import org.gradle.api.InvalidUserDataException

import com.jacobo.gradle.plugins.JaxbPlugin
import com.jacobo.gradle.plugins.model.TreeManager
import com.jacobo.gradle.plugins.model.TreeNode
import com.jacobo.gradle.plugins.structures.NamespaceData

/**
 * @author jacobono
 * Created: Tue Dec 04 09:01:34 EST 2012
 */

class JaxbXJCTask extends DefaultTask {
  static final Logger log = Logging.getLogger(JaxbXJCTask.class)

  @Input
  TreeManager manager

  /**
   * If an xsd namespace has dependencies, references episode files from
   * this directory. Must write own episode file to this directory
   */
  @OutputDirectory
  File episodeDirectory

  /**
   * User can create custom bindings, they would be in this directory
   */
  @OutputDirectory
  File customBindingDirectory

  /**
   * directory where the generated java files from xjc would go
   * Usually <pre><project-root>/src/main/java</pre>
   */
  @OutputDirectory
  File generatedFilesDirectory

  @TaskAction
  void start() {
    def treeManager = getManager()
    parseNodes(treeManager.currentNodeRow)
  }

  def parseNodes(baseTreeNodes) {
    def nextRow = baseTreeNodes as Set
    while(nextRow) {
      nextRow.each { node -> parseNode(node) }
      nextRow = manager.nextNodeRow(nextRow)
    }
  }

  def parseNode(TreeNode<NamespaceData> node) {
    def episodeBindings = this.resolveEpisodeFiles(node)
    def episodeName = this.convertNamespaceToEpisodeName(node.data.namespace)
    // def episodePath = project.jaxb.jaxbEpisodeDirectory +
    //   "/${episodeName}.episode"
    def xsdFiles = node.data.filesToParse()
    def parseFiles = this.xsdFilesListToString(xsdFiles)
    def bindings = this.transformBindingListToString(project.jaxb.bindingIncludes)

    sanityChecks(episodeBindings, schemaIncludes, ns)

    xjc(project.jaxb.jaxbSchemaDestinationDirectory, project.jaxb.extension,
	project.jaxb.removeOldOutput, project.jaxb.header,
	project.jaxb.xsdDirectoryForGraph, parseFiles,
	project.jaxb.bindingIncludes, project.jaxb.jaxbBindingDirectory,
	bindings, episodeBindings, project.jaxb.jaxbEpisodeDirectory,
	episodeName)
  }

  def xjc(destinationDirectory, extension, removeOldOutput, header,
	  schemasDirectory, xsdFiles, customBinding, bindingDirectory,
	  bindings, episodeBindings, episodesDirectory, episodeName) {
    ant.taskdef (name : 'xjc', 
		 classname : 'com.sun.tools.xjc.XJCTask',
		 classpath : project.configurations[JaxbPlugin.JAXB_CONFIGURATION_NAME].asPath
		)

    ant.xjc(destdir : destinationDirectory,
	    extension : extension,
	    removeOldOutput : removeOldOutput,
	    header : header)
    {
      //TODO maybe should put the produces in there?
      //produces (dir : destinationDirectory)
      schema (dir : schemasDirectory,
	      includes : xsdFiles )
      if (!customBinding.isEmpty() && customBinding != null) {
	binding(dir : bindingDirectory, includes : bindings)
      }
      episodeBindings.each { episode ->
	log.info("binding with file {}.episode", episode)
	binding (dir : episodesDirectory,
		 includes : "${episode}.episode")
      }
      log.info("episode file is {}", episodeName)
      arg(value : '-episode')
      arg(value: episodeName)
      arg(value : '-verbose')
      arg(value : '-npa')
    }
  }

  def sanityChecks(episodeBindingsNames, schemaIncludes, ns) { 
    // needs to have include files
    if(schemaIncludes == "" ||
       schemaIncludes.isEmpty())
    throw new RuntimeException(
      "There are no files to include in the parsing in " +
      ns.parseFiles + "for namespace " + ns.namespace)

    // default directory schema destination dir is relative to the project
    // .projectDir not the rootDir like the rest of these
    directoryExists(project.projectDir.path + File.separator +
		    project.jaxb.jaxbSchemaDestinationDirectory)

    // these Directories need to exist
    [project.jaxb.jaxbSchemaDirectory,
     project.jaxb.jaxbEpisodeDirectory].each directoryExists

    // if we have includes, then check for this binding directory existing
    // if not don't check
    if (!project.jaxb.bindingIncludes.isEmpty() &&
	project.jaxb.bindingIncludes != null) {
      directoryExists(project.jaxb.jaxbBindingDirectory)
    }

    // all episode file bindings MUST exist
    episodeBindingsNames.each { 
      def episode = it + ".episode"
      def file = new File(project.jaxb.jaxbEpisodeDirectory, episode)
      if(!file.exists())
	throw new RuntimeException(
	  "${file} does not exist, there is most likely a null " + 
	  "Namespace data somewhere")
    }
  }

  def directoryExists = { dir ->
    def directory = new File(dir)
    if(!directory.isDirectory() &&
       !directory.exists())
    throw new InvalidUserDataException(
      " ${directory} is not an existing directory, make this directory " + 
      "or adjust the extensions to point to a proper directory")
  }

  /**
   * @param Tree Node with NamespaceData as a data object
   * @return List of episode names to bind with.
   * Based off of the current Node looking to parse.  Node's parents
   * form the core episode bindings, but the node, along with each
   * one of its parents can depend on external namespaces, and these
   * can be duplicated, so a Set of dependentNamespaces is constructed
   * and then each namespace is converted to it episode file name.
   */
  def resolveEpisodeFiles(TreeNode<NamespaceData> node) { 
    def dependentNamespaces = [] as Set
    // must check for current nodes external dependencies.  Could have 0
    // parents and only possible external dependencies lay in this node
    if (node.data.hasExternalDependencies) {
      dependentNamespaces.addAll(node.data.dependentExternalNamespaces)
    }

    def parents = manager.getParents(node)
    // returns empty set if no parents found
    if (parents) {
      dependentNamespaces.addAll(parents.data.namespace)
      // check if any parent has external dependencies
      parents.each { parent ->
	if (parent.data.hasExternalDependencies) {
	  dependentNamespaces.addAll(parent.data.dependentExternalNamespaces)
	}
      }
    }

    def episodeBindings = dependentNamespaces.collect { dependentNamespace ->
      this.convertNamespaceToEpisodeName(dependentNamespace)
    }

    return episodeBindings
  }

  /**
   * @param List of File objects
   * @return String fileNames -  the list of included schemas to undergo
   * parsing via xjc. File name ONLY, space separated.
  */
  public String xsdFilesListToString(List<File> filesToParse) {
    String fileNames = filesToParse.name.join(" ")
    log.debug("List of files to Parse for xjc is '{}'", fileNames)
    return fileNames
  }


  /**
   * @param bindings a list of user defined bindings
   * @return String binding list
   * List transformed into a string so the xjc task can accordingly
   * process it Takes a binding list (user defined) and turns it into an
   * appropriate string for the xjc ant task to use
   */
  public String transformBindingListToString(List bindings) { 
    String bindingNames = bindings.join(" ")
    log.debug("List of custom bindings for xjc is '{}'", bindingNames)
    return bindingNames
  }

  /**
   * Method that converts the namespace into an appropriate episode file
   * name that the file system accepts
   * TODO: conventions could be better
   */
  public convertNamespaceToEpisodeName(String namespace) { 
    def episodeName = namespace.replace("http://", "")
    episodeName = episodeName.replace(":", "-")
    episodeName = episodeName.replace("/", "-")
    return  episodeName
  }
}