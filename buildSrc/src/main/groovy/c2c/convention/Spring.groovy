package c2c.convention;

import org.gradle.api.*;

class Spring {
    def transitive = true
    def version = "3.0.4.RELEASE"
    def modules = ['spring-webmvc', 'spring-jdbc', 'spring-aop','spring-web']
    def otherDeps = ['org.apache.httpcomponents:httpclient:4.0-beta2',
                     'org.slf4j:slf4j-log4j12:1.4.3', 'javax.servlet:jstl:1.1.2', 
                     'org.tuckey:urlrewritefilter:3.1.0'] 
    def repositories = [:]
    def excludes = ['org.tuckey:urlrewritefilter:3.1.0':'commons-logging:commons-logging']
    
    def dependencyString(module) {return "org.springframework:$module:$version"}
        
}