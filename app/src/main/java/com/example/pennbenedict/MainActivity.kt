package com.example.pennbenedict

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pennbenedict.ui.AqiViewModel
import com.example.pennbenedict.ui.theme.PennBenedictTheme
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.example.pennbenedict.data.AqiConstants
import com.example.pennbenedict.data.LocationDetails
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var launcherMultiplePermissions: ManagedActivityResultLauncher<Array<String>, Map<String, Boolean>>
    private var locationCallback: LocationCallback? = null
    var fusedLocationClient: FusedLocationProviderClient? = null
    private var locationRequired = false
    var internetIsAvailable = MutableLiveData<Boolean>()
    var currentLocation = MutableLiveData<LocationDetails>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        setContent {
            PennBenedictTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var context = LocalContext.current

                    fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
                    locationCallback = object : LocationCallback() {
                        override fun onLocationResult(p0: LocationResult) {
                            for (lo in p0.locations) {
                                // Update UI with location data
                                currentLocation.value = LocationDetails(lo.latitude, lo.longitude)
                            }
                        }
                    }

                     launcherMultiplePermissions = rememberLauncherForActivityResult(
                        ActivityResultContracts.RequestMultiplePermissions()
                    ) { permissionsMap ->
                        val areGranted = permissionsMap.values.reduce { acc, next -> acc && next }
                        if (areGranted) {
                            locationRequired = true
                            startLocationUpdates()

                            Toast.makeText(context, "Permission Granted", Toast.LENGTH_SHORT).show()
                        } else {
                             Toast.makeText(context, "Permission Denied", Toast.LENGTH_SHORT).show()
                        }
                    }


                   // getLastLocation( )
                    AQIView()
                }
            }
        }
    }

    @Composable
    fun AQIView(modifier: Modifier = Modifier, viewModel: AqiViewModel = viewModel(), ){//, locationLive: LiveData<LocationDetails>) {
        var cityText = remember {
            mutableStateOf("")
        }

        Column(
            modifier = modifier
                .padding(12.dp)
        ) {

            val allDataObject = viewModel.aQIDataObject.observeAsState().value
            val dataIsValid = viewModel.dataIsValid.observeAsState().value

            var context = LocalContext.current


            val permissions = arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )

            Button(
                modifier = Modifier
                    .width(300.dp)
                    .height(56.dp)
                    .padding(top = 2.dp),
                onClick = {
                    if (permissions.all {
                            ContextCompat.checkSelfPermission(
                                context,
                                it
                            ) == PackageManager.PERMISSION_GRANTED
                        }) {
                        // Get the location
                        startLocationUpdates()


                        Log.d(
                            "ben!",
                            "What geo looks like:" + "/geo:" + currentLocation?.value?.latitude.toString() + ";" + currentLocation?.value?.toString()
                        )

                        getAqi(
                            viewModel,
                            AqiConstants.GEO_PREFIX + currentLocation?.value?.latitude.toString() + ";" + currentLocation?.value?.longitude.toString()
                        )

                    } else {
                        launcherMultiplePermissions.launch(permissions)
                        getAqi(
                            viewModel,
                            AqiConstants.GEO_PREFIX + currentLocation?.value?.latitude.toString() + ";" + currentLocation?.value?.longitude.toString()
                        )
                    }
                },

                ) {
                Text(text = "Local Location. (Click Twice?)", fontSize = 16.sp )
            }

            isInternetAvailable(context)
            if (!isInternetAvailable(context)){
                Log.d("ben!", "No internet connection!")
                Text(text = "No internet connection!",
                    fontSize = 18.sp,
                    color = Color.Red)
            }
            if (dataIsValid == false ){
                Text(text = "Bad Request. Is The Location Valid?",
                    fontSize = 18.sp,
                    color = Color.Red)
            } else if ((dataIsValid == true )){ }


            if (allDataObject != null) {
                allDataObject?.dataValue?.airQualityIndex?.let {
                    Text(
                        text = "Current AQI: $it",
                        modifier = modifier.padding(top = 6.dp),
                        fontSize = 32.sp
                    )
                }
            }
            allDataObject?.dataValue?.forecast?.daily?.o3?.first()?.avg.let {
                Text(
                    text = "Yesterday's AQI: $it",
                    modifier = Modifier.padding(top = 6.dp),
                    fontSize = 18.sp
                )
            }
            allDataObject?.dataValue?.forecast?.daily?.o3?.get(2)?.avg.let {
                Text(
                    text = "Tomorrow's AQI: $it",
                    modifier = Modifier.padding(top = 6.dp),
                    fontSize = 18.sp
                )
            }
            allDataObject?.dataValue?.city?.cityName.let {
                Text(
                    text = "City:  $it",
                    modifier = modifier.padding(top = 24.dp),
                    fontSize = 18.sp
                )
            }
            allDataObject?.dataValue?.attributions?.first()?.stationName.let {
                Text(
                    text = "Station: $it",
                    modifier = modifier.padding(top = 6.dp),
                    fontSize = 18.sp
                )
            }
            allDataObject?.dataValue?.city?.geo?.first().let {
                Text(
                    text = "Latitude:  $it",
                    modifier = Modifier.padding(top = 6.dp),
                    fontSize = 18.sp
                )
            }
            allDataObject?.dataValue?.city?.geo?.last().let {
                Text(
                    text = "Longitude: $it",
                    modifier = Modifier.padding(top = 6.dp),
                    fontSize = 18.sp
                )
            }
            OutlinedTextField(
                modifier = Modifier.padding(top = 24.dp),
                value = cityText.value,
                label = {
                    Text("ENTER CITY")
                },
                onValueChange = {
                    cityText.value = it
                })

            Button(
                onClick = {
                    getAqi(viewModel, cityText.value)
                          },
                modifier = Modifier
                    .width(300.dp)
                    .height(65.dp)
                    .padding(top = 12.dp)
            ) {
                Text(
                    text = "Search Location",
                    fontSize = 16.sp
                )
            }
        }
    }


    @SuppressLint("ServiceCast")
    fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
        internetIsAvailable.value = activeNetwork.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        return activeNetwork.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        locationCallback?.let {
            val locationRequest = LocationRequest.create().apply {
                interval = 10000
                fastestInterval = 3000
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            }
            fusedLocationClient?.requestLocationUpdates(
                locationRequest,
                it,
                Looper.getMainLooper()
            )
            Log.d("ben!", "real location up dates?")
        }
    }

    fun getAqi(viewModel: AqiViewModel, location: String){
        lifecycleScope.launch {viewModel.getData(location, AqiConstants.TOKEN_ID,)}
    }

    override fun onResume() {
        super.onResume()
        if (locationRequired) {
            startLocationUpdates()
        }
    }
    override fun onPause() {
        super.onPause()
        locationCallback?.let { fusedLocationClient?.removeLocationUpdates(it) }
    }
}

@Preview(showBackground = true)
@Composable
fun AQIView(){}