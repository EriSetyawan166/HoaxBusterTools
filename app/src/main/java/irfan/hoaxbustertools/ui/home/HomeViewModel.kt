package irfan.hoaxbustertools.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomeViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Tool Favorit Kamu Masih Kosong"
    }
    val text: LiveData<String> = _text

    private val _secondText = MutableLiveData<String>().apply {
        value = "Tambah Tool ke Favorit agar Tool bisa kamu gunakan dari halaman depan"
    }
    val secondText: LiveData<String> = _secondText
}