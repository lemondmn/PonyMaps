package mx.edu.ubicatec.ponymaps.models.eventos

class EventoProvider {
    companion object {
        val eventoList = listOf<Evento>(
            Evento(
                "Null",
                1,
                "30° Festival Cultural Didáctico",
                "El Departamento de Actividades Extraescolares y su Oficina de Promoción Cultural, invitan a toda la comunidad tecnológica a ser parte del 30° Festival Cultural Didáctico. ",
                "Arco Techo",
                "08 de Junio",
                "13:00 Hrs"
            ),Evento(
                "Null",
                2,
                "Coloquio Virtual",
                "Procesamiento de Lenguaje natural aplicado a la inteligencia Artificial ¿Pueden escribirse frases literarias aunque sean artificiales?",
                "Zoom | Facebook | YouTube",
                "03 de Junio",
                "12:00 Hrs"
            )
        )
    }
}