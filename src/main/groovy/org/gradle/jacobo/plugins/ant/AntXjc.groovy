package org.gradle.jacobo.plugins.ant
import org.gradle.api.file.FileCollection
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.jacobo.plugins.extension.XjcExtension

/**
 * Defines and executes the {@code xjc} ant task.
 */
class AntXjc implements AntExecutor {
  static final Logger log = Logging.getLogger(AntXjc.class)

  /**
   * Defines and executes the {@code xjc} ant task.
   * A variable list of arguments is passed in containing data to configure
   * this task.  In order, they are:
   * <ul>
   * <li> {@code extension} => the {@code XjcExtenstion} for task configuration
   * <li> {@code classpath} => string of classpath to set up via this plugins
   *      configuration dependencies
   * <li> {@code xsds} => {@code FileCollection} of xsds to run through this task
   * <li> {@code bindings} => {@code FileCollection} of user defined bindings
   *      for this task
   * <li> {@code episodes} => {@code FileCollection} of episode bindings for
   *      this task
   * <li> {@code episodeFile} => episode File to generate for these xsd's run
   *      through the xjc task
   * </ul>
   *
   * @param ant  ant builder to configure and execute
   * @param arguments  variable arguments to configure this {@code xjc} task
   * @see org.gradle.api.file.FileCollection
   * @see org.gradle.jacobo.plugins.extension.XjcExtension
   */
  public void execute(AntBuilder ant, Object... arguments) {
    XjcExtension extension = arguments[0]
    def classpath = arguments[1]
    def xsds = arguments[2]
    def bindings = arguments[3]
    def episodes = arguments[4]
    def episodeFile = arguments[5]

    log.info("xjc task is being passed these arguments: '{}'", arguments)
    ant.taskdef (name : 'xjc',
		 classname : extension.taskClassname,
		 classpath : classpath)

    ant.xjc(destdir	    : extension.destinationDir,
	    extension	    : extension.extension,
	    removeOldOutput : extension.removeOldOutput,
	    header	    : extension.header,
      package     : extension.generatePackage)
    {
      //TODO maybe should put the produces in there?
      //produces (dir : destinationDirectory)
      xsds.addToAntBuilder(ant, 'schema', FileCollection.AntType.FileSet)
      bindings.addToAntBuilder(ant, 'binding', FileCollection.AntType.FileSet)
      episodes.addToAntBuilder(ant, 'binding', FileCollection.AntType.FileSet)
      arg(line : "-episode $episodeFile")
      for (String val : extension.args) {
        arg(value: val)
      }
      if (log.isDebugEnabled()) {
        arg(value: '-debug')
      }
      if (log.isInfoEnabled()) {
        arg(value: '-verbose')
      }
    }
  }
}
