package com.jacobo.gradle.plugins.task

import org.gradle.api.logging.Logging
import org.gradle.api.logging.Logger
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.DefaultTask
import com.jacobo.gradle.plugins.structures.OrderGraph
import com.jacobo.gradle.plugins.structures.XsdNamespaces
import com.jacobo.gradle.plugins.structures.FindNamespaces
import com.jacobo.gradle.plugins.JaxbNamespacePlugin

/**
 * @author djmijares
 * Created: Tue Dec 04 09:01:34 EST 2012
 */

class JaxbNamespaceTask extends DefaultTask { 
  
  static final Logger log = Logging.getLogger(JaxbNamespaceTask.class)
  
  String xsdDir
  OrderGraph order = new OrderGraph()

  def grabUniqueNamespaces() { 
    def findNs = new FindNamespaces(baseDir: project.jaxb.xsdDirectoryForGraph)
    findNs.startFinding()
    log.debug("unique namespace map is {}", findNs.nsMap)
    log.info("found {} unique namespace in {}", findNs.nsMap.size(), findNs.baseDir)
    findNs.nsMap.each { key, val -> 
      order.nsCollection << new XsdNamespaces(namespace: key, xsdFiles: val)
    }
  }

  @TaskAction
  void start() { 
    log.info("starting jaxb namespace dependency task at: {}", project.jaxb.xsdDirectoryForGraph)
    grabUniqueNamespaces()
    log.info("unique namespaces aquired")
    log.info("getting all import and includes statments in namespace files to see what they depend on")
    order.populateIncludesAndImportsData()
    log.info("processing includes data and removing from files data accordingly")
    order.performIncludesProcessing()
    order.gatherInitialNamespaceGraphOrdering()
    order.parseEachDependentNamespace()
    log.info("namespace dependency graph is resolved")

    log.debug("order Graph is {}", order.orderGraph)
    log.info("nsCollection size (unique namespace) is {}", order.nsCollection.size())
    log.debug("nsCollection is {}", order.nsCollection)
    project.jaxb.dependencyGraph = order
    log.info("adding order graph to the jaxb extension")
  }

}