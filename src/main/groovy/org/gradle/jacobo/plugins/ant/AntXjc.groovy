package org.gradle.jacobo.plugins.ant

import org.gradle.api.file.FileCollection
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

import org.gradle.jacobo.plugins.ant.AntExecutor

class AntXjc implements AntExecutor {
  static final Logger log = Logging.getLogger(AntXjc.class)

  // public void execute(AntBuilder ant, Map<String, Object> arguments) {
  public void execute(AntBuilder ant, Object... arguments) {
    def extension = arguments[0]
    def classpath = arguments[1]
    def xsds = arguments[2]
    def bindings = arguments[3]
    def episodes = arguments[4]
    def episodeFile = arguments[5]

    log.info("xjc task is being passed these arguments: '{}'", arguments)
    ant.taskdef (name : 'xjc',
		 classname : 'com.sun.tools.xjc.XJCTask',
		 classpath : classpath)

    ant.xjc(destdir	    : extension.destinationDir,
	    extension	    : extension.extension,
	    removeOldOutput : extension.removeOldOutput,
	    header	    : extension.header)
    {
      //TODO maybe should put the produces in there?
      //produces (dir : destinationDirectory)
      xsds.addToAntBuilder(ant, 'schema', FileCollection.AntType.FileSet)
      bindings.addToAntBuilder(ant, 'binding', FileCollection.AntType.FileSet)
      episodes.addToAntBuilder(ant, 'binding', FileCollection.AntType.FileSet)
      arg(line : "-episode $episodeFile")
      // TODO add fields for verbosity or does gradle handle it with --debug?
      // arg(value : '-verbose')
      // arg(value : '-npa')
    }
  }
}
