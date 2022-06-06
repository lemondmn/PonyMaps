package mx.edu.ubicatec.ponymaps.models.horarios

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import mx.edu.ubicatec.ponymaps.R

class HorarioAdapter(private val horarioList: List<Horario>): RecyclerView.Adapter<HorarioViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HorarioViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)

        return HorarioViewHolder(layoutInflater.inflate(R.layout.item_horario, parent, false))
    }

    override fun onBindViewHolder(holder: HorarioViewHolder, position: Int) {
        val item = horarioList[position]
        holder.setData(item)
    }

    override fun getItemCount(): Int = horarioList.size

}