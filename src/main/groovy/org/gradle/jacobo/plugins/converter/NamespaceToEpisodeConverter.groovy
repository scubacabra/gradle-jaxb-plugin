package org.gradle.jacobo.plugins.converter

import org.gradle.api.logging.Logging
import org.gradle.api.logging.Logger

import org.gradle.jacobo.plugins.converter.NameConverter

class NamespaceToEpisodeConverter implements NameConverter {
  static final Logger log = Logging.getLogger(NamespaceToEpisodeConverter.class)
  
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