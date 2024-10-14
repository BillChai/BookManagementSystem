package org.example.quocardcodingtest.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Past
import java.time.LocalDate

data class AuthorRegisterDto(
    @field:NotBlank(message = "著者名は必須です。")
    val name: String,

    @field:Past(message = "誕生日は過去の日付でなければなりません。")
    val birthDate: LocalDate
)
