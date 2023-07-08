package com.mustafamujawar.libstack.network

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

data class LoanRq(
    val member_id: String = "",
    val book_id: String = "",
    val due_date: Long = 0,
    )

val BASE_URL = "https://pokeapi.co/api/v2/"
interface ApiService {
    @POST("pokemon/ditto")
    @JvmSuppressWildcards(suppress = true)
    fun postLoan(@Body body: LoanRq): Call<ResponseBody>
}

//2. Create an instance of Retrofit:

val retrofit: Retrofit = Retrofit.Builder()
    .baseUrl(BASE_URL)
    .addConverterFactory(GsonConverterFactory.create())
    .build()

//3. Create an instance of the interface:

val apiService: ApiService = retrofit.create(ApiService::class.java)

//4. Create the request body:

