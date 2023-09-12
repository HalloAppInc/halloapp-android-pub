package com.halloapp.tools

import com.google.gson.Gson
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.content.PartData
import io.ktor.utils.io.core.internal.*
import io.ktor.utils.io.streams.asInput
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.FileInputStream


object POEditorApi {

    const val EXPORT_ENDPOINT = "https://api.poeditor.com/v2/projects/export"
    const val UPLOAD_ENDPOINT = "https://api.poeditor.com/v2/projects/upload"
    const val LIST_LANGUAGES_ENDPOINT = "https://api.poeditor.com/v2/languages/list"


    data class Response(
            val status: String,
            val code: Int,
            val result: Result?
    )

    data class Result(
            val url: String?,
            val languages: List<Language>?,
            val terms: ParseResult?,
            val translations: ParseResult?
    )

    data class Language(
            val name: String,
            val code: String,
            val translations: Int,
            val percentage: Float,
            val updated: String
    )

    data class ParseResult(
            val parsed: Int,
            val added: Int,
            val deleted: Int?,
            val updated: Int?
    )

    @OptIn(DangerousInternalIoApi::class)
    suspend fun upload(apiToken: String, id: Int, file: File): Response {
        val client = HttpClient(Apache)

        val data: List<PartData> = formData {
            append("api_token", apiToken)
            append("id", id)
            append("updating", "terms_translations")
            append("language", "en")
            append("sync_terms", 1)
            append("overwrite", 1)
            append("fuzzy_trigger", 1)
            appendInput("file", size = file.length(), headers = Headers.build {
                append(HttpHeaders.ContentDisposition, "filename=${file.name}")
            }) { FileInputStream(file).asInput() }
            append("type", "android_strings")
        }

        val response: String = client.submitFormWithBinaryData(UPLOAD_ENDPOINT, data)

        val gson = Gson()
        val result: Response = gson.fromJson(response, Response::class.java)
        return result
    }

    suspend fun export(apiToken: String, id: Int, languageCode: String): String? {
        val client = HttpClient(Apache)

        val data: List<PartData> = formData {
            append("api_token", apiToken)
            append("id", id)
            append("language", languageCode)
            append("type", "android_strings")
        }

        val response: String = client.submitFormWithBinaryData(EXPORT_ENDPOINT, data)

        val gson = Gson()
        val result: Response = gson.fromJson(response, Response::class.java)
        return result.result?.url
    }

    suspend fun listLanguages(apiToken: String, id: Int): List<POEditorApi.Language>? {
        val client = HttpClient(Apache)

        val data: List<PartData> = formData {
            append("api_token", apiToken)
            append("id", id)
        }

        val response: String = client.submitFormWithBinaryData(LIST_LANGUAGES_ENDPOINT, data)

        val gson = Gson()
        val result: Response = gson.fromJson(response, Response::class.java)
        return result.result?.languages
    }
}
