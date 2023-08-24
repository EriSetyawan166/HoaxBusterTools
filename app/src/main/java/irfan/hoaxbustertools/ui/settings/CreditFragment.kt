package irfan.hoaxbustertools.ui.settings

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import irfan.hoaxbustertools.R
import irfan.hoaxbustertools.SettingsActivity

class CreditFragment : Fragment() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_credit, container, false)

        // Set the title of the action bar to "Kredit"
        (activity as? SettingsActivity)?.setActionBarTitle("Kredit")

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()

        // Reset the title of the action bar to "Pengaturan"
        (activity as? SettingsActivity)?.setActionBarTitle("Pengaturan")
    }








}