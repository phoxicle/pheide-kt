package com.pheide.controller

import com.pheide.controller.Authenticator.verifyAccess
import com.pheide.repository.DAL
import io.ktor.server.application.*

class AdminController(
    call: ApplicationCall,
) : BaseController(call) {

    override suspend fun doAction(action: String?, params: Map<String, String?>) {
        when (action?.lowercase()) {
            "reset" -> reset()
            else -> null
        }
    }

    suspend fun reset() {
        verifyAccess(call)
        DAL.clearData()
        DAL.createSchemaAndPopulateDataIfNone()
        redirect(LinkBuilder.link("page", "show"))
    }
}
