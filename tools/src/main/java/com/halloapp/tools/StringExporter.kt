package com.halloapp.tools

import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFCellStyle
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.w3c.dom.Document
import org.w3c.dom.Node
import java.io.File
import java.io.FileOutputStream
import javax.xml.parsers.DocumentBuilderFactory


open class StringExporter(private val resourcesDir: File) {
    private val enStrings: List<StringResource>
    private val enPlurals: List<PluralResource>
    private val dbFactory = DocumentBuilderFactory.newInstance()
    private val dBuilder = dbFactory.newDocumentBuilder()
    private var styles: Map<String, CellStyle>? = null
    init {
        val stringsFile = File(resourcesDir, "values/strings.xml")

        val doc = dBuilder.parse(stringsFile)
        doc.documentElement.normalize()

        enStrings = parseStrings(doc)
        enPlurals = parsePlurals(doc)
    }

    private fun fetchLocaleStrings(id: String, strings: HashMap<String, String>, plurals: HashMap<String, PluralResource>) {
        var localeStrings: List<StringResource> = ArrayList()
        var localePlurals: List<PluralResource> = ArrayList()
        val localeStringsFile = File(resourcesDir, "values-$id/strings.xml")
        if (localeStringsFile.exists()) {
            val localeDoc = dBuilder.parse(localeStringsFile)
            localeDoc.documentElement.normalize()

            localeStrings = parseStrings(localeDoc)
            localePlurals = parsePlurals(localeDoc)
        }
        for (stringResource in localeStrings) {
            strings[stringResource.name] = stringResource.text
        }
        for (plural in localePlurals) {
            plurals[plural.name] = plural
        }
    }

    private fun outputLocale(locale: Locale, translationWb: XSSFWorkbook) {
        val stringMap = HashMap<String, String>()
        val pluralMap = HashMap<String, PluralResource>()

        fetchLocaleStrings(locale.identifier, stringMap, pluralMap)

        val localeSheet = translationWb.createSheet(locale.name + " - " + locale.identifier)
        localeSheet.defaultColumnWidth = 40
        addHeader(localeSheet, "String Id", "English", locale.name)

        var rowCount = 1
        for (enStringResource in enStrings) {
            outputStringRow(localeSheet, rowCount, enStringResource, stringMap[enStringResource.name])
            rowCount++
        }

        val localePluralSheet = translationWb.createSheet(locale.name + " - " + locale.identifier + " (Plurals)")
        localePluralSheet.defaultColumnWidth = 40
        addHeader(localePluralSheet,
                "Plural Id",
                "zero - en", "zero - " + locale.identifier,
                "one - en", "one - " + locale.identifier,
                "two - en", "two - " + locale.identifier,
                "few - en", "few - " + locale.identifier,
                "many - en", "many - " + locale.identifier,
                "other - en", "other - " + locale.identifier
        )

        rowCount = 1
        for (enPluralResource in enPlurals) {
            outputPluralRow(localePluralSheet, rowCount, enPluralResource, pluralMap[enPluralResource.name])
            rowCount++
        }
    }

    private fun addHeader(sheet: Sheet, vararg headers: String) {
        val headerRow = sheet.createRow(0)
        for (i in headers.indices) {
            createHeaderCell(headerRow, i, headers[i])
        }
        sheet.createFreezePane(0, 1)
    }

    private fun outputStringRow(sheet: Sheet, rowCount: Int, parentString: StringResource, localeString: String?) {
        val row = sheet.createRow(rowCount)
        createFadedCell(row, 0, parentString.name)
        createNormalCell(row, 1, parentString.text)
        createNormalCell(row, 2, localeString)
    }

    private fun outputPluralRow(sheet: Sheet, rowCount: Int, parentPlural: PluralResource, localePlural: PluralResource?) {
        val row = sheet.createRow(rowCount)
        createFadedCell(row, 0, parentPlural.name)

        createNormalCell(row, 1, parentPlural.zero)
        createNormalCell(row, 3, parentPlural.one)
        createNormalCell(row, 5, parentPlural.two)
        createNormalCell(row, 7, parentPlural.few)
        createNormalCell(row, 9, parentPlural.many)
        createNormalCell(row, 11, parentPlural.other)

        createNormalCell(row, 2, localePlural?.zero)
        createNormalCell(row, 4, localePlural?.one)
        createNormalCell(row, 6, localePlural?.two)
        createNormalCell(row, 8, localePlural?.few)
        createNormalCell(row, 10, localePlural?.many)
        createNormalCell(row, 12, localePlural?.other)
    }

    private fun createFadedCell(row: Row, col: Int, text: String?) {
        val faded = styles!!["cell_faded"] as XSSFCellStyle
        createCell(row, col, text, faded)
    }

    private fun createNormalCell(row: Row, col: Int, text: String?) {
        val normal = styles!!["cell_normal"] as XSSFCellStyle
        createCell(row, col, text, normal)
    }

    private fun createHeaderCell(row: Row, col: Int, text: String?) {
        val header = styles!!["header"] as XSSFCellStyle
        createCell(row, col, text, header)
    }

