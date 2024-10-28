package com.example.pennbenedict

//import com.example.pennbenedict.data.model.dataAqi.Data
//import com.example.pennbenedict.data.model.dataAqi.Feedback
import com.example.pennbenedict.data.model.Aqi
import retrofit2.Response
//import com.example.pennbenedict.data.model.dataAqi.AQI
//import com.example.pennbenedict.data.model.Data2
//import com.example.pennbenedict.data.model.dataAqi.AQI
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path
import retrofit2.http.Query

interface AqiApiService {

    @GET("{geoOrCity}/")
    suspend fun getData(
        @Path("geoOrCity") geoOrCity: String,
        @Query("token") token: String,
        ): Response<Aqi>

//    @Headers("accept:text/plain")//this is for token/apiKeys


}