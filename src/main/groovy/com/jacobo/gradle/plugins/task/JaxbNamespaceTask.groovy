package com.jacobo.gradle.plugins.task

import org.gradle.api.logging.Logging
import org.gradle.api.logging.Logger
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.DefaultTask
import com.jacobo.gradle.plugins.structures.OrderGraph
import com.jacobo.gradle.plugins.structures.XsdNamespaces
import com.jacobo.gradle.plugins.structures.FindNamespaces

/**
 * @author djmijares
 * Created: Tue Dec 04 09:01:34 EST 2012
 */

class JaxbNamespaceTask extends DefaultTask { 
  
  static final Logger log = Logging.getLogger(JaxbNamespaceTask.class)
  
  String xsdDir

  List nsCollection = []
  OrderGraph order = new OrderGraph()

  def grabUniqueNamespaces() { 
    def findNs = new FindNamespaces(baseDir: getXsdDir())
    findNs.startFinding()
    findNs.nsMap.each { key, val -> 
      order.nsCollection << new XsdNamespaces(namespace: key, xsdFiles: val)
    }
  }

  @TaskAction
  void start() { 
    log.info("starting jaxb namespace dependency task at: {}", getXsdDir())
    grabUniqueNamespaces()
    log.info("unique namespaces aquired")
    log.info("getting all import statments in namespace files to see what they depend on")
    order.grabNamespaceImports()
    order.findBaseSchemaNamespaces()
    log.info("found the base namespace packages to be parsed first")
    order.findDependentSchemaNamespaces()
    log.info("aquired the rest of the namespaces to be graphed out")
    order.parseEachDependentNamespace()
    log.info("namespace dependency graph is resolved")
    project.convention.plugins.JaxbNamespacePlugin.target = nsCollection
    log.info ("asafasf, {}", project.convention.plugins.JaxbNamespacePlugin.target[0])
    order.orderGraph.each { order ->
      order.each { namespace ->
	def nsData = order.nsCollection.find{ it.namespace == namespace}
	doTheSchemaParsing(nsData)
      }
    }
  }

  def doTheSchemaParsing(XsdNamespaces ns) { 
    ant.taskdef (name : 'xjc', classname : 'com.sun.tools.xjc.XJCTask', classpath : project.configurations[JaxbNamespacePlugin.JAXB_CONFIGURATION].asPath)
    ant.xjc(destdir : "src/main/java", extension : 'true', removeOldOutput : 'yes', header : false, target : '2.1') {
      produces (dir : "src/main/java")
      schema (dir : getXsdDir(), includes : ns.xsdFiles )
      binding(dir : "${rootDir}/XMLSchema", includes : '*.xjb')
      ns.fileImports.each { episode ->
	log.debug("binding with file {}", episodeDir+episode+".episode")
	binding (dir : "${rootDir}/XMLSchema/Episodes", includes : "${episode}.episode")
      }
      arg(value : '-episode')
      arg(value: "${rootDir}/XMLSchema/Episodes/${ns.namespace}.episode")
      arg(value : '-verbose')
      arg(value : '-npa')
    }
  }
}