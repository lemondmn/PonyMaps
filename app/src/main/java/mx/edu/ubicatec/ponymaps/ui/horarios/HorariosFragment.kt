package mx.edu.ubicatec.ponymaps.ui.horarios

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import mx.edu.ubicatec.ponymaps.databinding.FragmentHorariosBinding

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

        val textView: TextView = binding.textViewHor
        horariosViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}