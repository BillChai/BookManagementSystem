package org.example.quocardcodingtest.dto

import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class BookRegisterDto(
    @field:NotBlank(message = "タイトルは必須です。")
    val title: String,

    @field:DecimalMin(value = "0", inclusive = true, message = "価格は0以上である必要があります。")
    val price: Double,

    val publishStatus: Boolean,

    @field:Size(min = 1, message = "著者は最低1人必要です。")
    val author: List<AuthorRegisterDto>
)
