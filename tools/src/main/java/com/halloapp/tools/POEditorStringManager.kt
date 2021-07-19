package com.halloapp.tools

import kotlinx.coroutines.runBlocking
import org.w3c.dom.Document
import org.w3c.dom.Node
import org.w3c.dom.Text
import org.w3c.dom.bootstrap.DOMImplementationRegistry
import org.w3c.dom.ls.DOMImplementationLS
import org.w3c.dom.ls.LSOutput
import org.w3c.dom.ls.LSSerializer
import java.io.*
import java.net.URL
import java.net.URLConnection
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.text.Charsets.UTF_8


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

    fun uploadStrings() {
        runBlocking {
            uploadStringsToPOEditor()
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

            processStrings(getStringFile(language.code))
        }
    }

    fun processStrings(stringsFile : File) {
        val dbFactory = DocumentBuilderFactory.newInstance()
        val dBuilder = dbFactory.newDocumentBuilder()
        val doc = dBuilder.parse(stringsFile)
        doc.documentElement.normalize()

        processNodes(doc, "string")
        processNodes(doc, "item")

        outputXml(doc, stringsFile)
    }

    fun processNodes(doc : Document, tag : String) {
        val stringList = doc.getElementsByTagName(tag)
        for (i in 0 until stringList.length) {
            val stringNode: Node = stringList.item(i)

            if (stringNode.nodeType != Node.ELEMENT_NODE) {
                continue
            }
            var strName: String? = null
            var translatable: String? = null
            for (j in 0 until stringNode.attributes.length) {
                val node = stringNode.attributes.item(j)
                when (node.nodeName) {
                    "name" -> strName = node.nodeValue
                    "translatable" -> translatable = node.nodeValue
                    "quantity" -> strName = node.nodeValue
                }
            }
            if (strName == null || translatable == "false") {
                continue
            }
            processNode(stringNode)
        }
    }

    fun processNode(node: Node) {
        val childNodes = node.childNodes
        for (j in 0 until childNodes.length) {
            val n = childNodes.item(j)
            if (n is Text) {
                n.textContent = processString(n.textContent)
            } else {
                processNode(n)
            }
        }
    }

    fun outputXml(doc : Node, output :File) {
        val dom: DOMImplementationLS = DOMImplementationRegistry.newInstance().getDOMImplementation("LS") as DOMImplementationLS
        val serializer: LSSerializer = dom.createLSSerializer()
        serializer.newLine = "\n"
        val destination: LSOutput = dom.createLSOutput()
        destination.setEncoding(UTF_8.name())
        val bos = FileOutputStream(output)
        destination.setByteStream(bos)
        serializer.write(doc, destination)

        // Ensure file ends with a new line
        bos.write("\n\n".toByteArray(UTF_8))
        bos.flush()
    }

    fun processString(str: String): String {
        return str.replace("...", "…")
    }

}
