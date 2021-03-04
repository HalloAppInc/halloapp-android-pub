package com.halloapp.tools

import org.apache.http.util.TextUtils
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.w3c.dom.Document
import org.w3c.dom.Element
import java.io.File
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

fun main() {
    val importer = StringImporter()
    importer.parseCurrentStrings(getStringFile(null))
    val needsExport = importer.parseGoogleSheet()
    importer.outputStrings()
    println()
    if (needsExport) {
        val stringsFile = File("../app/src/main/res")
        val spreadSheetFile = File("../strings.xlsx")
        print("Exporting strings to ${spreadSheetFile.absoluteFile.normalize()}...")

        val exporter = StringExporter(stringsFile)

        exporter.outputSpreadsheet(spreadSheetFile)
        println("Done!\n\nMake sure to upload the updated sheet to: https://docs.google.com/spreadsheets/d/${SheetFetcher.STRINGS_SPREADSHEET_ID}/")
    } else {
        println("No need to export, string update complete!")
    }
}

class LocalizedStrings() {
    var strings = ArrayList<StringResource>()
    var plurals = ArrayList<PluralResource>()
}

class StringImporter() {
    private var localizations = HashMap<String, LocalizedStrings>()
    private val enStrings: HashMap<String, StringResource> = HashMap()
    private val enPlurals: HashMap<String, PluralResource> = HashMap()
    private val validator = StringResValidator()

    fun outputStrings() {
        println("Creating strings.xml files...")
        for (id in localizations.keys) {
            val stringsFile = getStringFile(id)
            print("${stringsFile.absoluteFile.normalize()}...")

            val dbFactory: DocumentBuilderFactory = DocumentBuilderFactory.newInstance()
            val dBuilder: DocumentBuilder = dbFactory.newDocumentBuilder()
            val doc: Document = dBuilder.newDocument()

            val root = doc.createElement("resources")
            val localizedStrings: LocalizedStrings = localizations[id]!!
            for (strResource in localizedStrings.strings) {
                val strElement = doc.createElement("string")
                strElement.setAttribute("name", strResource.name)
                strElement.textContent = processString(strResource.text)
                root.appendChild(strElement)
            }
            for (pluralResource in localizedStrings.plurals) {
                val pluralElement = doc.createElement("plurals")
                pluralElement.setAttribute("name", pluralResource.name)
                if (!TextUtils.isEmpty(pluralResource.few)) {
                    pluralElement.appendChild(createItemNode(doc, "few", pluralResource.few!!))
                }
                if (!TextUtils.isEmpty(pluralResource.zero)) {
                    pluralElement.appendChild(createItemNode(doc, "zero", pluralResource.zero!!))
                }
                if (!TextUtils.isEmpty(pluralResource.one)) {
                    pluralElement.appendChild(createItemNode(doc, "one", pluralResource.one!!))
                }
                if (!TextUtils.isEmpty(pluralResource.two)) {
                    pluralElement.appendChild(createItemNode(doc, "two", pluralResource.two!!))
                }
                if (!TextUtils.isEmpty(pluralResource.many)) {
                    pluralElement.appendChild(createItemNode(doc, "many", pluralResource.many!!))
                }
                if (!TextUtils.isEmpty(pluralResource.other)) {
                    pluralElement.appendChild(createItemNode(doc, "other", pluralResource.other!!))
                }
                root.appendChild(pluralElement)
            }
            doc.appendChild(root)
            val transformerFactory: TransformerFactory = TransformerFactory.newInstance()
            val transformer: Transformer = transformerFactory.newTransformer()
            val source = DOMSource(doc)
            stringsFile.parentFile.mkdirs()

            val result = StreamResult(stringsFile)
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            transformer.transform(source, result)

            println("Done!")
        }
        println("\nImport completed successfully")
    }

    private fun createItemNode(doc: Document, quantity: String, text: String): Element {
        val element = doc.createElement("item")
        element.setAttribute("quantity", quantity)
        element.textContent = processString(text)
        return element
    }

    fun parseCurrentStrings(stringsFile: File) {
        val dbFactory = DocumentBuilderFactory.newInstance()
        val dBuilder = dbFactory.newDocumentBuilder()
        val doc = dBuilder.parse(stringsFile)
        doc.documentElement.normalize()

        val strings = parseStrings(doc)
        for (string in strings) {
            enStrings[string.name] = string
        }
        val plurals = parsePlurals(doc)
        for (plural in plurals) {
            enPlurals[plural.name] = plural
        }
    }

    fun parseGoogleSheet(): Boolean {
        return SheetFetcher.fetchStrings(enStrings, enPlurals, localizations)
    }

    fun processString(str: String): String {
        var updatedStr = str.replace("...", "…")
        updatedStr = updatedStr.replace(Regex("(?<![\\\\])'"), "\\\\'")
        return updatedStr
    }
}
