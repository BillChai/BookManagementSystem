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
    fun getBooksService(authorName: String?) : List<Book>
    fun getBookService(id: Long) : Book
    fun updateBookService(id: Long, bookUpdateDto: BookUpdateDto) : Book
    fun insertBookService(bookRegisterDto: BookRegisterDto) : Book
}

@Service
class BookServiceImpl (
    private val bookRepository: BookRepository
) : BookService{
    /**
     * get all books
     * @param authorName
     */
    override fun getBooksService(authorName: String?): List<Book> {
        return if(authorName != null){
            bookRepository.findAllByAuthor(authorName)
        } else{
            bookRepository.findAll()
        }
    }

    /**
     * get book by id
     * @param id book id
     * @return book if book exist else throw not found error
     */
    override fun getBookService(id: Long): Book {
        val book = bookRepository.findById(id)
        if (book === null){
            throw ResourceNotFoundException("book not found")
        }
        return book
    }

    /**
     * insert book
     * @param bookRegisterDto
     * @return book if inserted success else throw error
     */
    override fun insertBookService(bookRegisterDto: BookRegisterDto): Book {
        val bookId = bookRepository.insert(bookRegisterDto)
        val book = bookRepository.findById(bookId)
        if (book === null){
            throw InternalServerErrorException("book insert failed")
        }
        return book
    }

    /**
     * update book
     * @param id book id
     * @param bookUpdateDto
     * @return book
     * if all parameter = null return UpdateBadRequestException
     * if book updated failed return UpdateBadRequestException
     */
    override fun updateBookService(id: Long, bookUpdateDto: BookUpdateDto): Book {
        if (bookUpdateDto.allPropertiesAreNullExceptAge()){
            throw UpdateBadRequestException("Book update request must contain at least one valid field.")
        }

        // check book id exist
        var book = bookRepository.findById(id)
        if (book === null){
            throw ResourceNotFoundException("book not found")
        }

        // check book is publishStatus is not false
        if (bookUpdateDto.publishStatus == false && book.publishStatus){
            throw UpdateBadRequestException("published book can't be change to unpublished")
        }

        bookRepository.update(id, bookUpdateDto)

        // return book object
        book = bookRepository.findById(id)
        if (book === null){
            throw InternalServerErrorException("book update failed")
        }
        return book
    }
}