    private fun createCell(row: Row, col: Int, text: String?, style: XSSFCellStyle) {
        val cell = row.createCell(col)
        cell.setCellValue(text)
        cell.cellStyle = style
    }

    fun outputSpreadsheet(outputFile: File) {
        val translationWb = XSSFWorkbook()
        styles = createStyles(translationWb)

        for(locale in supportedLocales) {
            outputLocale(locale, translationWb)
        }
        val out = FileOutputStream(outputFile)
        translationWb.write(out)
        out.close()

        translationWb.close()
    }
}

class Locale(val identifier: String, val name: String)
class StringResource(val name: String, val text: String)
class PluralResource(val name: String, val zero: String?, val one: String?, val two: String?, val few: String?, val many: String?, val other: String?)

fun main() {
    val stringsFile = File("../app/src/main/res")
    println("Exporting strings from " + stringsFile.absolutePath)

    val exporter = StringExporter(stringsFile)
    exporter.outputSpreadsheet(File("../strings.xlsx"))
}

fun createStyles(wb: Workbook): Map<String, CellStyle> {
    val styles: MutableMap<String, CellStyle> = HashMap()
    val df = wb.createDataFormat()
    var style: CellStyle
    val headerFont: Font = wb.createFont()
    headerFont.setBold(true)
    style = createBorderedStyle(wb)
    style.setAlignment(HorizontalAlignment.CENTER)
    style.fillForegroundColor = IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex()
    style.setFillPattern(FillPatternType.SOLID_FOREGROUND)
    style.setFont(headerFont)
    styles["header"] = style
    style = createBorderedStyle(wb)
    style.setAlignment(HorizontalAlignment.LEFT)
    style.wrapText = true
    styles["cell_normal"] = style
    val font = wb.createFont()
    font.color = IndexedColors.GREY_50_PERCENT.index
    style = createBorderedStyle(wb)
    style.setAlignment(HorizontalAlignment.LEFT)
    style.wrapText = true
    style.setFont(font)
    styles["cell_faded"] = style
    style = createBorderedStyle(wb)
    style.setAlignment(HorizontalAlignment.RIGHT)
    style.wrapText = true
    style.dataFormat = df.getFormat("d-mmm")
    styles["cell_normal_date"] = style
    style = createBorderedStyle(wb)
    style.setAlignment(HorizontalAlignment.LEFT)
    style.indention = 1.toShort()
    style.wrapText = true
    styles["cell_indented"] = style
    style = createBorderedStyle(wb)
    style.fillForegroundColor = IndexedColors.BLUE.getIndex()
    style.setFillPattern(FillPatternType.SOLID_FOREGROUND)
    styles["cell_blue"] = style
    return styles
}

fun createBorderedStyle(wb: Workbook): CellStyle {
    val thin = BorderStyle.THIN
    val black = IndexedColors.BLACK.getIndex()
    val style = wb.createCellStyle()
    style.setBorderRight(thin)
    style.rightBorderColor = black
    style.setBorderBottom(thin)
    style.bottomBorderColor = black
    style.setBorderLeft(thin)
    style.leftBorderColor = black
    style.setBorderTop(thin)
    style.topBorderColor = black
    return style
}

fun parseStrings(doc: Document): List<StringResource> {
    val strings = ArrayList<StringResource>()

    val stringList = doc.getElementsByTagName("string")
    for (i in 0 until stringList.length) {
        val stringNode: Node = stringList.item(i)

        if (stringNode.nodeType != Node.ELEMENT_NODE) {
            continue
        }
        var strName: String? = null
        var translatable: String? = null
        for (i in 0 until stringNode.attributes.length) {
            val node = stringNode.attributes.item(i)
            when (node.nodeName) {
                "name" -> strName = node.nodeValue
                "translatable" -> translatable = node.nodeValue
            }
        }
        if (strName == null || translatable == "false") {
            continue
        }
        val strValue = stringNode.textContent

        strings.add(StringResource(strName, strValue))
    }
    return strings
}

fun parsePlurals(doc: Document): List<PluralResource> {
    val plurals = ArrayList<PluralResource>()
    val pluralsList = doc.getElementsByTagName("plurals")
    for (i in 0 until pluralsList.length) {
        val pluralNode: Node = pluralsList.item(i)

        if (pluralNode.nodeType != Node.ELEMENT_NODE) {
            continue
        }

        var zero: String? = null
        var one: String? = null
        var two: String? = null
        var few: String? = null
        var many: String? = null
        var other: String? = null

        val strName = pluralNode.attributes.getNamedItem("name").textContent
        for (j in 0 until pluralNode.childNodes.length) {
            val childNode: Node = pluralNode.childNodes.item(j)

            if (childNode.nodeType != Node.ELEMENT_NODE) {
                continue
            }

            when(childNode.attributes.getNamedItem("quantity").textContent) {
                "zero" -> zero = childNode.textContent
                "one" -> one = childNode.textContent
                "two" -> two = childNode.textContent
                "few" -> few = childNode.textContent
                "many" -> many = childNode.textContent
                "other" -> other = childNode.textContent
            }
        }

        plurals.add(PluralResource(strName, zero, one, two, few, many, other))
    }
    return plurals
}
