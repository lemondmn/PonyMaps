package mx.edu.ubicatec.ponymaps.models.horarios

data class Horario (
    val nombreSalon: String,
    val nombreMateria: String,
    val nombreDocente: String,
    val horaEntrada: String,
    val horaSalida: String,
    val dia: String
    )