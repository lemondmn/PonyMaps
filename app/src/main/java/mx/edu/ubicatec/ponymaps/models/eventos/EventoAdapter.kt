package mx.edu.ubicatec.ponymaps.models.eventos

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import mx.edu.ubicatec.ponymaps.R

class EventoAdapter(private val eventoList: List<Evento>) : RecyclerView.Adapter<EventoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventoViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return EventoViewHolder(layoutInflater.inflate(R.layout.item_evento, parent, false))
    }

    override fun onBindViewHolder(holder: EventoViewHolder, position: Int) {
        val item = eventoList[position]

        holder.setData(item)
    }

    override fun getItemCount(): Int = eventoList.size

}