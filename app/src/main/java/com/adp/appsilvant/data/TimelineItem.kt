// Archivo: app/src/main/java/com/adp/appsilvant/data/TimelineItem.kt
package com.adp.appsilvant.data

import java.time.LocalDate

// Esta clase nos permite meter Viajes y Regalos en una misma lista
sealed class TimelineItem {
    // Todos los items de la línea de tiempo DEBEN tener una fecha para ordenarse
    abstract val fecha: LocalDate?

    data class ViajeItem(val viaje: Viaje, override val fecha: LocalDate?) : TimelineItem()
    data class RegaloItem(val regalo: Regalo, override val fecha: LocalDate?) : TimelineItem()
}