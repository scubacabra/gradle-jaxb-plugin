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
     * slurped contents from this xsd from XmlSlurper
     */
    def content

    /**
     * name of the xsd file, absolute
     */
    File document

    /**
     * xsd namespace for this document (targetNamespace value)
     */
    def xsdNamespace

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
    def grabXsdNamespace() {
        log.debug("starting to grab XSD namespace for {}", content)
	xsdNamespace = content.@targetNamespace?.text()
	if (xsdNamespace) {
	  log.warning("There is no targetNamespace attribute for file {} (assigning 'null' to it), it is strongly advised best practice to ALWAYS include a targetNamespace attribute in your <xsd:schema> root element.  no targetNamespaces are referred to using the Chameleon design pattern, which is not advisable!", schemaDoc)
	}
        log.debug("grabbed XSD namespace for {}", content)
    }

    /**
     * grabs the dependencies of this xsd document being slurped, slurps import statements and include statements
     * @param xsd the xml slurped document to grab data from
     */
    def grabXsdDependencies() {
        log.debug("starting to grab XSD dependencies for {}", content)
        grabXsdIncludedDependencies(content)
        grabXsdImportedDependencies(content)
        log.debug("grabbed all XSD dependencies for {}", content)
    }

    /**
     * Slurp xsd import statements
     * @param xsd the xml slurped document to grab data from
     */
    def grabXsdImportedDependencies() {
        log.debug("resolving xsd 'imported' dependencies for {}", content)
        processXsdDependencyLocations(content?.import)
        log.debug("resolved all xsd 'imported' dependencies for {}", content)
    }

    /**
     * slurp xsd includes statements
     * @param xsd the xml slurped document to grab data from
     */
    def grabXsdIncludedDependencies() {
        log.debug("resolving xsd 'include' dependencies for {}", content)
        processXsdDependencyLocations(content?.include)
        log.debug("resolved all xsd 'include' dependencies for {}", content)
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