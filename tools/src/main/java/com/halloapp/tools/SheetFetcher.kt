package com.halloapp.tools

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import com.google.api.services.sheets.v4.model.ValueRange
import java.io.*
import java.security.GeneralSecurityException
import java.util.*


object SheetFetcher {
    private const val APPLICATION_NAME = "HalloApp Android String Manager"
    private val JSON_FACTORY: JsonFactory = JacksonFactory.getDefaultInstance()
    private const val TOKENS_DIRECTORY_PATH = "tokens"

    /** Spreadsheet: https://docs.google.com/spreadsheets/d/1s_AFv-HzTEZf66OD8x4CHs2PjV50pcGeY2YfVBpm8d4/ */
    const val STRINGS_SPREADSHEET_ID = "1s_AFv-HzTEZf66OD8x4CHs2PjV50pcGeY2YfVBpm8d4"

    private val validator = StringResValidator()

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private val SCOPES: List<String> = Collections.singletonList(SheetsScopes.SPREADSHEETS)
    private const val CREDENTIALS_FILE_PATH = "credentials.json"

    /**
     * Creates an authorized Credential object.
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    @Throws(IOException::class)
    private fun getCredentials(HTTP_TRANSPORT: NetHttpTransport): Credential {
        // Load client secrets.
        val cred = FileInputStream(CREDENTIALS_FILE_PATH)
        val clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, InputStreamReader(cred))

        // Build flow and trigger user authorization request.
        val flow = GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(FileDataStoreFactory(File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build()
        val receiver = LocalServerReceiver.Builder().setPort(8888).build()
        return AuthorizationCodeInstalledApp(flow, receiver).authorize("user")
    }

    private fun getCell(row: List<Any>, col: Int): String? {
        if (row.size <= col) {
            return null
        }
        return row[col] as String
    }

    /**
     * Fetch the strings from the Google Doc Spreadsheet
     * Stores parsed localizations in the localizations parameter
     *
     * @return returns true if we need to re-export, false otherwise
     */
    @Throws(IOException::class, GeneralSecurityException::class)
    fun fetchStrings (enStrings: HashMap<String, StringResource>, enPlurals: HashMap<String, PluralResource>, localizationsOut: HashMap<String,LocalizedStrings>): Boolean {
        // Build a new authorized API client service.
        val httpTransport = GoogleNetHttpTransport.newTrustedTransport()
        val service = Sheets.Builder(httpTransport, JSON_FACTORY, getCredentials(httpTransport))
                .setApplicationName(APPLICATION_NAME)
                .build()
        val spreadsheet = service.spreadsheets()[STRINGS_SPREADSHEET_ID].execute()
        var parseSuccess = true
        var needExport = false

        for (sheet in spreadsheet.sheets) {
            var sheetLocale: Locale? = null
            for (locale in supportedLocales) {
                if (sheet.properties.title.contains(locale.name)) {
                    sheetLocale = locale
                    break;
                }
            }
            if (sheetLocale == null) {
                println("Unsupported locale found for sheet: " + sheet.properties.title)
                continue;
            }
            val localizedStrings: LocalizedStrings = localizationsOut[sheetLocale.identifier]
                    ?: LocalizedStrings()
            localizationsOut[sheetLocale.identifier] = localizedStrings

            println("Processing ${sheet.properties.title}")
            val response: ValueRange = service.spreadsheets().values()[STRINGS_SPREADSHEET_ID, "${sheet.properties.title}!A2:M"]
                    .execute()
            val values: List<List<Any>> = response.getValues()
            if (sheet.properties.title.contains("Plurals")) {
                val enPluralSet = HashSet(enPlurals.keys)
                val iterator = values.iterator()
                while (iterator.hasNext()) {
                    val row = iterator.next()
                    val strName = getCell(row, 0) ?: break
                    val zero = getCell(row, 2)
                    val one = getCell(row, 4)
                    val two = getCell(row, 6)
                    val few = getCell(row, 8)
                    val many = getCell(row, 10)
                    val other = getCell(row, 12)

                    var enzero = getCell(row, 1)
                    enzero = if (isEmpty(enzero)) null else enzero
                    var enone = getCell(row, 3)
                    enone = if (isEmpty(enone)) null else enone
                    var entwo = getCell(row, 5)
                    entwo = if (isEmpty(entwo)) null else entwo
                    var enfew = getCell(row, 7)
                    enfew = if (isEmpty(enfew)) null else enfew
                    var enmany = getCell(row, 9)
                    enmany = if (isEmpty(enmany)) null else enmany
                    var enother = getCell(row, 11)
                    enother = if (isEmpty(enother)) null else enother
                    enPluralSet.remove(strName)
                    val currentPlural = enPlurals[strName]
                    if (currentPlural == null || currentPlural.other != enother || currentPlural.few != enfew || currentPlural.many != enmany || currentPlural.zero != enzero || currentPlural.one != enone || currentPlural.two != entwo) {
                        needExport = true
                    }
                    if (!isEmpty(zero) || !isEmpty(one) || !isEmpty(two) || !isEmpty(few) || !isEmpty(many) || !isEmpty(other)) {
                        localizedStrings.plurals.add(PluralResource(strName, zero, one, two, few, many, other))
                    }
                }
                if (enPluralSet.isNotEmpty()) {
                    needExport = true
                }
            } else {
                val enStringSet = HashSet(enStrings.keys)
                val iterator = values.iterator()
                while (iterator.hasNext()) {
                    val row = iterator.next()
                    val strName = getCell(row, 0) ?: break
                    val oldValue = getCell(row, 1)
                    val strValue = getCell(row, 2)
                    val currentResource = enStrings[strName]
                    enStringSet.remove(strName)
                    if (currentResource == null || currentResource.text != oldValue) {
                        needExport = true
                        continue
                    }
                    if (!isEmpty(strValue)) {
                        val localizedRes = StringResource(strName, strValue!!)
                        val validation = validator.validateString(sheetLocale, currentResource, localizedRes)
                        if (validation != null) {
                            println("[ERROR] $validation")
                            parseSuccess = false
                            continue
                        }
                        localizedStrings.strings.add(StringResource(strName, strValue))
                    }
                }
                if (enStringSet.isNotEmpty()) {
                    needExport = true
                }
            }
        }

        if (!parseSuccess) {
            error("Parsing of excel file failed! See above errors")
        } else {
            println("Successfully processed: ${localizationsOut.keys.joinToString()}")
        }

        if (needExport) {
            println("\nBase strings.xml has changed!")
        }

        for (locale in supportedLocales) {
            if (!localizationsOut.containsKey(locale.identifier)) {
                // There's a new locale!
                needExport = true
                print("\nNew supported locale ${locale.identifier} detected!")
            }
        }

        println("\n")

        return needExport
    }
}
