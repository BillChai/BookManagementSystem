package org.example.quocardcodingtest.model

data class Book (
    val id: Int,
    val title: String,
    val price: Double,
    val publishStatus: Boolean,
    var author: List<String>
)
