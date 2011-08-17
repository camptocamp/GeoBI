// Plugin containing geotools dependencies.  See the conventions file for configuration options
package c2c.plugin

import org.gradle.api.*

class Spring implements Plugin<Project> {  
    public void apply(Project project) {
        def convention = new c2c.convention.Spring()
        project.convention.plugins.spring = convention  
        
        DependencyPlugins.apply(project,convention,"c2c.plugin.Spring")
    }
}