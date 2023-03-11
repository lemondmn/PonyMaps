package mx.edu.ubicatec.ponymaps.models.eventos

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import mx.edu.ubicatec.ponymaps.R
import mx.edu.ubicatec.ponymaps.models.ubicacion.Ubicacion

abstract class EventoAdapter(private var eventoList: List<Evento>) : RecyclerView.Adapter<EventoAdapter.EventoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventoAdapter.EventoViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return EventoViewHolder(layoutInflater.inflate(R.layout.item_evento, parent, false))
    }

    override fun onBindViewHolder(holder: EventoViewHolder, position: Int) {
        val item = eventoList[position]
        holder.setData(item)
    }

    override fun getItemCount(): Int = eventoList.size

    inner class EventoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        //val img = view.findViewById<ImageView>(R.id.imageView_eventos)
        val nombreEvento = itemView.findViewById<TextView>(R.id.EventoName)
        val descripcionEvento = itemView.findViewById<TextView>(R.id.EventoInfo)
        val ubicacionEvento = itemView.findViewById<TextView>(R.id.EventoUbicacion)
        val fechaEvento = itemView.findViewById<TextView>(R.id.EventoFecha)
        val horaEvento = itemView.findViewById<TextView>(R.id.EventoHora)

        fun setData(eventoModel: Evento){
            nombreEvento.text = eventoModel.nombreEvento
            descripcionEvento.text = eventoModel.infoEvento
            ubicacionEvento.text = eventoModel.ubicEvento
            fechaEvento.text = eventoModel.fechaEvento
            horaEvento.text = eventoModel.horarioEvento

            itemView.setOnClickListener {
                sendEvento(eventoModel.eventoID)
            }
        }
    }

    abstract fun sendEvento(id : Int)

    fun getEventos(): List<Evento> {
        return eventoList
    }
    fun updateEventos(newEventos: List<Evento>){
        eventoList = newEventos
        notifyDataSetChanged()
    }

}

