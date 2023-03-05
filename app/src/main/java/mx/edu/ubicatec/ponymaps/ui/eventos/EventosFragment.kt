package mx.edu.ubicatec.ponymaps.ui.eventos

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import mx.edu.ubicatec.ponymaps.R
import mx.edu.ubicatec.ponymaps.databinding.FragmentEventosBinding
import mx.edu.ubicatec.ponymaps.models.eventos.Evento
import mx.edu.ubicatec.ponymaps.models.eventos.EventoAdapter
import mx.edu.ubicatec.ponymaps.models.eventos.EventoProvider
import mx.edu.ubicatec.ponymaps.models.ubicacion.Ubicacion
import mx.edu.ubicatec.ponymaps.models.ubicacion.UbicacionAdapter
import mx.edu.ubicatec.ponymaps.models.ubicacion.UbicacionProvider
import mx.edu.ubicatec.ponymaps.ui.mapa.MapaViewModel
import mx.edu.ubicatec.ponymaps.ui.eventos.eventoAdapter

private var eventoAdapter: EventoAdapter? = null

class EventosFragment : Fragment() {

    private var _binding: FragmentEventosBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var mapaViewModel : MapaViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val eventosViewModel = ViewModelProvider(this).get(EventosViewModel::class.java)

        _binding = FragmentEventosBinding.inflate(inflater, container, false)
        val root: View = binding.root

        mapaViewModel = ViewModelProvider(requireActivity()).get(MapaViewModel::class.java)

        initRecyclerView()

        return root
    }

    fun initRecyclerView(){
        val recyclerView = binding.recyclerEventos
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        eventoAdapter = object : EventoAdapter(EventoProvider.eventoList) {
            override fun sendEvento(id: Int) {
                mapaViewModel.idEvento.postValue(id)
                findNavController().navigate(R.id.action_na_fragment_eventos_to_na_fragment_map)
            }
        }
        recyclerView.adapter = eventoAdapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun resetList(){
        eventoAdapter?.updateEventos(EventoProvider.eventoList)
    }

    fun updateRecyclerView(textSubmited: String){
        var filterList = ArrayList<Evento>()

        if (eventoAdapter != null) {
            Log.d("SearchList", "Checking Cards")
            eventoAdapter?.getEventos()?.forEach {
                if (
                    it.nombreEvento.uppercase().contains(textSubmited.uppercase()) ||
                    it.infoEvento.uppercase().contains(textSubmited.uppercase()) ||
                    it.ubicEvento.uppercase().contains(textSubmited.uppercase()) ||
                    it.fechaEvento.uppercase().contains(textSubmited.uppercase()) ||
                    it.horarioEvento.uppercase().contains(textSubmited.uppercase())
                ) {
                    filterList.add(it)
                    Log.d("SearchList", it.nombreEvento)
                }
            }
            eventoAdapter?.updateEventos(filterList)
        }else{ Log.d("SearchList", "No init") }
    }
}