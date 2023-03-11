package mx.edu.ubicatec.ponymaps.models.ubicacion

data class Ubicacion(
    val id: String,
    val nombre: String,
    val informacion: String,
    val latitud: String,
    val longitud: String,
    val areas: ArrayList<String>,
    var visibility: Boolean
)

/*public class Ubicacion(nom: String, info: String){
    val nombre: String = nom
    val informacion: String = info

}*/