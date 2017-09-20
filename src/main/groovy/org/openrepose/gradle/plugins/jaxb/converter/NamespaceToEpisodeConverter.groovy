package org.openrepose.gradle.plugins.jaxb.converter

import org.gradle.api.logging.Logging
import org.gradle.api.logging.Logger

/**
 * Converts a namespace to an episode file.
 * Defines certain conventions to remove certain characters from the URL
 * that wouldn't save to the filesystem (and to make easier to identify).
 */
class NamespaceToEpisodeConverter implements NameConverter {
  static final Logger log = Logging.getLogger(NamespaceToEpisodeConverter.class)
  
  /**
   * Converts a namespace to an episode file.
   * Removes characters {@code :} and {@code /}, replacing with {@code -}.
   * Removes leading {@code http://} completely.
   *
   * @param namespace  the namespace to convert
   * @return  converted episode file
   */
  @Override
  public String convert(String namespace) {
    def episodeName = namespace.replace("http://", "")
    episodeName = episodeName.replace(":", "-")
    episodeName = episodeName.replace("/", "-") + ".episode"
    log.info("converted namespace '{}' to episode name '{}'",
	     namespace, episodeName)
    return episodeName
  }
}