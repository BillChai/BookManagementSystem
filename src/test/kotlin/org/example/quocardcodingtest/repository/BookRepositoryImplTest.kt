package org.example.quocardcodingtest.repository

import org.example.quocardcodingtest.dto.AuthorRegisterDto
import org.example.quocardcodingtest.dto.BookRegisterDto
import org.example.quocardcodingtest.dto.BookUpdateDto
import org.jooq.DSLContext
import org.jooq.model.tables.Authors.Companion.AUTHORS
import org.jooq.model.tables.BookAuthor.Companion.BOOK_AUTHOR
import org.jooq.model.tables.Books.Companion.BOOKS
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jooq.JooqTest
import org.springframework.context.annotation.Import
import java.time.LocalDate

@JooqTest
@Import(BookRepositoryImpl::class)
class BookRepositoryImplTest {

    @Autowired
    private lateinit var dslContext: DSLContext

    @Autowired
    private lateinit var bookRepository: BookRepositoryImpl

    @BeforeEach
    fun setUp() {
        // Clean up all tables to ensure isolated tests
        dslContext.deleteFrom(BOOK_AUTHOR).execute()
        dslContext.deleteFrom(AUTHORS).execute()
        dslContext.deleteFrom(BOOKS).execute()
    }

    /**
     * test for bookRepository insert function
     */
    @Nested
    inner class InsertBook{
        @Test
        fun `insert and check id been inserted by findById`() {
            val authorDto = AuthorRegisterDto("J.K. Rowling", LocalDate.of(1965, 7, 31))
            val bookRegisterDto = BookRegisterDto(
                title = "Harry Potter",
                price = 39.99,
                publishStatus = true,
                author = listOf(authorDto)
            )

            val bookId = bookRepository.insert(bookRegisterDto)
            val book = bookRepository.findById(bookId)

            assertNotNull(book)
            assertEquals("Harry Potter", book?.title)
            assertEquals(39.99, book?.price)
            assertEquals(listOf("J.K. Rowling"), book?.author)
        }
    }

    /**
     * test for bookRepository find all and find all by name function
     */
    @Nested
    inner class FindAll{
        @Test
        fun `find all books`() {
            val author1 = AuthorRegisterDto("Author 1", LocalDate.of(1970, 1, 1))
            val author2 = AuthorRegisterDto("Author 2", LocalDate.of(1980, 2, 2))

            val book1 = BookRegisterDto("Book 1", 29.99, true, listOf(author1))
            val book2 = BookRegisterDto("Book 2", 19.99, false, listOf(author2))

            bookRepository.insert(book1)
            bookRepository.insert(book2)

            val books = bookRepository.findAll()

            assertEquals(2, books.size)
            assertEquals("Book 1", books[0].title)
            assertEquals("Book 2", books[1].title)
        }

        @Test
        fun `find all books by name`() {
            val author1 = AuthorRegisterDto("Author 1", LocalDate.of(1970, 1, 1))
            val author2 = AuthorRegisterDto("Author 2", LocalDate.of(1980, 2, 2))

            val book1 = BookRegisterDto("Book 1", 19.99, true, listOf(author1,author2))
            val book2 = BookRegisterDto("Book 2", 29.99, false, listOf(author1,author2))
            val book3 = BookRegisterDto("Book 3", 39.99, false, listOf(author2))

            bookRepository.insert(book1)
            bookRepository.insert(book2)
            bookRepository.insert(book3)

            val books = bookRepository.findAllByAuthor(author1.name)

            assertEquals(2, books.size)
            assertEquals("Book 1", books[0].title)
            assertEquals("Book 2", books[1].title)
        }
    }

    /**
     * test for bookRepository find by id
     */
    @Nested
    inner class FindById{
        @Test
        fun `find book by id`() {
            val author = AuthorRegisterDto("Author 1", LocalDate.of(1970, 1, 1))
            val bookRegisterDto = BookRegisterDto("Book 1", 29.99, true, listOf(author))

            val bookId = bookRepository.insert(bookRegisterDto)

            val book = bookRepository.findById(bookId)

            assertNotNull(book)
            assertEquals(book?.title, bookRegisterDto.title)
            assertEquals(book?.price, bookRegisterDto.price)
            assertEquals(book?.publishStatus, bookRegisterDto.publishStatus)
            assertEquals(book?.author, listOf(author.name))
        }
    }

    /**
     * test for bookRepository update book
     */
    @Nested
    inner class UpdateBook{
        @Test
        fun `test update book for title, price, publishStatus and author`() {
            val authorDto = AuthorRegisterDto("George R.R. Martin", LocalDate.of(1948, 9, 20))
            val bookRegisterDto = BookRegisterDto(
                title = "Game of Thrones",
                price = 49.99,
                publishStatus = true,
                author = listOf(authorDto)
            )

            val bookId = bookRepository.insert(bookRegisterDto)

            val bookUpdateDto = BookUpdateDto(
                title = "A Song of Ice and Fire",
                price = 59.99,
                publishStatus = true,
                author = listOf(AuthorRegisterDto("G.R.R. Martin", LocalDate.of(1948, 9, 20)))
            )

            bookRepository.update(bookId, bookUpdateDto)
            val updatedBook = bookRepository.findById(bookId)

            assertNotNull(updatedBook)
            assertEquals(bookUpdateDto.title, updatedBook?.title)
            assertEquals(bookUpdateDto.price, updatedBook?.price)
            assertEquals(listOf("G.R.R. Martin"), updatedBook?.author)
        }

        @Test
        fun `test update book only author`() {
            val authorDto = AuthorRegisterDto("George R.R. Martin", LocalDate.of(1948, 9, 20))
            val bookRegisterDto = BookRegisterDto(
                title = "Game of Thrones",
                price = 49.99,
                publishStatus = true,
                author = listOf(authorDto)
            )

            val bookId = bookRepository.insert(bookRegisterDto)

            val bookUpdateDto = BookUpdateDto(
                title = null,
                price = null,
                publishStatus = null,
                author = listOf(AuthorRegisterDto("G.R.R. Martin", LocalDate.of(1948, 9, 20)))
            )

            bookRepository.update(bookId, bookUpdateDto)
            val updatedBook = bookRepository.findById(bookId)

            assertNotNull(updatedBook)
            assertEquals(bookRegisterDto.title, updatedBook?.title)
            assertEquals(bookRegisterDto.price, updatedBook?.price)
            assertEquals(listOf("G.R.R. Martin"), updatedBook?.author)
        }
    }
}