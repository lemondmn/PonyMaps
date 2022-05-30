package mx.edu.ubicatec.ponymaps.ui.ubicaciones

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import mx.edu.ubicatec.ponymaps.databinding.FragmentUbicacionesBinding
import mx.edu.ubicatec.ponymaps.models.ubicacion.UbicacionAdapter
import mx.edu.ubicatec.ponymaps.models.ubicacion.UbicacionProvider

class UbicacionesFragment : Fragment() {

    private var _binding: FragmentUbicacionesBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val ubicacionesViewModel = ViewModelProvider(this).get(UbicacionesViewModel::class.java)

        _binding = FragmentUbicacionesBinding.inflate(inflater, container, false)
        val root: View = binding.root

        initRecyclerView()

        return root
    }

    fun initRecyclerView(){
        val recyclerView = binding.recyclerUbicaciones
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = UbicacionAdapter(UbicacionProvider.ubicacionesList)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}