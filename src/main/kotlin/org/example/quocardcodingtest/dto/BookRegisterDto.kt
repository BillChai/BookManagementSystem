package org.example.quocardcodingtest.dto

import jakarta.validation.Valid
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class BookRegisterDto(
    @field:NotBlank(message = "title must not be blank")
    val title: String,

    @field:DecimalMin(value = "0", inclusive = true, message = "price should be over 0")
    val price: Double,

    val publishStatus: Boolean,

    @field:Valid
    @field:Size(min = 1, message = "at least one author required")
    val author: List<AuthorRegisterDto>
)
