// Plugin containing geotools dependencies.  See the conventions file for configuration options
package c2c.plugin

import org.gradle.api.*

class Geotools implements Plugin<Project> {
    
    public void apply(Project project) {
        def convention = new c2c.convention.Geotools()
        project.convention.plugins.geotools = convention

        DependencyPlugins.apply(project,convention,"c2c.plugin.Geotools")
    }
}