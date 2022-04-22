package mx.edu.ubicatec.ponymaps.ui.horarios

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HorariosViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Horario"
    }
    val text: LiveData<String> = _text
}