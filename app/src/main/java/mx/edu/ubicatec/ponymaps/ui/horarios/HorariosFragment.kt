package mx.edu.ubicatec.ponymaps.ui.horarios

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import mx.edu.ubicatec.ponymaps.R
import mx.edu.ubicatec.ponymaps.databinding.FragmentHorariosBinding
import mx.edu.ubicatec.ponymaps.models.horarios.HorarioAdapter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import mx.edu.ubicatec.ponymaps.models.horarios.Horario
import java.io.IOException

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
        //val horariosViewModel = ViewModelProvider(this).get(HorariosViewModel::class.java)

        _binding = FragmentHorariosBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.motionBaseHor.transitionToEnd()

        /** Spinners setup */

        setSpinners()

        /** Button on click */

        binding.buttonBuscarHorario.setOnClickListener {
            val salon = binding.spinnerSalon.selectedItem.toString()
            val dia = binding.spinnerDia.selectedItem.toString()

            setRecyclerView(salon, dia)
            binding.motionBaseHor.transitionToStart()
        }

        binding.btnAbrirSelectorHorario.setOnClickListener {
            binding.motionBaseHor.transitionToEnd()
        }

        /** SETS GPS */

        return root
    }

    fun setSpinners(){

        val edifs = arrayOf("F", "K")
        var salones = arrayListOf<String>("")
        val dias = arrayOf("Lunes", "Martes", "Miercoles", "Jueves", "Viernes")

        val spinnerEdificio: Spinner = binding.spinnerEdificio
        var spinnerSalon: Spinner = binding.spinnerSalon
        val spinnerDia: Spinner = binding.spinnerDia

        val ada1 = ArrayAdapter(requireContext(), R.layout.custom_spinner_item, edifs)
        val ada3 = ArrayAdapter(requireContext(), R.layout.custom_spinner_item, dias)

        ada1.setDropDownViewResource(R.layout.custom_spinner_dropdown)
        ada3.setDropDownViewResource(R.layout.custom_spinner_dropdown)

        spinnerEdificio.adapter = ada1
        spinnerDia.adapter = ada3

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
                var ada2 = ArrayAdapter(requireContext(), R.layout.custom_spinner_item, salones)
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

        val recyclerView = binding.recyclerHorarios
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = HorarioAdapter(y)
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