package com.jacobo.gradle.plugins.util

import groovy.io.FileType

/**
 * File Helper utility class
 * User: djmijares
 * Date: 5/22/13
 * Time: 10:14 AM
 */
class FileHelper {

    /**
     * Figure out the ABSOLUTE schema location of the String relative to the parent/current directory
     * @param schemaLocation is the relative path of the schema location being called in either the xsd:import or xsd:includes call
     * @param parentDir is the parent directory of the schema file that is currently being Xml Slurped
     * @return File absolute File path to schema Location
     */
    public static File getAbsoluteSchemaLocation(String schemaLocation, File parentDir) {
        def relPath = new File(parentDir, schemaLocation)
        return new File(relPath.canonicalPath)
    }

  /**
   * @param directory the directory to search for all *.xsd files
   * Finds all the xsd files in #directory, populates the Namesapce Meta Data #namespaceData
   */
  public static List<File> findAllXsdFiles(String directory) { 
    def schemaList = []
    def baseDirectory = new File(directory)
    baseDirectory.eachFileRecurse(FileType.FILES) {  file -> 
      if(file.name.split("\\.")[-1] == 'xsd') {
	schemaList << file
      }
    }
    return schemaList
  }

  /**
   * @param directory the directory to search for all *.xsd files
   * Finds all the xsd files in #directory, populates the Namesapce Meta Data #namespaceData
   */
  public static List<File> findAllXsdFiles(File directory) { 
    def schemaList = []
    directory.eachFileRecurse(FileType.FILES) {  file -> 
      if(file.name.split("\\.")[-1] == 'xsd') {
	schemaList << file
      }
    }
    return schemaList
  }

}
