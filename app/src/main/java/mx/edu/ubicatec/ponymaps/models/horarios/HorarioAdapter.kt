package mx.edu.ubicatec.ponymaps.models.horarios

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import mx.edu.ubicatec.ponymaps.R

abstract class HorarioAdapter(private val horarioList: List<Horario>): RecyclerView.Adapter<HorarioAdapter.HorarioViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HorarioViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return HorarioViewHolder(layoutInflater.inflate(R.layout.item_horario, parent, false))
    }

    override fun onBindViewHolder(holder: HorarioViewHolder, position: Int) {
        val item = horarioList[position]
        holder.setData(item)
    }

    override fun getItemCount(): Int = horarioList.size

    inner class HorarioViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

        val materia = itemView.findViewById<TextView>(R.id.materiaName)
        val docente = itemView.findViewById<TextView>(R.id.docente)
        val horaEntradaSalida = itemView.findViewById<TextView>(R.id.hEntradaSalida)

        fun setData(horarioModel: Horario){
            val horas = horarioModel.horaEntrada + " - " + horarioModel.horaSalida
            materia.text = horarioModel.nombreMateria
            docente.text = horarioModel.nombreDocente
            horaEntradaSalida.text = horas

            itemView.setOnClickListener {
                sendHorario(horarioModel.nombreSalon)
            }
        }
    }

    abstract fun sendHorario(salon: String)

}