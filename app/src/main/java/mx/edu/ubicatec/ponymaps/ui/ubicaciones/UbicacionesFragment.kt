package mx.edu.ubicatec.ponymaps.ui.ubicaciones

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_ubicaciones.*
import mx.edu.ubicatec.ponymaps.R
import mx.edu.ubicatec.ponymaps.databinding.FragmentUbicacionesBinding
import mx.edu.ubicatec.ponymaps.models.Classes.DataCard
import mx.edu.ubicatec.ponymaps.models.ubicacion.UbicacionAdapter
import mx.edu.ubicatec.ponymaps.models.ubicacion.UbicacionProvider
import mx.edu.ubicatec.ponymaps.ui.mapa.MapaViewModel

private var saveAdapter: UbicacionAdapter? = null

class UbicacionesFragment : Fragment() {

    private var _binding: FragmentUbicacionesBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var mapaViewModel : MapaViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val ubicacionesViewModel = ViewModelProvider(this).get(UbicacionesViewModel::class.java)

        _binding = FragmentUbicacionesBinding.inflate(inflater, container, false)
        val root: View = binding.root

        mapaViewModel = ViewModelProvider(requireActivity()).get(MapaViewModel::class.java)

        initRecyclerView()

        return root
    }

    fun initRecyclerView(){
        val recyclerView = binding.recyclerUbicaciones
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        saveAdapter = object : UbicacionAdapter(UbicacionProvider.ubicacionesList) {
            override fun sendUbicacion(nombre: String) {
                mapaViewModel.nombreUbicacion.postValue(nombre)
                findNavController().navigate(R.id.action_na_fragment_ubicaciones_to_na_fragment_map)
            }
        }
        recyclerView.adapter = saveAdapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun updateRecyclerView(textSubmited: String){
        var filterList = ArrayList<DataCard>()

        if (saveAdapter != null) {
            Log.d("SearchList", "Checking Cards")
            /*adapter?.cards?.forEach {card ->
                if (
                    card.getTitle().uppercase().contains(textSubmited.uppercase()) ||
                    card.getDetail().uppercase().contains(textSubmited.uppercase())
                ) {
                    filterList.add(card)
                    Log.d("SearchList", card.getTitle())
                }
            }
            adapter?.updateCards(filterList)*/
        }else{ Log.d("SearchList", "No init") }
    }
}