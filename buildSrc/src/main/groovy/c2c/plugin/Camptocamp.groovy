// A standard set of plugins for a camptocamp project containing code (not a parent project)
package c2c.plugin;

import org.gradle.api.*;
import org.gradle.api.plugins.*;
import c2c.task.*

class Camptocamp implements Plugin<Project> {  
    final String FILTER_RESOURCES_TASKNAME = "filterResources"
    final String FILTER_WEBAPP_TASKNAME = "filterWebapp"
    
    public void apply(Project project) {
        def env = System.getenv()
        
    	project.repositories.mavenRepo (name: 'mapfish', urls: "http://dev.mapfish.org/maven/repository")        

        def localRepo = env['MAVEN_REPO'] ?: env['HOME']+'/.m2/repository'
        project.repositories.mavenRepo (name: 'localm2', urls: "file://${localRepo}")
        
        def convention = new c2c.convention.Camptocamp(project)
        project.convention.plugins.camptocamp = convention

        configureFiltering(project, FILTER_RESOURCES_TASKNAME, convention.filterResourcesIn, convention.filterResourcesOut)
        configureFiltering(project, FILTER_WEBAPP_TASKNAME, convention.filterWebappIn, convention.filterWebappOut)

        addDefaultTasks(project)
        project.afterEvaluate {
            configureWarPlugins(project, convention)        
            configureJettyRunAll(project)
        }
    }

    def configureJettyRunAll(Project project) {

        if(project.getTasksByName("war",false).isEmpty())  return ;
        
        project.tasks.addRule("Pattern: $JettyRunAllWar.TASK_NAME") { taskName -> 
            if (taskName == JettyRunAllWar.TASK_NAME) {
                def allTasks = project.tasks
                    allTasks.add(taskName, JettyRunAllWar.class)
            }
        }
    }

    def configureWarPlugins(Project project, c2c.convention.Camptocamp convention) {
         project.tasks.withType(org.gradle.api.tasks.bundling.War.class).allTasks { task ->
             task.dependsOn project.tasks.getByName(FILTER_WEBAPP_TASKNAME)
             task.from convention.filterWebappOut
         }
    }
    
    def addDefaultTasks(Project project) {
        project.tasks.add("layout", ProjectLayout.class)
        project.tasks.add("listPlugins", ListPlugins.class)
        project.tasks.add("addSecurityProxy", AddSecurityProxy.class)
        
        // TODO WAR plugin
    }
    
    def configureFiltering(Project project, String name, String input, String output) {
        Filtering filtering = project.tasks.add(name, Filtering.class)
        filtering.description = "copies all the files that need to have strings updated from the "+
                                "filter files to the build dir for inclusion into the webapp"
        
        filtering.from input
        filtering.into output
        
        project.ant.delete(dir: output)
        
        if(System.getProperty("server") == null) {
            project.logger.info("system property 'server' is not defined so defaulting to 'local'")
        }
        def server = System.getProperty("server") ?: "local"
        
        project.fileTree {
            from 'filters'
            include "$server/*.filter"
        }.each {
            filtering.filterFile(it)
        }
        
        def global = project.file("filters/global-resource.filter") 
        if (global.exists()) {
            filtering.filterFile(global)
        } else {
            project.logger.info("$global does not exist. Verify this is not an error")
        }
        
    }
}
