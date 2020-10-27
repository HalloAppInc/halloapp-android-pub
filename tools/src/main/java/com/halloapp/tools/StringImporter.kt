package com.halloapp.tools

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
    importer.parseSpreadsheet(File("../strings.xlsx"))
    importer.outputStrings()
}

class LocalizedStrings() {
    var strings = ArrayList<StringResource>()
    var plurals = ArrayList<PluralResource>()
}

class StringImporter() {
    private val localizations = HashMap<String, LocalizedStrings>()
    private var enStrings: HashMap<String, StringResource> = HashMap()
    private var enPlurals: HashMap<String, PluralResource> = HashMap()

    fun outputStrings() {
        for (id in localizations.keys) {
            val stringsFile = getStringFile(id)

            val dbFactory: DocumentBuilderFactory = DocumentBuilderFactory.newInstance()
            val dBuilder: DocumentBuilder = dbFactory.newDocumentBuilder()
            val doc: Document = dBuilder.newDocument()

            val root = doc.createElement("resources")
            val localizedStrings: LocalizedStrings = localizations[id]!!
            for (strResource in localizedStrings.strings) {
                val strElement = doc.createElement("string")
                strElement.setAttribute("name", strResource.name)
                strElement.textContent = strResource.text
                root.appendChild(strElement)
            }
            for (pluralResource in localizedStrings.plurals) {
                val pluralElement = doc.createElement("plurals")
                pluralElement.setAttribute("name", pluralResource.name)
                if (pluralResource.few != null) {
                    pluralElement.appendChild(createItemNode(doc, "few", pluralResource.few))
                }
                if (pluralResource.zero != null) {
                    pluralElement.appendChild(createItemNode(doc, "zero", pluralResource.zero))
                }
                if (pluralResource.one != null) {
                    pluralElement.appendChild(createItemNode(doc, "one", pluralResource.one))
                }
                if (pluralResource.two != null) {
                    pluralElement.appendChild(createItemNode(doc, "two", pluralResource.two))
                }
                if (pluralResource.many != null) {
                    pluralElement.appendChild(createItemNode(doc, "many", pluralResource.many))
                }
                if (pluralResource.other != null) {
                    pluralElement.appendChild(createItemNode(doc, "other", pluralResource.other))
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
        }
    }

    private fun createItemNode(doc: Document, quantity: String, text: String): Element {
        val element = doc.createElement("item")
        element.setAttribute("quantity", quantity)
        element.nodeValue = text
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

    fun parseSpreadsheet(file: File) {
        println(file.absolutePath)
        val workbook = XSSFWorkbook(file)
        for (sheet in workbook.sheetIterator()) {
            var sheetLocale: Locale? = null
            for (locale in supportedLocales) {
                if (sheet.sheetName.contains(locale.name)) {
                    sheetLocale = locale
                    break;
                }
            }
            if (sheetLocale == null) {
                println("Unsupported locale found for sheet: " + sheet.sheetName)
                continue;
            }
            val localizedStrings: LocalizedStrings = localizations[sheetLocale.identifier] ?: LocalizedStrings()
            localizations[sheetLocale.identifier] = localizedStrings

            println("Processing ${sheet.sheetName}")
            if (sheet.sheetName.contains("Plurals")) {
                val iterator = sheet.iterator()
                iterator.next()
                while (iterator.hasNext()) {
                    val row = iterator.next()
                    if (row.getCell(0) == null) {
                        break
                    }
                    val strName = row.getCell(0).stringCellValue
                    val zero = row.getCell(2).stringCellValue
                    val one = row.getCell(4).stringCellValue
                    val two = row.getCell(6).stringCellValue
                    val few = row.getCell(8).stringCellValue
                    val many = row.getCell(10).stringCellValue
                    val other = row.getCell(12).stringCellValue

                    var enzero = row.getCell(1).stringCellValue
                    enzero = if (isEmpty(enzero)) null else enzero
                    var enone = row.getCell(3).stringCellValue
                    enone = if (isEmpty(enone)) null else enone
                    var entwo = row.getCell(5).stringCellValue
                    entwo = if (isEmpty(entwo)) null else entwo
                    var enfew = row.getCell(7).stringCellValue
                    enfew = if (isEmpty(enfew)) null else enfew
                    var enmany = row.getCell(9).stringCellValue
                    enmany = if (isEmpty(enmany)) null else enmany
                    var enother = row.getCell(11).stringCellValue
                    enother = if (isEmpty(enother)) null else enother

                    val currentPlural = enPlurals[strName]
                    if (currentPlural == null || currentPlural.other != enother || currentPlural.few != enfew || currentPlural.many != enmany || currentPlural.zero != enzero || currentPlural.one != enone || currentPlural.two != entwo) {
                        println("English plural no longer matches for $strName, skipping")
                    }
                    if (strName != null && (!isEmpty(zero) || !isEmpty(one) || !isEmpty(two) || !isEmpty(few) || !isEmpty(many) || !isEmpty(other))) {
                        localizedStrings.plurals.add(PluralResource(strName, zero, one, two, few, many, other))
                    }
                }
            } else {
                val iterator = sheet.iterator()
                iterator.next()
                while (iterator.hasNext()) {
                    val row = iterator.next()
                    if (row.getCell(0) == null) {
                        break
                    }
                    val strName = row.getCell(0).stringCellValue
                    val oldValue = row.getCell(1).stringCellValue
                    val strValue = row.getCell(2).stringCellValue
                    val currentResource = enStrings[strName]
                    if (currentResource == null || currentResource.text != oldValue) {
                        println("English string no longer matches for $strName, skipping")
                        continue
                    }
                    if (strName != null && !isEmpty(strValue)) {
                        localizedStrings.strings.add(StringResource(strName, strValue))
                    }
                }
            }
        }
    }
}
