package org.example.quocardcodingtest.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Past
import java.time.LocalDate

data class AuthorRegisterDto(
    @field:NotBlank(message = "author can't not be blank")
    val name: String,

    @field:Past(message = "author birthDate can't be future day")
    val birthDate: LocalDate
)
