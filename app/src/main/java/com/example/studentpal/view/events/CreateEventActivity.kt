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
import com.example.studentpal.BuildConfig
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
/**
 * This activity is responsible for creating an event by entering its information.
 * Makes use of the [Places API](https://developers.google.com/maps/documentation/places/web-service/overview)
 * and [Fused Location Provider](https://developers.google.com/maps/documentation/places/web-service/overview)
 *
 * The code displayed was adapted from Denis Panjuta's Trello clone (see references file)
 * However, the author significantly evolved the code produced by Panjuta to accommodate the project requirements.
 *
 * All code that was created by the author will be labelled [My Code].
 *
 * Reused code that has been adapted by the author is labeled [Adapted ].
 *
 * @see[com.example.studentpal.common.References]
 */
@Suppress("DEPRECATION")
class CreateEventActivity : BaseActivity() {
    // GLOBAL VARIABLES
    var binding: ActivityCreateBoardBinding? = null
    private lateinit var mUserName: String
    private var mSelectedImageFileUri: Uri? = null // Uri value for event image
    private var mEventImageUrl: String = "" // URL for event image
    private var eventLatitude: Double? = null // Latitude value of event location
    private var eventLongitude: Double? = null // Longitude value of event location
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private var mSelectedEventDate: Long = 0 // Selected Event Date
    private var mSelectedTime: String = "" // Selected Event Time

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

