package com.adp.appsilvant.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FotoRegalo(
    val id: Long = 0,
    @SerialName("regalo_id")
    val regaloId: Long,
    @SerialName("url_foto")
    val urlFoto: String,
    @SerialName("descripcion_foto")
    val descripcionFoto: String? = null,
    @SerialName("creado_en")
    val creadoEn: String? = null
)
