package com.adp.appsilvant.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FotoViaje(
    val id: Long = 0,
    @SerialName("viaje_id")
    val viajeId: Long? = null,
    @SerialName("url_foto")
    val urlFoto: String? = null,
    @SerialName("descripcion_foto")
    val descripcionFoto: String? = null,
    @SerialName("creado_en")
    val creadoEn: String? = null
)
