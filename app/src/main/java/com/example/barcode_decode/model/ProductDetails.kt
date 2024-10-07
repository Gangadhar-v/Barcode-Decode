package com.example.barcode_decode.model

import com.google.gson.annotations.SerializedName

data class ProductDetails(
    @SerializedName("allergens") val allergens: String,
    @SerializedName("expiration_date") val expirationDate: String?,
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("quantity") val quantity: String?,
    @SerializedName("product_name") val productName: String,
    @SerializedName("ingredients_text_en_ocr_1642445989_result") val ingredientsText: String?,
    @SerializedName("proteins_100g") val proteins100g: Double?,
    @SerializedName("salt_100g") val salt100g: Double?,
    @SerializedName("saturated-fat_100g") val saturatedFat100g: Double?,
    @SerializedName("sugars_100g") val sugars100g: Double?
)
