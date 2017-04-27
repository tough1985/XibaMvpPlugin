package me.xiba

import org.gradle.api.Project
import org.gradle.api.Plugin

class XibaMvpPlugin implements Plugin<Project> {
    void apply(Project target){
        target.extensions.create("xibaMvp", XibaMvpExtension)

        target.task('generateMvp', type: XibaMvpTask){
            group "mvpGenerator"
        }
    }
}