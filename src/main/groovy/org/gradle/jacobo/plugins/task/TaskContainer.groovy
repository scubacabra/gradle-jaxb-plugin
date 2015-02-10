package org.gradle.jacobo.plugins.task

/**
 * Created by James Cefaratti
 * 2/9/15.
 */
class TaskContainer {

    List<File> xsds
    List<File> episodes
    List<File> episodeFiles
    
    TaskContainer() {
        xsds = []
        episodes = []
        episodeFiles = []
        
    }
}
