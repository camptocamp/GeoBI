// Plugin containing geotools dependencies.  See the conventions file for configuration options
package c2c.plugin

import org.gradle.api.*

class Scala implements Plugin<Project> {
    def scalaVersion = '2.8.0.RC2'
    List<String> scalaTestLibs = ['org.scala-tools.testing:scalacheck_2.8.0.RC2:1.7', 
                                  'org.scala-tools.testing:specs_2.8.0.RC2:1.6.5-SNAPSHOT',
                                  'junit:junit:4.7']
    String scalaLibrary() {return "org.scala-lang:scala-library:$scalaVersion"}
    String scalaCompiler() {return "org.scala-lang:scala-compiler:$scalaVersion"}
    
    public void apply(Project project) {
        project.apply plugin:'scala'
        project.repositories.mavenRepo name: 'scala-repo', urls: 'http://scala-tools.org/repo-releases/'
        project.repositories.mavenRepo name: 'scala-repo-snapshot', urls: 'http://nexus.scala-tools.org/content/repositories/snapshots/'
        project.dependencies {
            scalaTools scalaCompiler()
            scalaTools scalaLibrary()
            
            compile scalaLibrary()
            testCompile scalaTestLibs
        }
    }
}