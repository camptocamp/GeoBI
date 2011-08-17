package c2c.convention;

import org.gradle.api.*;

class Geotools {
    boolean transitive = true
    String version = "2.7-SNAPSHOT"
    List<String> modules = ["epsg-hsql", "shapefile", "render", "geojson","brewer"]
    List<String> otherDeps = []
    Map<String,String> repositories = ["ibiblio":"http://www.ibiblio.org/maven2", 
                        "osgeo":"http://download.osgeo.org/webdav/geotools",
                        "opengeo":"http://repo.opengeo.org/"]
    Map<String,String> excludes = [:]
    
    def dependencyString(module) {
        if(module.startsWith("xsd-")) {
            return "org.geotools.xsd:gt-$module:$version"
        } else {
            return "org.geotools:gt-$module:$version"
        }
    }
    def geotools(Closure closure) {
        closure.delegate = this
        closure()
    }
    
}