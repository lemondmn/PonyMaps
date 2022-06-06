package mx.edu.ubicatec.ponymaps.models.horarios

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import mx.edu.ubicatec.ponymaps.R

class HorarioViewHolder(view: View): RecyclerView.ViewHolder(view) {

    val materia = view.findViewById<TextView>(R.id.materiaName)
    val docente = view.findViewById<TextView>(R.id.docente)
    val horaEntrada = view.findViewById<TextView>(R.id.hEntrada)
    val horaSalida = view.findViewById<TextView>(R.id.hSalida)

    fun setData(horarioModel: Horario){
        materia.text = horarioModel.nombreMateria
        docente.text = horarioModel.nombreDocente
        horaEntrada.text = horarioModel.horaEntrada
        horaSalida.text = horarioModel.horaSalida
    }
}