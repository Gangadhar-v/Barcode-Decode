package com.example.barcode_decode.model

import com.google.gson.annotations.SerializedName

data class Product(
    @SerializedName("code") val code: String,
    @SerializedName("product") val productDetails: ProductDetails
)