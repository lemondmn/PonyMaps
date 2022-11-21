package mx.edu.ubicatec.ponymaps.ui.mapa

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MapaViewModel : ViewModel (){
    val idEvento = MutableLiveData<Int>()
    val nombreUbicacion = MutableLiveData<String>()
    val nombreSalon = MutableLiveData<String>()
}