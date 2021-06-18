package com.github.jrt324.mysqltoh2sql.services

import com.github.jrt324.mysqltoh2sql.MyBundle
import com.intellij.openapi.project.Project

class MyProjectService(project: Project) {

    init {
        println(MyBundle.message("projectService", project.name))
    }
}
