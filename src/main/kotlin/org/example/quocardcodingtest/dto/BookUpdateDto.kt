package org.example.quocardcodingtest.dto

import jakarta.validation.Valid

data class BookUpdateDto(
    val title: String?,
    val price: Double?,
    val publishStatus: Boolean?,

    @field:Valid
    val author: List<AuthorRegisterDto>?
){
    fun allPropertiesAreNullExceptAge(): Boolean {
        return title == null && price == null && publishStatus == null && author == null
    }
}
