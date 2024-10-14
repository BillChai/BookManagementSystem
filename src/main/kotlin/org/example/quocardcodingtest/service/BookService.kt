package org.example.quocardcodingtest.service

import org.example.quocardcodingtest.dto.BookRegisterDto
import org.example.quocardcodingtest.dto.BookUpdateDto
import org.example.quocardcodingtest.exception.InternalServerErrorException
import org.example.quocardcodingtest.exception.ResourceNotFoundException
import org.example.quocardcodingtest.exception.UpdateBadRequestException
import org.example.quocardcodingtest.model.Book
import org.example.quocardcodingtest.repository.BookRepository
import org.springframework.stereotype.Service

interface BookService {
    // get all
    fun getBooksService() : List<Book>
    fun getBookService(id: Long) : Book
    fun updateBookService(id: Long, bookUpdateDto: BookUpdateDto) : Book
    fun insertBookService(bookRegisterDto: BookRegisterDto) : Book
}

@Service
class BookServiceImpl (
    private val bookRepository: BookRepository
) : BookService{
    override fun getBooksService(): List<Book> {
        return bookRepository.findAll()
    }

    override fun getBookService(id: Long): Book {
        val book = bookRepository.findById(id)
        if (book === null){
            throw ResourceNotFoundException("book not found")
        }
        return book
    }

    override fun insertBookService(bookRegisterDto: BookRegisterDto): Book {
        val bookId = bookRepository.insert(bookRegisterDto)
        val book = bookRepository.findById(bookId)
        if (book === null){
            throw ResourceNotFoundException("book not found")
        }
        return book
    }

    override fun updateBookService(id: Long, bookUpdateDto: BookUpdateDto): Book {
        if (bookUpdateDto.allPropertiesAreNullExceptAge()){
            throw UpdateBadRequestException("Book update request must contain at least one valid field.")
        }

        bookRepository.update(id, bookUpdateDto)

        val book = bookRepository.findById(id)
        if (book === null){
            throw InternalServerErrorException("book not found")
        }
        return book
    }
}