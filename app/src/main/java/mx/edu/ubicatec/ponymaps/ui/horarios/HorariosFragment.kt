package mx.edu.ubicatec.ponymaps.ui.horarios

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.android.volley.RequestQueue
import kotlinx.android.synthetic.main.fragment_horarios.view.*
import mx.edu.ubicatec.ponymaps.databinding.FragmentHorariosBinding
import mx.edu.ubicatec.ponymaps.utils.volleyUtils
import org.json.JSONObject

class HorariosFragment : Fragment() {

    private var _binding: FragmentHorariosBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val horariosViewModel = ViewModelProvider(this).get(HorariosViewModel::class.java)

        _binding = FragmentHorariosBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val btninsertar : Button = root.btninsertar
        val btnconsulta : Button = root.btnconsulta
        val horaEI : TextView = root.horaEI
        val horaSI : TextView = root.horaSI
        val horaEC : TextView = root.horaEC
        val horaSC : TextView = root.horaSC
        val idhorario : TextView = root.idhorario
        val requestqueue : RequestQueue

        btnconsulta.setOnClickListener {
            try {
                val id = "${idhorario.text.toString()}"
                val url = "https://ponymaps.000webhostapp.com/pony/consulta.php?idHorario=${id}" //URL QUE SE ENVIA PARA HACER LA CONSULTA
                object : volleyUtils(){
                    override fun formatResponse(response: String) {
                        response.get(0)
                        var json : JSONObject
                        json = JSONObject(response)
                        var he : String
                        var hs : String
                        he = json.getString("horaEntrada") // EN VARIABLE he SE GUARDA LO DATOS DE HORA ENTRADA
                        hs = json.getString("horaSalida") // EN VARIABLE hs SE GUARDA LO DATOS DE HORA SALIDA
                        horaEI.text = he //SE MUESTRAN EN PANTALLA
                        horaSI.text = hs
                        println(json)
                    }
                }.consumeGet(requireContext(), url)
            }
            catch (e: Exception){
                e.printStackTrace()
            }
        }

        /*
        val textView: TextView = binding.textViewHor
        horariosViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        } */
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}