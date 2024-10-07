package com.example.barcode_decode

import com.example.barcode_decode.model.Product
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface RetrofitService {

    @GET("api/v2/product/{barcode}")
    fun getProduct(@Path("barcode") barcode: String): Call<Product>
}