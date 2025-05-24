package com.pheide.controller

import com.pheide.view.View
import org.slf4j.Logger
import org.slf4j.LoggerFactory

abstract class BaseController {

    abstract fun doAction(action: String?, params: Map<String, String?>): String?

}
