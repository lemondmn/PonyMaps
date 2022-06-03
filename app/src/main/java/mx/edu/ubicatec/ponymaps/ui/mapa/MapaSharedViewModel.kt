package mx.edu.ubicatec.ponymaps.ui.mapa

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MapaSharedViewModel: ViewModel() {

    val message = MutableLiveData<String>()

    fun sendData(u: String){
        message.value = u
    }
}