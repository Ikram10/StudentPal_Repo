@file:Suppress("DEPRECATION")

package com.example.studentpal.common.utils

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.AsyncTask
import java.util.*
/**
 * A helper class, responsible for converting the latitude and longitude values into an Address format.
 *
 * The code displayed was reused from Denis Panjuta's Happy Place application (Panjuta,2021) (see references file)
 *
 * @see[com.example.studentpal.common.References]
 */

class GetAddressFromLatLng(
    context: Context,
    private val latitude: Double,
    private val longitude: Double
) : AsyncTask<Void, String, String>() {

    /**
     * Constructs a Geocoder whose responses will be localized for the given Locale.
     *
     * @param context the Context of the calling Activity
     * @param locale the desired Locale for the query results
     *
     * @throws NullPointerException if Locale is null
     */
    private val geoCoder: Geocoder = Geocoder(context, Locale.getDefault())

    // A variable for address listener interface
    private lateinit var mAddressListener: AddressListener

    /**
     * A background method of AsyncTask where the background operation will be performed
     */
    @Deprecated("Deprecated in Java")
    override fun doInBackground(vararg p0: Void?): String {

        try {

            /**
             * Returns an array of Addresses that are known to describe the area immediately surrounding the given latitude and longitude.
             */
            val addressList: List<Address>? =
                geoCoder.getFromLocation(latitude, longitude, 1)

            if (addressList != null && addressList.isNotEmpty()) {
                val address: Address = addressList[0]
                val stringBuilder = StringBuilder()

                for (i in 0..address.maxAddressLineIndex) {
                    stringBuilder.append(address.getAddressLine(i)).append(" ")
                }

                // Here we remove the last comma that we have added above from the address.
                stringBuilder.deleteCharAt(stringBuilder.length - 1)
                return stringBuilder.toString()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }

    /**
     * onPostExecute method of AsyncTask where the result will be received and assigned to the interface accordingly.
     */
    @Deprecated("Deprecated in Java")
    override fun onPostExecute(resultString: String?) {
       if (resultString == null){
           mAddressListener.onError()
       } else {
           mAddressListener.onAddressFound(resultString)
       }
        super.onPostExecute(resultString)
    }

    /**
     *  A public function to set the AddressListener.
     */
    fun setAddressListener(addressListener: AddressListener){
        mAddressListener = addressListener
    }

    /**
     * A public function to execute the AsyncTask from the class is it called.
     */
    fun getAddress(){
        execute()
    }

    /**
     * A interface for AddressListener which contains the function like success and error.
     */
    interface AddressListener {
        fun onAddressFound(address: String?)

        fun onError()
    }
}