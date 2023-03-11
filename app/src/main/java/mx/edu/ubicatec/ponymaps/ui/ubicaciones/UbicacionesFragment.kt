package mx.edu.ubicatec.ponymaps.ui.ubicaciones

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import mx.edu.ubicatec.ponymaps.MainActivity
import mx.edu.ubicatec.ponymaps.R
import mx.edu.ubicatec.ponymaps.databinding.FragmentUbicacionesBinding
import mx.edu.ubicatec.ponymaps.models.Classes.AtlasConnection
import mx.edu.ubicatec.ponymaps.models.ubicacion.Ubicacion
import mx.edu.ubicatec.ponymaps.models.ubicacion.UbicacionAdapter
import mx.edu.ubicatec.ponymaps.models.ubicacion.UbicacionProvider
import mx.edu.ubicatec.ponymaps.ui.mapa.MapaViewModel

private var ubicacionAdapter: UbicacionAdapter? = null

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

        requireActivity().invalidateOptionsMenu()

        return root
    }

    fun initRecyclerView(){
        val recyclerView = binding.recyclerUbicaciones
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        ubicacionAdapter = object : UbicacionAdapter(UbicacionProvider.ubicacionesList) {
            override fun sendUbicacion(nombre: String) {
                mapaViewModel.nombreUbicacion.postValue(nombre)
                findNavController().navigate(R.id.action_na_fragment_ubicaciones_to_na_fragment_map)
            }
        }
        recyclerView.adapter = ubicacionAdapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun resetList(){
        ubicacionAdapter?.updateUbications(UbicacionProvider.ubicacionesList)
    }

    fun updateRecyclerView(textSubmited: String){
        var filterList = ArrayList<Ubicacion>()

        if (ubicacionAdapter != null) {
            Log.d("SearchList", "Checking Cards")
            ubicacionAdapter?.getUbications()?.forEach {
                if (
                    it.nombre.uppercase().contains(textSubmited.uppercase()) ||
                    it.informacion.uppercase().contains(textSubmited.uppercase())
                ) {
                    filterList.add(it)
                    Log.d("SearchList", it.nombre)
                }
                else {
                    it.areas.forEach {area ->
                        if (
                            area.uppercase().contains(textSubmited.uppercase()) &&
                            !filterList.contains(it)
                        ){
                            filterList.add(it)
                            Log.d("SearchList", area)
                        }
                    }
                }
            }
            ubicacionAdapter?.updateUbications(filterList)
        }else{ Log.d("SearchList", "No init") }
    }

}