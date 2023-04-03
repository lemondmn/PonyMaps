package mx.edu.ubicatec.ponymaps.models.ubicacion

import java.io.Serializable

data class Nodo(
    val nombre: String,
    val informacion: String,
    val lat: Double,
    val lng: Double,
    var id_view: Int?,
    var id: String?

) : Serializable