        //Initialise Fused location provider
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)


        //Initialize the places sdk if it is not initialized earlier using the api key.
        if (!Places.isInitialized()) {
            Places.initialize(
                this, Constants.MAPS_API_KEY
            )
        }

        //handles the functionality when the user selects the event image
        binding?.ivEventImage?.setOnClickListener {
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
                // Location Permission requests with Dexter
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
                uploadEventImage()
            } else {
                showProgressDialog(resources.getString(R.string.please_wait))
                createEvent()
            }
        }
    }


    /**
     * method is responsible setting up the event details to be stored in cloud Firestore
     */
    private fun createEvent() {
        //arraylist will store all the users assigned to the event
        val assignedUsersArrayList: ArrayList<String> = ArrayList()
        assignedUsersArrayList.add(getCurrentUserID())

        // Retrieves the entered details from the event form
        val etEventName = binding?.etEventName?.text.toString()
        val etEventLocation = binding?.etEventLocation?.text.toString()
        val etEventDate = binding?.etEventDate?.text.toString()

        // validates the information is entered correctly
        if (validateEditForm(etEventName, etEventLocation, etEventDate)) {
            //board information that will be stored in Firestore
            val event = Event(
                binding?.etEventName?.text.toString(),
                mEventImageUrl,
                mUserName,
                assignedUsersArrayList,
                creatorID = getCurrentUserID(),
                eventLocation = etEventLocation,
                latitude = eventLatitude!!,
                longitude = eventLongitude!!,
                eventDate = mSelectedEventDate,
                eventDescription = binding?.etEventDescription?.text.toString(),
                eventTime = mSelectedTime
            )

            //this function handles the creation of the board in cloud Firestore
            storeEvent(this, event)
        } else {
            hideProgressDialog()
        }

    }

    /**
     * Method ensures all information is entered in the event form
     */
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

    /**
     * method verifies if location or GPS is enabled for the users device
     */
    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    /**
     * A method to request the current location. Using the fused location provider
     */
    @SuppressLint("MissingPermission") // Suppressed because location permission has already been checked
    private fun requestNewLocationData() {
        val mLocationRequest = LocationRequest()

        // Configures location settings
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

    /**
     * A location callback object of fused location provider client where we will get current location details
     */
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
            //Call the AsyncTask class for getting an address from the latitude and longitude.
            addressTask.setAddressListener(object : GetAddressFromLatLng.AddressListener {
                override fun onAddressFound(address: String?) {
                    binding?.etEventLocation?.setText(address) // Address is set to the edittext
                }

                override fun onError() {
                    Log.e("Get Address:", "Error getting address")
                }
            })
            addressTask.getAddress()
        }
    }

    /**
     * method handles the uploading of event images to cloud storage securely
     */

    private fun uploadEventImage() {
        showProgressDialog(resources.getString(R.string.please_wait))
        //reference to firebase storage
        val storageRef: StorageReference = FirebaseStorage.getInstance().reference.child(
            "EVENT_IMAGE" +
                    System.currentTimeMillis() + "." + Constants.getFileExtension(
                this,
                mSelectedImageFileUri
            )
        )
        //places the image file into Firebase storage
        storageRef.putFile(mSelectedImageFileUri!!).addOnSuccessListener { taskSnapshot ->
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
                mEventImageUrl = it.toString()
                //only successful storage of board image will call this createBoard() method
                createEvent()
            }
        }.addOnFailureListener {
            Toast.makeText(this, it.message, Toast.LENGTH_LONG).show()
            hideProgressDialog()
        }
    }


    /** Method is called if the event is created and stored successfully in Firestore
     */
    fun eventCreatedSuccessfully() {
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
            actionBar.title = "Create Event"
        }
        binding?.toolbarCreateBoardActivity?.setNavigationOnClickListener {
            onBackPressed()
        }

    }

    /**
     * Receives the result from a previous call to startActivityForResult
     * @param resultCode The integer result code returned by the child activity through its setResult().
     * @Param requestCode The integer request code originally supplied to startActivityForResult(), allowing you to identify where this result came from
     * @param data  An Intent, which can return result data to the caller
     */
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == Constants.PICK_IMAGE_REQUEST_CODE && data!!.data != null) {
            mSelectedImageFileUri = data.data
            try {
                // Loads the selected image from gallery to imageview
                binding?.ivEventImage?.let {
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
                    // Handles the error.
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

    /**
     * method handles the request permission result
     */
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

    /**
     * Method to display a date picker dialog to user
     */
    private fun showDatePicker() {
        val c = Calendar.getInstance()
        val yearSelected = c.get(Calendar.YEAR) // Returns the value of the given calendar year
        val monthSelected = c.get(Calendar.MONTH) // Returns the value of the given calendar Month
        val daySelected = c.get(Calendar.DAY_OF_MONTH) // Returns the value of the given day of month

        DatePickerDialog(
            this,
            android.R.style.Theme_DeviceDefault_Light_Dialog,
            { _, year, month, day ->
                // Appends a 0 to the start of day if month is less than 10
                val sDayOfMonth = if (day < 10) "0$day" else "$day"
                // Appends a 0 to the start of month  if less than 10
                val sMonthOfYear = if ((month + 1) < 10) "0${month + 1}" else "${month + 1}"
                // Sets the edit text to the selected date
                val selectedDate = "$sDayOfMonth/$sMonthOfYear/$year"
                binding?.etEventDate?.setText(selectedDate)

                // Prepares the date format to be displayed
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
                val theDate = sdf.parse(selectedDate)
                //initialises variable
                mSelectedEventDate = theDate!!.time
            },
            yearSelected,
            monthSelected,
            daySelected
        ).show()
    }

    /**
     * Method to display time picker to user
     */
    private fun showTimePicker() {
        val t = Calendar.getInstance()
        val hourOfDay = t.get(Calendar.HOUR_OF_DAY)
        val minuteSelected= t.get(Calendar.MINUTE)

        TimePickerDialog(
            this,
            android.R.style.Theme_DeviceDefault_Light_Dialog, { _, hour, minute ->
                //appends a 0 if hour is less than 10
                val sHour = if (hour < 10) "0${hour}" else "$hour"
                //appends a 0 if minute is less than 10
                val sMinute = if (minute < 10) "0${minute}" else "$minute"
                // Sets the edit text to the selected time
                val selectedTime = "$sHour:$sMinute"
                binding?.etEventTime?.setText(selectedTime)
                //initialises variable
                mSelectedTime = selectedTime
            },
            hourOfDay,
            minuteSelected,
            true
        )
            .show()
    }
}
