package com.adp.appsilvant.data

import java.time.LocalDate

enum class TimelineType { VIAJE, REGALO }

/**
 * Modelo unificado para la línea de tiempo de la pantalla principal.
 * Contiene el tipo (Viaje o Regalo), la fecha para ordenar y la entidad asociada.
 */
 data class TimelineItem(
    val type: TimelineType,
    val fecha: LocalDate,
    val viaje: Viaje? = null,
    val regalo: Regalo? = null
)
