package com.example.studentpal.view.events

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.studentpal.R
import com.example.studentpal.common.Constants
import com.example.studentpal.common.utils.GetAddressFromLatLng
import com.example.studentpal.databinding.ActivityCreateBoardBinding
import com.example.studentpal.model.entities.Event
import com.example.studentpal.model.remote.EventDatabase.storeEvent
import com.example.studentpal.view.BaseActivity
import com.google.android.gms.location.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class CreateEventActivity : BaseActivity() {
    // GLOBAL VARIABLES
    var binding: ActivityCreateBoardBinding? = null
    private var mSelectedImageFileUri: Uri? = null
    private lateinit var mUserName: String
    private var mBoardImageUrl: String = ""
    private var eventLatitude: Double? = null
    private var eventLongitude: Double? = null
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private var mSelectedDueDateMilliSeconds: Long = 0
    private var mSelectedTime: String = ""


    // Constants for permission code
    companion object {
        private const val PLACE_AUTOCOMPLETE_REQUEST_CODE = 3
    }


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityCreateBoardBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        setupActionBar()


        //retrieves the intent extra by using the name key
        if (intent.hasExtra(Constants.NAME)) {
            //initialises this variable with the user's name that was passed with the intent
            mUserName = intent.getStringExtra(Constants.NAME).toString()
        }

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        //ensure the Places API is initialised
        if (!Places.isInitialized()) {
            Places.initialize(
                this,
                resources.getString(R.string.google_maps_api_key)
            )
        }

        //handles the functionality when the user selects the board image
        binding?.ivBoardImage?.setOnClickListener {
            /* Application first checks if the user has granted permission to read files from the devices storage media
               before displaying the image chooser
             */
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                Constants.showImageChooser(this)
                /* If permission is denied, the user will be prompted to grant permission to read
                   files from devices media storage in order to upload a board image.
                 */
            } else {
                ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    Constants.READ_STORAGE_PERMISSION_CODE
                )


            }
        }



        binding?.etEventLocation?.setOnClickListener {
            try {
                // This is the list of fields which has to be passed
                val fields = listOf(
                    Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS
                )
                // Start the autocomplete intent with a unique request code
                val intent =
                    Autocomplete
                        .IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                        .setHint("Event location")
                        .build(this)
                startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE)


            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        binding?.tvUseCurrentLocation?.setOnClickListener {
            if (!isLocationEnabled()) {

                Toast.makeText(this, "Your location provider is turned off", Toast.LENGTH_SHORT)
                    .show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            } else {
                Dexter.withActivity(this).withPermissions(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ).withListener(object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                        if (report != null) {
                            if (report.areAllPermissionsGranted()) {
                                showProgressDialog(resources.getString(R.string.please_wait))
                                requestNewLocationData()
                            }
                        }
                    }

                    @RequiresApi(Build.VERSION_CODES.M)
                    override fun onPermissionRationaleShouldBeShown(
                        permissions: MutableList<PermissionRequest>?,
                        token: PermissionToken?
                    ) {
                        shouldShowRequestPermissionRationale("Current Location")
                    }

                }).onSameThread()
                    .check()
            }


        }

        binding?.etEventDate?.setOnClickListener {
            showDatePicker()
        }
        binding?.etEventTime?.setOnClickListener {

            showTimePicker()
        }
        //handles the functionality when user selects the create button
        binding!!.btnCreate.setOnClickListener {
            if (mSelectedImageFileUri != null) {
                uploadBoardImage()
            } else {
                showProgressDialog(resources.getString(R.string.please_wait))
                creatEvent()
            }
        }
    }


    //method is responsible setting up the event details to be stored in cloud Firestore
    private fun creatEvent() {
        val assignedUsersArrayList: ArrayList<String> = ArrayList()
        assignedUsersArrayList.add(getCurrentUserID())
        val etEventName = binding?.etBoardName?.text.toString()
        val etEventLocation = binding?.etEventLocation?.text.toString()
        val etEventDate = binding?.etEventDate?.text.toString()

        if (validateEditForm(etEventName, etEventLocation, etEventDate)) {
            //board information that will be stored in Firestore
            val event = Event(
                binding?.etBoardName?.text.toString(),
                mBoardImageUrl,
                mUserName,
                assignedUsersArrayList,
                creatorID = getCurrentUserID(),
                eventLocation = etEventLocation,
                latitude = eventLatitude!!,
                longitude = eventLongitude!!,
                eventDate = mSelectedDueDateMilliSeconds,
                eventDescription = binding?.etEventDescription?.text.toString(),
                eventTime = mSelectedTime
            )

            //this function handles the creation of the board in cloud Firestore
            storeEvent(this, event)
        } else {
            hideProgressDialog()
        }


    }

    private fun validateEditForm(
        eventName: String,
        eventLocation: String,
        eventDate: String
    ): Boolean {
        return when {
            TextUtils.isEmpty(eventName) -> {
                showErrorSnackBar("Please enter an event name")
                false
            }
            TextUtils.isEmpty(eventLocation) -> {
                showErrorSnackBar("Please enter an event Location")
                false
            }
            TextUtils.isEmpty(eventDate) -> {
                showErrorSnackBar("Please enter an event date")
                false
            }
            else -> {
                return true
            }
        }
    }

    // Checks if the current location of the user is retrievable or not
    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    @SuppressLint("MissingPermission") // Suppressed because location permission has already been checked
    private fun requestNewLocationData() {
        val mLocationRequest = LocationRequest()

        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 1000
        mLocationRequest.numUpdates = 1

        mFusedLocationClient.requestLocationUpdates(
            mLocationRequest,
            mLocationCallBack,
            Looper.myLooper()!!
        )
        hideProgressDialog()
    }

    private val mLocationCallBack = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val mLastLocation: Location = locationResult.lastLocation

            eventLatitude = mLastLocation.latitude
            Log.i("Current Latitude", "$eventLatitude")
            eventLongitude = mLastLocation.longitude
            Log.i("Current Longitude", "$eventLongitude")

            val addressTask = GetAddressFromLatLng(
                this@CreateEventActivity,
                eventLatitude!!, eventLongitude!!
            )
            addressTask.setAddressListener(object : GetAddressFromLatLng.AddressListener {
                override fun onAddressFound(address: String?) {
                    binding?.etEventLocation?.setText(address)
                }

                override fun onError() {
                    Log.e("Get Address:", "Error getting address")
                }
            })
            addressTask.getAddress()
        }
    }

    //method handles the uploading of board images to cloud storage securely
    private fun uploadBoardImage() {
        showProgressDialog(resources.getString(R.string.please_wait))
        //reference to firebase storage
        val sref: StorageReference = FirebaseStorage.getInstance().reference.child(
            "BOARD_IMAGE" +
                    System.currentTimeMillis() + "." + Constants.getFileExtension(
                this,
                mSelectedImageFileUri
            )
        )
        //places the image file into Firebase storage
        sref.putFile(mSelectedImageFileUri!!).addOnSuccessListener { taskSnapshot ->
            /* logs the task snapshot to display where the image file is stored.
         * download URL is a publicly accessible URL that is used to access a file from Cloud Storage.
         * The download URL contains a download token which acts as a security measure to restrict access only to those who possess the token.
         */
            Log.i(
                "Event Image URL",
                taskSnapshot.metadata!!.reference!!.downloadUrl.toString()
            )
            taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener {
                Log.i(
                    "Downloadable Image URL", it.toString()
                )
                //stores the image uri in this variable
                mBoardImageUrl = it.toString()
                //only successful storage of board image will call this createBoard() method
                creatEvent()
            }
        }.addOnFailureListener {
            Toast.makeText(this, it.message, Toast.LENGTH_LONG).show()
            hideProgressDialog()
        }
    }


    //this function is called if the board is created and stored successfully in Firestore
    fun boardCreatedSuccessfully() {
        hideProgressDialog()
        setResult(Activity.RESULT_OK)
        finish()
    }

    private fun setupActionBar() {
        setSupportActionBar(binding?.toolbarCreateBoardActivity)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_round_arrow_back_24)
            actionBar.title = resources.getString(R.string.create_board_title)
        }
        binding?.toolbarCreateBoardActivity?.setNavigationOnClickListener {
            onBackPressed()
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && requestCode == Constants.PICK_IMAGE_REQUEST_CODE && data!!.data != null) {
            mSelectedImageFileUri = data.data

            try {
                binding?.ivBoardImage?.let {
                    Glide
                        .with(this)
                        .load(mSelectedImageFileUri)
                        .centerCrop()
                        .placeholder(R.drawable.add_screen_image_placeholder)
                        .into(it)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        } else if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    data.let {
                        // Stores the information retrieved from the places intent
                        val place: Place = Autocomplete.getPlaceFromIntent(data!!)
                        eventLatitude = place.latLng?.latitude
                        eventLongitude = place.latLng?.longitude
                        // Sets the Location input text field to the location searched Places search bar
                        binding?.etEventLocation?.setText(place.address)
                    }
                }
                AutocompleteActivity.RESULT_ERROR -> {
                    // Handle the error.
                    data?.let {
                        val status = Autocomplete.getStatusFromIntent(data)
                        Log.i(TAG, status.statusMessage.toString())
                    }
                }
                Activity.RESULT_CANCELED -> {
                    //cancels the widget
                    finishActivity(PLACE_AUTOCOMPLETE_REQUEST_CODE)
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Constants.READ_STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Constants.showImageChooser(this)
            }
        } else {
            Toast.makeText(
                this,
                "You just denied access permission to storage. You can allow it in the settings",
                Toast.LENGTH_LONG
            ).show()

        }
    }

    private fun showDatePicker() {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR) // Returns the value of the given calendar year
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(
            this,
            android.R.style.Theme_DeviceDefault_Light_Dialog,
            { datePicker, year, month, day ->
                val sDayOfMonth = if (day < 10) "0$day" else "$day"
                val sMonthOfYear = if ((month + 1) < 10) "0${month + 1}" else "${month + 1}"

                val selectedDate = "$sDayOfMonth/$sMonthOfYear/$year"
                binding?.etEventDate?.setText(selectedDate)

                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
                val theDate = sdf.parse(selectedDate)
                mSelectedDueDateMilliSeconds = theDate!!.time
            },
            year,
            month,
            day
        ).show()
    }

    private fun showTimePicker() {
        val t = Calendar.getInstance()
        val hourOfDay = t.get(Calendar.HOUR_OF_DAY)
        val minute = t.get(Calendar.MINUTE)

        val tpd = TimePickerDialog(
            this,
            android.R.style.Theme_DeviceDefault_Light_Dialog,
            { timePicker, hour, minute ->

                val sHour = if (hour < 10) "0${hour}" else "$hour"
                val sMinute = if (minute < 10) "0${minute}" else "$minute"

                val selectedTime = "$sHour:$sMinute"
                binding?.etEventTime?.setText(selectedTime)

                mSelectedTime = selectedTime
            },
            hourOfDay,
            minute,
            true
        )
            .show()
    }
}
