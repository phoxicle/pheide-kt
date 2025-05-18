package com.pheide.controller

abstract class BaseController {

    abstract fun doAction(action: String?, params: Map<String, String?>, isAuthenticated: Boolean): String?

}
