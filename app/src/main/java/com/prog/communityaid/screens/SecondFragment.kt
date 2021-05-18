package com.prog.communityaid.screens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.prog.communityaid.R
import com.prog.communityaid.data.models.Complaint

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

/**
 * A simple [Fragment] subclass.
 * Use the [SecondFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SecondFragment(private var complaint: Complaint) : Fragment(), OnMapReadyCallback {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_more_second, container, false)
        val polNumber = view.findViewById<EditText>(R.id.poleNumberMore)
        val pumpNumber = view.findViewById<EditText>(R.id.pumpNumberMore)
        val meterNumber = view.findViewById<EditText>(R.id.meterNumberMore)
        val map = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        map.getMapAsync(this)
        if (complaint.complaintType == "ecg") {
            pumpNumber.visibility = View.GONE
            polNumber.visibility = View.VISIBLE
            meterNumber.visibility = View.VISIBLE
            polNumber.setText(complaint.complaintInfo["poleNumber"])
            meterNumber.setText(complaint.complaintInfo["meterNumber"])
        } else if (complaint.complaintType == "gwc") {
            pumpNumber.visibility = View.VISIBLE
            polNumber.visibility = View.GONE
            meterNumber.visibility = View.GONE
            pumpNumber.setText(complaint.complaintInfo["pumpNumber"])
        } else {
            pumpNumber.visibility = View.VISIBLE
            polNumber.visibility = View.GONE
            meterNumber.visibility = View.GONE
            pumpNumber.setText(complaint.complaintInfo["roadName"])
        }

        return view
    }

    override fun onMapReady(map: GoogleMap) {
        val position = LatLng(complaint.lat as Double, complaint.long as Double)
        map.addMarker(MarkerOptions().also {
            it.position(position)
            it.title("Complaint Location")
        })
        map.moveCamera(CameraUpdateFactory.newLatLng(position))
        map.animateCamera(CameraUpdateFactory.zoomTo(17.0F))
    }

}