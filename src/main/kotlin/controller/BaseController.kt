package com.pheide.controllers

abstract class BaseController {

    abstract fun doAction(action: String?, params: Map<String, String?>, isAuthenticated: Boolean): String?

}