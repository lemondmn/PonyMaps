package mx.edu.ubicatec.ponymaps.ui.eventos

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class EventosViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Eventos"
    }
    val text: LiveData<String> = _text
}