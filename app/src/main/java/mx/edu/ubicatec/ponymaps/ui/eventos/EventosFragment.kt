package mx.edu.ubicatec.ponymaps.ui.eventos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import mx.edu.ubicatec.ponymaps.databinding.FragmentEventosBinding
import mx.edu.ubicatec.ponymaps.models.eventos.Evento
import mx.edu.ubicatec.ponymaps.models.eventos.EventoAdapter
import mx.edu.ubicatec.ponymaps.models.eventos.EventoProvider

class EventosFragment : Fragment() {

    private var _binding: FragmentEventosBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val eventosViewModel = ViewModelProvider(this).get(EventosViewModel::class.java)

        _binding = FragmentEventosBinding.inflate(inflater, container, false)
        val root: View = binding.root

        initRecyclerView()

        return root
    }

    fun initRecyclerView(){
        val recyclerView = binding.recyclerEventos
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = EventoAdapter(EventoProvider.eventoList)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}