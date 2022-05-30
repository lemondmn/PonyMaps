package mx.edu.ubicatec.ponymaps.models.ubicacion

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import mx.edu.ubicatec.ponymaps.R

class UbicacionViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    val nombre = view.findViewById<TextView>(R.id.UbicacionName)
    val info = view.findViewById<TextView>(R.id.UbicacionInfo)

    fun setData(ubicacionModel: Ubicacion) {
        nombre.text = ubicacionModel.nombre
        info.text = ubicacionModel.informacion
    }
}