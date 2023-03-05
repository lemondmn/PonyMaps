package mx.edu.ubicatec.ponymaps.models.ubicacion

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.custom_spinner_dropdown.view.*
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
        val areas = itemView.findViewById<TextView>(R.id.SubAreas)
        val titleAreas = itemView.findViewById<TextView>(R.id.AreasTitle)

        fun setData(ubicacionModel: Ubicacion) {
            var areasText = ""
            nombre.text = ubicacionModel.nombre
            info.text = ubicacionModel.informacion

            if (
                ubicacionModel.areas.isEmpty() ||
                ubicacionModel.areas[0] == ""
            ) titleAreas.text = ""
            else
                ubicacionModel.areas.forEach{
                    areasText += "- $it\n"
                }
            areas.text = areasText

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