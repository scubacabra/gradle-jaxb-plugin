package com.jacobo.gradle.plugins.task

import org.gradle.api.logging.Logging
import org.gradle.api.logging.Logger
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.DefaultTask
import com.jacobo.gradle.plugins.structures.OrderGraph
import com.jacobo.gradle.plugins.structures.NamespaceMetaData
import com.jacobo.gradle.plugins.JaxbNamespacePlugin

/**
 * @author djmijares
 * Created: Tue Dec 04 09:01:34 EST 2012
 */

class JaxbNamespaceTask extends DefaultTask { 
  
  static final Logger log = Logging.getLogger(JaxbNamespaceTask.class)
  
  String xsdDir
  OrderGraph order = new OrderGraph()

  @TaskAction
  void start() { 
    log.info("starting jaxb namespace dependency task at: {}", project.jaxb.xsdDirectoryForGraph)

    order.findAllXsdFiles(project.jaxb.xsdDirectoryForGraph)

    log.info("unique namespaces aquired")
    log.info("getting all import and includes statments in namespace files to see what they depend on")

    order.populateIncludesAndImportsData()
    log.info("processing includes data and removing from files data accordingly")

    order.performIncludesProcessing()
    order.gatherInitialNamespaceGraphOrdering()

    log.info("found base namespace packages")

    order.parseEachDependentNamespace()

    log.info("parsed through dependent namespaces")
    log.info("processing external Dependencies")

    order.processExternalImports()

    log.info("namespace dependency graph is resolved")
    log.debug("order Graph is {}", order.orderGraph)
    log.info("namespaceData size (unique namespace) is {}", order.namespaceData.size())
    log.debug("namespaceData is {}", order.namespaceData)

    log.debug("converting all namespaces in order graph into their appropriate episode file names")
    order.namespaceData.each { it.convertNamespaceToEpisodeName() }

    project.jaxb.dependencyGraph = order

    log.info("adding order graph to the jaxb extension")
  }

}