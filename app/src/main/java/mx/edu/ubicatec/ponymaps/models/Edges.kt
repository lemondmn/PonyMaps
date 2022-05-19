package mx.edu.ubicatec.ponymaps.models

import java.io.Serializable

data class Edges(
    val origen: String,
    val destino: String,
    val dist: Int

) : Serializable
