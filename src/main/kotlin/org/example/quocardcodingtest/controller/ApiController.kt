package org.example.quocardcodingtest.controller

import org.example.quocardcodingtest.dto.AuthorRegisterDto
import org.example.quocardcodingtest.dto.AuthorUpdateDto
import org.example.quocardcodingtest.dto.BookRegisterDto
import org.example.quocardcodingtest.dto.BookUpdateDto
import org.example.quocardcodingtest.service.AuthorService
import org.example.quocardcodingtest.service.BookService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1")
class ApiController (
    private val authorService: AuthorService,
    private val bookService: BookService
){
    @GetMapping("/healthcheck")
    fun getHealthCheck(): ResponseEntity<Any> {
        return ResponseEntity.ok().build()
    }

    @GetMapping("/book/{bookId}")
    fun getBookById(@PathVariable bookId: Long): ResponseEntity<Any> {
        return restructureResponse(bookService.getBookService(bookId))
    }

    @PutMapping("/book/{bookId}")
    fun updateBookById(@PathVariable bookId: Long, @Validated @RequestBody bookUpdateDto: BookUpdateDto): ResponseEntity<Any> {
        return restructureResponse(bookService.updateBookService(bookId, bookUpdateDto))
    }

    @GetMapping("/books")
    fun getBooks(): ResponseEntity<Any> {
        return restructureResponse(bookService.getBooksService())
    }

    @PostMapping("/book")
    fun registerBook(@Validated @RequestBody bookRegisterDto: BookRegisterDto): ResponseEntity<Any> {
        return restructureResponse(bookService.insertBookService(bookRegisterDto))
    }


    @GetMapping("/author/{authorId}")
    fun getAuthorById(@PathVariable authorId: Long): ResponseEntity<Any> {
        return restructureResponse(authorService.getAuthorService(authorId))
    }

    @PutMapping("/author/{authorId}")
    fun updateAuthorById(@PathVariable authorId: Long, @Validated @RequestBody authorUpdateDto: AuthorUpdateDto): ResponseEntity<Any> {
        return restructureResponse(authorService.updateAuthorService(authorId, authorUpdateDto))
    }

    @GetMapping("/authors")
    fun getAuthors(): ResponseEntity<Any> {
        return restructureResponse(authorService.getAuthorsService())
    }

    @PostMapping("/author")
    fun registerAuthor(@Validated @RequestBody authorRegisterDto: AuthorRegisterDto): ResponseEntity<Any> {
        return restructureResponse(authorService.insertAuthorService(authorRegisterDto))
    }

    private fun <T> restructureResponse(data: T?, errorMessage: String? = null, status: HttpStatus = HttpStatus.OK): ResponseEntity<Any> {
        return when {
            data != null -> ResponseEntity(data, status) // Return the data as a ResponseEntity
            errorMessage != null -> ResponseEntity(mapOf("error" to errorMessage), status) // Return error message
            else -> ResponseEntity("Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR) // Default response when no data and no error message
        }
    }

}