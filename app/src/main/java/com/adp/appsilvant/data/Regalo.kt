package com.adp.appsilvant.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Regalo(
    val id: Long = 0,
    @SerialName("nombre_regalo")
    val nombreRegalo: String,
    val fecha: String? = null,
    val descripcion: String? = null,
    val tipo: String? = null,
    @SerialName("creado_en")
    val creadoEn: String? = null,
    @SerialName("fotos_regalos")
    val fotos: List<FotoRegalo> = emptyList()
)
