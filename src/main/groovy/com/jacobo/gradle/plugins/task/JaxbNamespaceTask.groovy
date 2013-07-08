package com.jacobo.gradle.plugins.task

import org.gradle.api.logging.Logging
import org.gradle.api.logging.Logger
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.Input
import org.gradle.api.DefaultTask
import com.jacobo.gradle.plugins.structures.OrderGraph
import com.jacobo.gradle.plugins.util.FileHelper
import com.jacobo.gradle.plugins.structures.NamespaceMetaData
import com.jacobo.gradle.plugins.JaxbPlugin

/**
 * @author djmijares
 * Created: Tue Dec 04 09:01:34 EST 2012
 */

class JaxbNamespaceTask extends DefaultTask { 
  
  static final Logger log = Logging.getLogger(JaxbNamespaceTask.class)
  
  @Input
  String xsdDir

  OrderGraph order = new OrderGraph()

  @TaskAction
  void start() { 
    log.info("finding all xsd files in: {}", getXsdDir())
    def files = FileHelper.findAllXsdFiles(getXsdDir())

    log.info("aquiring unique namespaces from xsd files")
    files.each { file ->
      order.obtainUniqueNamespaces(file)
    }

    log.info("getting all import and includes statments in namespace files to see what they depend on and resolving internal/external namespace dependencies")
    order.obtainNamespacesMetaData()

    order.gatherIndependentNamespaces()
    log.info("found namespaces that need to be parsed first")

    order.arrangeDependentNamespacesInOrder()
    log.info("namespaces with dependencies have been arranged on the order graph")

    log.info("processing external Dependencies")
    order.obtainExternalImportsDependencies()

    project.jaxb.dependencyGraph = order
    log.info("all {} (unique) namespaces have been ordered and saved for parsing. Their order is : {}", order.namespaceData.size(), order.orderGraph)
  }

}