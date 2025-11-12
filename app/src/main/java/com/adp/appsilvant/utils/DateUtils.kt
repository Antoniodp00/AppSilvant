// Archivo: app/src/main/java/com/adp/appsilvant/utils/DateUtils.kt
package com.adp.appsilvant.utils

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

// Convierte el String "AAAA-MM-DD" de tu BBDD a un objeto de fecha
fun parseDate(fechaString: String?): LocalDate? {
    if (fechaString.isNullOrBlank()) return null
    return try {
        LocalDate.parse(fechaString, DateTimeFormatter.ISO_LOCAL_DATE)
    } catch (e: DateTimeParseException) {
        null // Si el formato es incorrecto, lo trata como nulo
    }
}