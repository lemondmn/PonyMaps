package mx.edu.ubicatec.ponymaps.ui.mapa

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MapaViewModel : ViewModel (){
    val ubicacion = MutableLiveData<Int>()
}