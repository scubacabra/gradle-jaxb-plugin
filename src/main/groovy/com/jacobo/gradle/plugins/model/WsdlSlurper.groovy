package com.jacobo.gradle.plugins.model

import org.gradle.api.logging.Logging
import org.gradle.api.logging.Logger

/**
 *  Slurper for WSDL document processing.  May need to slurp XSD statemenets
 *  embedded in WSDL document, extends #XsdSlurper for this reason
 */
class WsdlSlurper extends XsdSlurper {
    private static final Logger log = Logging.getLogger(WsdlSlurper.class)

    /**
     * Set of imports representing this WSDL (Document) depending on another WSDL (Document)
     * Not representing any XSD (Document) dependencies
     */
    def wsdlImports = [] as Set

    /**
     * grabs the dependencies of this Objects slurped document
     * slurps import statements and include statements
     * grabs wsdl XSD dependencies (embedded in WSDL) located at
     * <pre>
     *      <wsdl>
     *          <types>
     *              <schema>
     *                  <xsd:include/>
     *                  <xsd:import/>
     *              </schema>
     *          </types>
     *      </wsdl>
     * </pre>
     * return none
     */
    @Override
    public void resolveDocumentDependencies() {
        log.debug("Getting WSDL Import  dependencies for '{}'", this.documentFile.name)
	slurpDependencies(this.slurpedDocument?.import, this.wsdlImports)
	super.slurpDependencies(this.slurpedDocument?.types?.schema?.import, this.xsdImports)
	super.slurpDependencies(this.slurpedDocument?.types?.schema?.include, this.xsdIncludes)
	this.resolveRelativePathDependencies([this.wsdlImports, this.xsdImports, this.xsdIncludes])
    }

    /**
     * dependentElements is the slurped elements needed, contains schemaLocation for either import/include data
     * collection is the collection to put the paths in -- all of are Strings (that's what schema Location is)
     **/
    @Override
    def slurpDependencies(dependentElements, elementCollection) { 
      log.debug("Slurping Dependencies for '{}' elements of the '{}' type", dependentElements.size, dependentElements[0].name())
      dependentElements?.each { wsdlImportElement ->
	elementCollection.add(wsdlImportElement.@location.text())
      }
    }
}