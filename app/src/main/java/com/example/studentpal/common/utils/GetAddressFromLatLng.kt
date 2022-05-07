package com.example.studentpal.common.utils

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.AsyncTask
import java.util.*


// This class is helper class, responsible for converting the latitude and longitude values into a readable format
class GetAddressFromLatLng(
    context: Context,
    private val latitude: Double,
    private val longitude: Double
) : AsyncTask<Void, String, String>() {


    private val geoCoder: Geocoder = Geocoder(context, Locale.getDefault())
    private lateinit var mAddressListener: AddressListener

    override fun doInBackground(vararg p0: Void?): String {

        try {
            val addressList: List<Address>? =
                geoCoder.getFromLocation(latitude, longitude, 1)

            if (addressList != null && addressList.isNotEmpty()) {
                val address: Address = addressList[0]
                val stringBuilder = StringBuilder()

                for (i in 0..address.maxAddressLineIndex) {
                    stringBuilder.append(address.getAddressLine(i)).append(" ")
                }

                stringBuilder.deleteCharAt(stringBuilder.length - 1)
                return stringBuilder.toString()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }

    override fun onPostExecute(resultString: String?) {
       if (resultString == null){
           mAddressListener.onError()
       } else {
           mAddressListener.onAddressFound(resultString)
       }
        super.onPostExecute(resultString)
    }

    fun setAddressListener(addressListener: AddressListener){
        mAddressListener = addressListener
    }

    fun getAddress(){
        execute()
    }
    interface AddressListener {
        fun onAddressFound(address: String?)

        fun onError()
    }
}