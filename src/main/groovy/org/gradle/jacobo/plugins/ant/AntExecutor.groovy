package org.gradle.jacobo.plugins.ant

import groovy.util.AntBuilder

/**
 *  Abstracts ant tasks defined in plugin, so that unit and integration
 *  testing is accomplished more easily through Mocking.
 *  <p>
 *  Previously, an ant task was defined in the a gradle {@code *Task.groovy}
 *  class, and testing was a pain.  This delegates to the same ant builder, but
 *  allows a variable number of arguments to be passed into it's execution.
 */
interface AntExecutor {
  
  /**
   * Executes an Ant task via {@code AntBuilder}
   * @param ant  {@code AntBuilder} to configure and execute
   * @param arguments  variable parameters to configure the {@code AntBuilder}
   */
  public void execute(AntBuilder ant, Object... arguments)
}