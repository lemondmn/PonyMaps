package mx.edu.ubicatec.ponymaps.ui.eventos

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import mx.edu.ubicatec.ponymaps.databinding.FragmentEventosBinding

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

        val textView: TextView = binding.textViewEve
        eventosViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}