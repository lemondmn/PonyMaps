package mx.edu.ubicatec.ponymaps.models.ubicacion

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import mx.edu.ubicatec.ponymaps.R

abstract class UbicacionAdapter (private var ubicacionList : List<Ubicacion>) : RecyclerView.Adapter<UbicacionAdapter.UbicacionViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UbicacionViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return UbicacionViewHolder(layoutInflater.inflate(R.layout.item_ubicacion, parent, false))
    }

    override fun onBindViewHolder(holder: UbicacionViewHolder, position: Int) {
        val item = ubicacionList[position]
        holder.setData(item)
    }

    override fun getItemCount(): Int = ubicacionList.size

    inner class UbicacionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val nombre = itemView.findViewById<TextView>(R.id.UbicacionName)
        val info = itemView.findViewById<TextView>(R.id.UbicacionInfo)

        fun setData(ubicacionModel: Ubicacion) {
            nombre.text = ubicacionModel.nombre
            info.text = ubicacionModel.informacion

            itemView.setOnClickListener {
                sendUbicacion(ubicacionModel.nombre)
            }
        }
    }

    abstract fun sendUbicacion(nombre : String)

    fun getUbications(): List<Ubicacion> {
        return ubicacionList
    }
    fun updateUbications(newUbications: List<Ubicacion>){
        ubicacionList = newUbications
        notifyDataSetChanged()
    }

}