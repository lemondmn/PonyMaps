package mx.edu.ubicatec.ponymaps.ui.horarios

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import mx.edu.ubicatec.ponymaps.R
import mx.edu.ubicatec.ponymaps.databinding.FragmentHorariosBinding
import mx.edu.ubicatec.ponymaps.models.horarios.HorarioAdapter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import mx.edu.ubicatec.ponymaps.models.horarios.Horario
import mx.edu.ubicatec.ponymaps.ui.mapa.MapaViewModel
import java.io.IOException

class HorariosFragment : Fragment() {

    private var _binding: FragmentHorariosBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var mapaViewModel : MapaViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        //val horariosViewModel = ViewModelProvider(this).get(HorariosViewModel::class.java)

        _binding = FragmentHorariosBinding.inflate(inflater, container, false)
        val root: View = binding.root

        mapaViewModel = ViewModelProvider(requireActivity()).get(MapaViewModel::class.java)

        /** Spinners setup */

        setSpinners()

        /** Button on click */

        binding.btnDiaL.setOnClickListener {
            val salon = binding.spinnerSalon.selectedItem.toString()

            setRecyclerView(salon, "Lunes")
        }

        binding.btnDiaM.setOnClickListener {
            val salon = binding.spinnerSalon.selectedItem.toString()

            setRecyclerView(salon, "Martes")
        }

        binding.btnDiaX.setOnClickListener {
            val salon = binding.spinnerSalon.selectedItem.toString()

            setRecyclerView(salon, "Miercoles")
        }

        binding.btnDiaJ.setOnClickListener {
            val salon = binding.spinnerSalon.selectedItem.toString()

            setRecyclerView(salon, "Jueves")
        }

        binding.btnDiaV.setOnClickListener {
            val salon = binding.spinnerSalon.selectedItem.toString()

            setRecyclerView(salon, "Viernes")
        }

        binding.btnDiaS.setOnClickListener {
            Toast.makeText(requireContext(), "No hay materias en sabado", Toast.LENGTH_LONG).show()
        }

        return root
    }

    fun setSpinners(){

        val edifs = arrayOf("F", "K")
        var salones = arrayListOf<String>("")

        val spinnerEdificio: Spinner = binding.spinnerEdificio
        val spinnerSalon: Spinner = binding.spinnerSalon

        val ada1 = ArrayAdapter(requireContext(), R.layout.custom_spinner_item, edifs)

        ada1.setDropDownViewResource(R.layout.custom_spinner_dropdown)

        spinnerEdificio.adapter = ada1

        spinnerEdificio.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                when (binding.spinnerEdificio.selectedItem.toString()){
                    "F" -> {
                        salones = arrayListOf<String>("F1", "F2", "F3", "F4", "F5", "F6")
                    }
                    "K" -> {
                        salones =
                            arrayListOf<String>("K1", "K2", "K3", "K4", "K5", "K6", "K7", "K8")
                    }
                }
                val ada2 = ArrayAdapter(requireContext(), R.layout.custom_spinner_item, salones)
                ada2.setDropDownViewResource(R.layout.custom_spinner_dropdown)
                spinnerSalon.adapter = ada2
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                //spinnerEdificio.setSelection(0)
            }
        }

    }

    fun setRecyclerView(salon: String, dia: String){

        val x = readJSON()
        val y = arrayListOf<Horario>()

        for (horario in x){
            if(horario.nombreSalon == salon && horario.dia == dia){
                y.add(horario)
            }
        }

        val recyclerView = binding.recyclerViewHorarios
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = object : HorarioAdapter(y) {
            override fun sendHorario(salon: String) {
                mapaViewModel.nombreSalon.postValue(salon)
                findNavController().navigate(R.id.action_na_fragment_horarios_to_na_fragment_map)
            }
        }
    }

    fun readJSON(): List<Horario> {
        val jsonFileString = getJSON(requireContext(), "horarios_kf.json")
        //Log.i("data", jsonFileString!!)
        val gson = Gson()
        val listHorario = object : TypeToken<List<Horario>>(){}.type
        var horarios: List<Horario> = gson.fromJson(jsonFileString, listHorario)
        //horarios.forEachIndexed { index, horario -> Log.i("data", "> Item $index:\n$horario") }
        return horarios
    }

    fun getJSON(context: Context, fileName: String,): String?{
        val jsonString: String
        try {
            jsonString = context.assets.open(fileName).bufferedReader().use { it.readText() }
        } catch (ioException: IOException) {
            ioException.printStackTrace()
            return null
        }
        return jsonString
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}