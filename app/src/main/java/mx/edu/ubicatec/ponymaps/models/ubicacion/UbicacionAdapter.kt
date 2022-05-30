package mx.edu.ubicatec.ponymaps.models.ubicacion

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import mx.edu.ubicatec.ponymaps.R

class UbicacionAdapter (private val ubicacionList : List<Ubicacion>) : RecyclerView.Adapter<UbicacionViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UbicacionViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return UbicacionViewHolder(layoutInflater.inflate(R.layout.item_ubicacion, parent, false))
    }

    override fun onBindViewHolder(holder: UbicacionViewHolder, position: Int) {
        val item = ubicacionList[position]
        holder.setData(item)
    }

    override fun getItemCount(): Int = ubicacionList.size

}