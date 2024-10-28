package com.example.pennbenedict.ui

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pennbenedict.RetrofitClient
import com.example.pennbenedict.RetrofitClient.aqiApiService
import com.example.pennbenedict.data.AqiConstants
import com.example.pennbenedict.data.model.Aqi
import kotlinx.coroutines.launch
import java.io.IOException

class AqiViewModel: ViewModel() {
    private val _allAQIData = MutableLiveData("No Data")
    private val _aQIDataObject = MutableLiveData<Aqi>()
    val allAQIData: LiveData<String> get() = _allAQIData
    var dataIsValid  = MutableLiveData<Boolean>()
    val aQIDataObject: LiveData<Aqi> get() = _aQIDataObject


    suspend fun getData(location: String, token: String):Boolean {
        var successfulResponse = true
        try {
            Log.d("ben!", "trying")
            val response = aqiApiService.getData(location, token)
            if (response.isSuccessful) {
                Log.d("ben!", "response IS successful")
                successfulResponse = true
                dataIsValid.value = true
                _aQIDataObject.value = response.body()
                Log.d("ben!", "response body: ${response.body().toString()}")
            } else {
                Log.d("ben!", "response NOT successful")
                successfulResponse = false
                dataIsValid.value = false
                // i can Handle HTTP errors (e.g., 4xx and 5xx status codes)
                val errorBody = response.errorBody()?.string()
                Log.d("ben!", "HTTP error: ${response.code()} - $errorBody")
                // I can also parse the error body if it's in a known format
            }
        } catch (e: IOException) {
            // Handle network issues/ no internet
            println("Network error: ${e.message}")
        } catch (e: Exception) {
            Log.d("ben!", "the second catch")
            dataIsValid.value = false
            // Handle any other errors
            println("Unexpected error: ${e.message}")
        }
        return successfulResponse
    }
}