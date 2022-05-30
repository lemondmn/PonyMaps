package mx.edu.ubicatec.ponymaps.ui.mapa

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import mx.edu.ubicatec.ponymaps.R
import kotlinx.android.synthetic.main.fragment_ruta.view.*

class RutaFragment : DialogFragment() {

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
        val rootView: View = inflater.inflate(R.layout.fragment_ruta, container, false)

        rootView.btnCerrar.setOnClickListener {
            dismiss()
        }

        rootView.button_ruta.setOnClickListener {
            Toast.makeText(context, "OK PRESIONADO", Toast.LENGTH_LONG).show()
            dismiss()
        }

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        return rootView
    }

}