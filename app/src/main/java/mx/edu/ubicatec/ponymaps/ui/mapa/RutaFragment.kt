package mx.edu.ubicatec.ponymaps.ui.mapa

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.findNavController
import mx.edu.ubicatec.ponymaps.R
import mx.edu.ubicatec.ponymaps.databinding.FragmentRutaBinding

class RutaFragment : DialogFragment() {

    private lateinit var binding: FragmentRutaBinding

    override fun onStart() {
        super.onStart()
        val w = (resources.displayMetrics.widthPixels)
        dialog?.window?.setLayout(w, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        
        binding = FragmentRutaBinding.inflate(inflater, container, false)

        //val rootView: View = inflater.inflate(R.layout.fragment_ruta, container, false)

        binding.btnCerrar.setOnClickListener {
            dismiss()
        }

        val spinnerOrigen: Spinner = binding.spOrigen
        val spinnerDestino: Spinner = binding.spDestino

        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.ubicaciones,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerOrigen.adapter = adapter
            spinnerDestino.adapter = adapter
        }

        binding.buttonRuta.setOnClickListener {
            //Toast.makeText(context, "OK PRESIONADO", Toast.LENGTH_LONG).show()

            val origen = spinnerOrigen.selectedItem.toString()
            val destino = spinnerDestino.selectedItem.toString()

            try {
                val destination = RutaFragmentDirections.sendArgsToMap(origen, destino)
                NavHostFragment.findNavController(this).navigate(destination)
            } catch (e: Exception){
                println("Me lleva la chingada")
                e.printStackTrace()
            }
            //NavHostFragment.findNavController(this).navigate(destination)

            //Toast.makeText(context, origen+destino, Toast.LENGTH_LONG).show()

            dismiss()
        }

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        return binding.root
    }

}