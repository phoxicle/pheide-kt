package com.pheide.controller

import com.pheide.view.View
import org.slf4j.Logger
import org.slf4j.LoggerFactory

abstract class BaseController(
    val logger: Logger = LoggerFactory.getLogger("Main"),
    val view: View = View()) {

    abstract fun doAction(action: String?, params: Map<String, String?>): String?

}
