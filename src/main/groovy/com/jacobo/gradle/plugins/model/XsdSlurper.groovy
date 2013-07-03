package com.jacobo.gradle.plugins.model

import com.jacobo.gradle.plugins.util.ListUtil

import org.gradle.api.logging.Logging
import org.gradle.api.logging.Logger

/**
 *  Actual slurper logic for XSD processing.
 * @author djmijares
 */
class XsdSlurper {
    private static final Logger log = Logging.getLogger(XsdSlurper.class)

    /**
     * name of the slurped document (file)
     */
    def documentName

    /**
     * The current dir the slurped document is in
     */
    File currentDir

    /**
     * xsd import locations (relative to @see #currentDir)
     */
    def xsdImports = []

    /**
     * xsd includes locations (relative to @see #currentDir)
     */
    def xsdIncludes = []

    /**
     * grabs the dependencies of this xsd document being slurped, slurps import statements and include statements
     * @param xsd the xml slurped document to grab data from
     */
    def grabXsdDependencies(xsd) {
        log.debug("starting to grab XSD dependencies for {}", xsd)
        grabXsdIncludedDependencies(xsd)
        grabXsdImportedDependencies(xsd)
        log.debug("grabbed all XSD dependencies for {}", xsd)
    }

    /**
     * Slurp xsd import statements
     * @param xsd the xml slurped document to grab data from
     */
    def grabXsdImportedDependencies(xsd) {
        log.debug("resolving xsd 'imported' dependencies for {}", xsd)
        processXsdDependencyLocations(xsd?.import)
        log.debug("resolved all xsd 'imported' dependencies for {}", xsd)
    }

    /**
     * slurp xsd includes statements
     * @param xsd the xml slurped document to grab data from
     */
    def grabXsdIncludedDependencies(xsd) {
        log.debug("resolving xsd 'include' dependencies for {}", xsd)
        processXsdDependencyLocations(xsd?.include)
        log.debug("resolved all xsd 'include' dependencies for {}", xsd)
    }

    /**
     * adds the schema location of the import/include statement to the appropriate list (relative path)
     * @param xsdSlurperElement is the array of elements taken from the @see #XmlSlurper for the given element
     * @see #xsdImports or @see #xsdIncludes
     */
    def processXsdDependencyLocations = { xsdSlurperElement ->
        xsdSlurperElement?.each { xsdElement ->
            def dependencyType = xsdElement.name()
            log.debug("the slurper element type is of {}", dependencyType)
            def dependentSchemaLocation = xsdElement.@schemaLocation.text()
            log.debug("the dependeny schema location is at {}", dependentSchemaLocation)
            if (dependencyType == "import") { //either going to be import or include
                ListUtil.addElementToList(xsdImports, dependentSchemaLocation)
            } else {
                ListUtil.addElementToList(xsdIncludes, dependentSchemaLocation)
            }
        }
    }

    /**
     * Gathers all relative locations belonging to this instance and packages up into one list
     * @return List of all locations from fields #xsdImports and #xsdIncludes
     */
    def gatherAllRelativeLocations() {
        def returnList = []
        returnList.addAll(xsdImports)
        returnList.addAll(xsdIncludes)
        return returnList
    }
}