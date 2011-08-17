package c2c.task;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

class WarProjectLayout extends ProjectLayout  { 
    {
        description = "Adds files for a default Camptocamp Spring webapp project configuration"
    }
 
     @TaskAction
     def filter() {
         
     }   
}