package org.example.quocardcodingtest.service

import org.example.quocardcodingtest.dto.BookRegisterDto
import org.example.quocardcodingtest.dto.BookUpdateDto
import org.example.quocardcodingtest.exception.InternalServerErrorException
import org.example.quocardcodingtest.exception.ResourceNotFoundException
import org.example.quocardcodingtest.exception.UpdateBadRequestException
import org.example.quocardcodingtest.model.Book
import org.example.quocardcodingtest.repository.BookRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.*
import kotlin.test.assertEquals

class BookServiceImplTest {

    private lateinit var bookRepository: BookRepository
    private lateinit var bookService: BookServiceImpl

    @BeforeEach
    fun setUp() {
        bookRepository = mock()
        bookService = BookServiceImpl(bookRepository)
    }

    /**
     * test for get all books
     */
    @Nested
    inner class GetBooksServiceTests {

        @Test
        fun `get all books`() {
            // Given
            val books = listOf(Book(1, "Book 1", 9.99, true, listOf("Author 1")))
            `when`(bookRepository.findAll()).thenReturn(books)

            // When
            val result = bookService.getBooksService(null)

            // Then
            assertEquals(books, result)
            verify(bookRepository).findAll()
        }

        @Test
        fun `get all books by author`() {
            // Given
            val authorName = "authorName"
            val books = listOf(Book(1, "Book 1", 9.99, true, listOf("Author 1")))
            `when`(bookRepository.findAllByAuthor(authorName)).thenReturn(books)

            // When
            val result = bookService.getBooksService(authorName)

            // Then
            assertEquals(books, result)
            verify(bookRepository).findAllByAuthor(authorName)
        }
    }

    /**
     * test for get book by id
     */
    @Nested
    inner class GetBookServiceTests {

        @Test
        fun `should return a book by id`() {
            // Given
            val book = Book(1, "Book 1", 9.99, true, listOf("Author 1"))
            `when`(bookRepository.findById(1)).thenReturn(book)

            // When
            val result = bookService.getBookService(1)

            // Then
            assertEquals(book, result)
            verify(bookRepository).findById(1)
        }

        @Test
        fun `should throw ResourceNotFoundException when book not found`() {
            // Given
            `when`(bookRepository.findById(1)).thenReturn(null)

            // Then
            assertThrows<ResourceNotFoundException> {
                bookService.getBookService(1)
            }
        }
    }

    /**
     * test for insert book
     */
    @Nested
    inner class InsertBookServiceTests {

        @Test
        fun `should insert a book and return it`() {
            // Given
            val bookRegisterDto = BookRegisterDto("Book 1", 9.99, true, emptyList())
            val book = Book(1, "Book 1", 9.99, true, listOf("Author 1"))
            `when`(bookRepository.insert(bookRegisterDto)).thenReturn(1)
            `when`(bookRepository.findById(1)).thenReturn(book)

            // When
            val result = bookService.insertBookService(bookRegisterDto)

            // Then
            assertEquals(book, result)
            verify(bookRepository).insert(bookRegisterDto)
            verify(bookRepository).findById(1)
        }

        @Test
        fun `should throw InternalServerErrorException when inserting a book fails`() {
            // Given
            val bookRegisterDto = BookRegisterDto("Book 1", 9.99, true, emptyList())
            `when`(bookRepository.insert(bookRegisterDto)).thenReturn(1)
            `when`(bookRepository.findById(1)).thenReturn(null)

            // Then
            assertThrows<InternalServerErrorException> {
                bookService.insertBookService(bookRegisterDto)
            }
        }
    }

    /**
     * test for update book
     */
    @Nested
    inner class UpdateBookServiceTests {

        @Test
        fun `should update a book and return it`() {
            // Given
            val existingBook = Book(1, "Book 1", 9.99, true, listOf("Author 1"))
            val updatedBook = Book(1, "Updated Book", 19.99, true, listOf("Author 1"))
            val bookUpdateDto = BookUpdateDto("Updated Book", 19.99, true, null)

            `when`(bookRepository.findById(1)).thenReturn(existingBook).thenReturn(updatedBook)
            doNothing().`when`(bookRepository).update(1, bookUpdateDto)

            // When
            val result = bookService.updateBookService(1, bookUpdateDto)

            // Then
            assertEquals(updatedBook, result)
        }

        @Test
        fun `should throw ResourceNotFoundException when updating a non-existing book`() {
            // Given
            val bookUpdateDto = BookUpdateDto("Updated Book", 19.99, true, null)
            `when`(bookRepository.findById(1)).thenReturn(null)

            // Then
            assertThrows<ResourceNotFoundException> {
                bookService.updateBookService(1, bookUpdateDto)
            }
        }

        @Test
        fun `should throw UpdateBadRequestException if updating to unpublished status`() {
            // Given
            val existingBook = Book(1, "Book 1", 9.99, true, listOf("Author 1"))
            val bookUpdateDto = BookUpdateDto(null, null, false, null)
            `when`(bookRepository.findById(1)).thenReturn(existingBook)

            // Then
            assertThrows<UpdateBadRequestException> {
                bookService.updateBookService(1, bookUpdateDto)
            }
        }

        @Test
        fun `should throw UpdateBadRequestException when all update fields are null`() {
            // Given
            val existingBook = Book(1, "Book 1", 9.99, true, listOf("Author 1"))
            val bookUpdateDto = BookUpdateDto(null, null, null, null)
            `when`(bookRepository.findById(1)).thenReturn(existingBook)

            // Then
            assertThrows<UpdateBadRequestException> {
                bookService.updateBookService(1, bookUpdateDto)
            }
        }

        @Test
        fun `should throw InternalServerErrorException if book not found after update`() {
            // Given
            val existingBook = Book(1, "Book 1", 9.99, true, listOf("Author 1"))
            val bookUpdateDto = BookUpdateDto("Updated Book", 19.99, true, null)
            `when`(bookRepository.findById(1)).thenReturn(existingBook).thenReturn(null)
            doNothing().`when`(bookRepository).update(1, bookUpdateDto)
            // Then
            assertThrows<InternalServerErrorException> {
                bookService.updateBookService(1, bookUpdateDto)
            }
        }
    }
}
