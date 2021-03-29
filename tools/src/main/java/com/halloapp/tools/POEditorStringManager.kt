package com.halloapp.tools

import kotlinx.coroutines.runBlocking
import java.io.BufferedInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.net.URL
import java.net.URLConnection

fun main() {
    POEditorStringManager.updateStrings()
}

object POEditorStringManager {

    const val PROJECT_ID = 423847

    fun updateStrings() {
        runBlocking {
            uploadStringsToPOEditor()
            importStringsFromPOEditor()
        }
    }

    suspend fun uploadStringsToPOEditor() {
        val token = getPOEditorToken()
        val stringsFile = getStringFile(null)
        println("Uploading $stringsFile")
        val result = POEditorApi.upload(token, PROJECT_ID, stringsFile)

        println("Done!")
        val terms = result.result?.terms
        if (terms != null) {
            println("Terms:")
            println(" - Parsed: ${terms.parsed}")
            println(" - Added: ${terms.added}")
            println(" - Deleted: ${terms.deleted}")
        }

        val translations = result.result?.translations
        if (translations != null) {
            println("Translations:")
            println(" - Parsed: ${translations.parsed}")
            println(" - Added: ${translations.added}")
            println(" - Updated: ${translations.updated}")
        }
    }

    suspend fun importStringsFromPOEditor() {
        val token = getPOEditorToken()

        val languages = POEditorApi.listLanguages(token, PROJECT_ID)
        if (languages == null) {
            println("No languages returned!")
            return
        }
        println("Fetching ${languages.size} languages")
        for (language in languages) {
            if ("en".equals(language.code)) continue
            println("Fetching ${language.name} - ${language.code}")
            val url = URL(POEditorApi.export(token, PROJECT_ID, language.code))
            val connection: URLConnection = url.openConnection()
            connection.connect()

            val input: InputStream = BufferedInputStream(url.openStream(),
                    8192)
            var stringFile = getStringFile(language.code)
            stringFile.parentFile.mkdirs()
            val output: OutputStream = FileOutputStream(getStringFile(language.code))

            val data = ByteArray(1024)

            var total: Long = 0

            var count: Int

            while (input.read(data).also { count = it } != -1) {
                total += count
                output.write(data, 0, count)
            }
            output.flush()
            output.close()
            input.close()
        }
    }
}
