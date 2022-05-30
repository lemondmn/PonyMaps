package mx.edu.ubicatec.ponymaps.models.eventos

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import mx.edu.ubicatec.ponymaps.R

class EventoViewHolder(view : View) : RecyclerView.ViewHolder (view){

    //val img = view.findViewById<ImageView>(R.id.imageView_eventos)
    val nombreEvento = view.findViewById<TextView>(R.id.EventoName)
    val descripcionEvento = view.findViewById<TextView>(R.id.EventoDescripcion)
    val ubicacionEvento = view.findViewById<TextView>(R.id.EventoUbicacion)
    val fechaEvento = view.findViewById<TextView>(R.id.EventoFecha)
    val horaEvento = view.findViewById<TextView>(R.id.EventoHora)

    fun setData(eventoModel: Evento){
        nombreEvento.text = eventoModel.nombreEvento
        descripcionEvento.text = eventoModel.infoEvento
        ubicacionEvento.text = eventoModel.ubicEvento
        fechaEvento.text = eventoModel.fechaEvento
        horaEvento.text = eventoModel.horarioEvento
    }

}