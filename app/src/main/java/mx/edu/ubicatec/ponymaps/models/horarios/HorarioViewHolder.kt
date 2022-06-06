package mx.edu.ubicatec.ponymaps.models.horarios

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import mx.edu.ubicatec.ponymaps.R

class HorarioViewHolder(view: View): RecyclerView.ViewHolder(view) {

    val materia = view.findViewById<TextView>(R.id.materiaName)
    val docente = view.findViewById<TextView>(R.id.docente)
    val horaEntradaSalida = view.findViewById<TextView>(R.id.hEntradaSalida)

    fun setData(horarioModel: Horario){
        val horas = horarioModel.horaEntrada + " - " + horarioModel.horaSalida
        materia.text = horarioModel.nombreMateria
        docente.text = horarioModel.nombreDocente
        horaEntradaSalida.text = horas
    }
}