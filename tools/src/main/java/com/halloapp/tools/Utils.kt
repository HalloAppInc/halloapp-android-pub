package com.halloapp.tools

import java.io.File

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
    return File("../app/src/main/res/values-$id/strings.xml")
}
