package c2c.task;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import java.io.File

class ListPlugins extends DefaultTask {
    {
        description = "Lists the camptocamp custom plugins"
    }
    @TaskAction
    def list () {
        println "\nPlugins: "
        new File("buildSrc/src/main/groovy/c2c/plugin").listFiles().findAll{it.isFile()}.each { file ->
            def baseName = file.name.substring(0,file.name.lastIndexOf("."))

            def reader = file.newReader("UTF-8")
            def firstLine = reader.readLine()
            reader.close()

            def desc = ""
            if(firstLine.startsWith("// ")){
                desc = " - "+firstLine.substring(3)
            }
            println "  "+baseName + desc
        }
        
    }
}