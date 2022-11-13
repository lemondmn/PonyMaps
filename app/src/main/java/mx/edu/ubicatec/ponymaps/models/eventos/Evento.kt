package mx.edu.ubicatec.ponymaps.models.eventos

data class Evento (
    val linkImgEvento : String,
    val eventoID : Int,
    val nombreEvento : String,
    val infoEvento : String,
    val ubicEvento : String,
    val fechaEvento : String,
    val horarioEvento : String
    )