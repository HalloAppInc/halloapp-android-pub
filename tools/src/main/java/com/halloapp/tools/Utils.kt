package com.halloapp.tools

import java.io.File
import java.util.Locale;

const val POEDITOR_TOKEN_FILE = "tokens/poeditor_token"

fun isEmpty(str: String?): Boolean {
    if (str == null) {
        return true
    }
    if (str.isEmpty() || str.isBlank()) {
        return true
    }
    return false
}

fun getStringFile(id: String?): File {
    if (id == null) {
        return File("../app/src/main/res/values/strings.xml")
    }
    var id = id
    if (id.contains("-")) {
        val parts = id.split("-")
        id = "${parts[0]}-r${parts[1].toUpperCase(Locale.ROOT)}"
    }
    return File("../app/src/main/res/values-$id/strings.xml")
}

fun getPOEditorToken(): String {
    val tokenFile = File(POEDITOR_TOKEN_FILE)
    if (!tokenFile.exists()) {
        println("Please add your token to ${tokenFile.absolutePath}" )
        throw Exception("POEditor API token missing!")
    }
    val token = File(POEDITOR_TOKEN_FILE).readText().trim()
    if (token.isEmpty()) {
        println("Please add your token to ${tokenFile.absolutePath}" )
        throw Exception("POEditor API token missing!")
    }
    return token
}
