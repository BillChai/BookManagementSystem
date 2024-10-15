package org.example.quocardcodingtest.controller

import org.example.quocardcodingtest.dto.*
import org.example.quocardcodingtest.service.AuthorService
import org.example.quocardcodingtest.service.BookService
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.BDDMockito.given
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import org.example.quocardcodingtest.model.Author
import org.example.quocardcodingtest.model.Book
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import java.time.LocalDate
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath

@WebMvcTest(ApiController::class)
class ApiControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var bookService: BookService

    @MockBean
    private lateinit var authorService: AuthorService

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @BeforeEach
    fun setup() {
        objectMapper.registerModule(JavaTimeModule()) // Register JavaTimeModule for tests
    }

    private var book = Book(
        id = 1,
        title = "Test Title",
        price = 1.0,
        publishStatus = true,
        author = listOf("authorA","authorB","authorC")
    )

    private var author = Author(
        id = 1,
        name = "test author",
        birthDate = LocalDate.of(1998,1,1)
    )

    @Nested
    inner class HealthCheck{
        @Test
        fun `health check should return OK`() {
            mockMvc.perform(get("/api/v1/healthcheck"))
                .andExpect(status().isOk)
        }
    }

    @Nested
    inner class GetBookById{
        @Test
        fun `should get book by ID`() {
            given(bookService.getBookService(1)).willReturn(book)

            mockMvc.perform(get("/api/v1/book/1"))
                .andExpect(status().isOk)
                .andExpect(content().json(objectMapper.writeValueAsString(book)))
        }
    }

    /**
     * test for get books
     */
    @Nested
    inner class GetBooks{
        @Test
        fun `should get all books`() {
            given(bookService.getBooksService(null)).willReturn(listOf(book))

            mockMvc.perform(get("/api/v1/books"))
                .andExpect(status().isOk)
                .andExpect(content().json(objectMapper.writeValueAsString(listOf(book))))
        }
    }

    /**
     * test for update book by id
     */
    @Nested
    inner class UpdateBookById{

        @Test
        fun `update a book`() {

            val updateDto = BookUpdateDto(
                title = "title",
                price = 1.0,
                publishStatus = true,
                author = listOf(
                    AuthorRegisterDto(
                        name = "author",
                        birthDate = LocalDate.of(1998,1,1)
                    )
                )
            )
            given(bookService.updateBookService(1, updateDto)).willReturn(book)

            mockMvc.perform(
                put("/api/v1/book/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateDto))
            )
                .andExpect(status().isOk)
                .andExpect(content().json(objectMapper.writeValueAsString(book)))
        }

        @Test
        fun `update a book with blank author`() {

            val updateDto = BookUpdateDto(
                title = "title",
                price = 1.0,
                publishStatus = true,
                author = listOf(
                    AuthorRegisterDto(
                        name = "",
                        birthDate = LocalDate.of(1998,1,1)
                    )
                )
            )

            given(bookService.updateBookService(1, updateDto)).willReturn(book)

            mockMvc.perform(
                put("/api/v1/book/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateDto))
            )
                // Then: Expect 400 Bad Request with validation errors
                .andExpect(status().isBadRequest)
                .andExpect(content().string("author can't not be blank"))
        }
    }

    /**
     * test for insert book controller and request body validation
     */
    @Nested
    inner class RegisterBook{
        @Test
        fun `should register a new book`() {
            val bookAuthor = listOf(
                AuthorRegisterDto(
                    name = "author",
                    birthDate = LocalDate.of(1998,1,1)
                )
            )
            book.author = listOf("author")
            val bookDto = BookRegisterDto(book.title,book.price,book.publishStatus,bookAuthor)
            given(bookService.insertBookService(bookDto)).willReturn(book)

            mockMvc.perform(
                post("/api/v1/book")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(bookDto))
            )
                .andExpect(status().isOk)
                .andExpect(content().json(objectMapper.writeValueAsString(book)))

            verify(bookService).insertBookService(bookDto)
        }

        @Test
        fun `should return 400 BadRequest when title is missing`() {
            // Given: Invalid DTO without title
            val bookDto = BookRegisterDto(
                title = "",  // Invalid: title is empty
                price = 19.99,
                publishStatus = true,
                author = listOf(
                    AuthorRegisterDto(
                        name = "Author Name",
                        birthDate = LocalDate.of(1998, 1, 1)
                    )
                )
            )

            // When: Send POST request with invalid data
            mockMvc.perform(
                post("/api/v1/book")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(bookDto))
            )
                // Then: Expect 400 Bad Request with validation errors
                .andExpect(status().isBadRequest)
                .andExpect(content().string("title must not be blank"))
        }

        @Test
        fun `should return 400 BadRequest when price is missing`() {
            // Given: Invalid DTO without title
            val bookDto = BookRegisterDto(
                title = "Title",  // Invalid: title is empty
                price = -1.0,
                publishStatus = true,
                author = listOf(
                    AuthorRegisterDto(
                        name = "Author Name",
                        birthDate = LocalDate.of(1998, 1, 1)
                    )
                )
            )

            // When: Send POST request with invalid data
            mockMvc.perform(
                post("/api/v1/book")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(bookDto))
            )
                // Then: Expect 400 Bad Request with validation errors
                .andExpect(status().isBadRequest)
                .andExpect(content().string("price should be over 0"))
        }

        @Test
        fun `should return 400 BadRequest when author name is missing`() {
            // Given: Invalid DTO with empty author name
            val bookDto = BookRegisterDto(
                title = "Valid Book Title",
                price = 19.99,
                publishStatus = true,
                author = listOf(
                    AuthorRegisterDto(
                        name = "",
                        birthDate = LocalDate.of(1998, 1, 1)
                    )
                )
            )

            // When: Send POST request with invalid data
            mockMvc.perform(
                post("/api/v1/book")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(bookDto))
            )
                // Then: Expect 400 Bad Request with validation errors
                .andExpect(status().isBadRequest)
                .andExpect(content().string("author can't not be blank"))
        }

        @Test
        fun `should return 400 BadRequest when author birthdate is future day`() {
            // Given: Invalid DTO with empty author name
            val bookDto = BookRegisterDto(
                title = "Valid Book Title",
                price = 19.99,
                publishStatus = true,
                author = listOf(
                    AuthorRegisterDto(
                        name = "Author",
                        birthDate = LocalDate.now().plusDays(1)
                    )
                )
            )

            // When: Send POST request with invalid data
            mockMvc.perform(
                post("/api/v1/book")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(bookDto))
            )
                // Then: Expect 400 Bad Request with validation errors
                .andExpect(status().isBadRequest)
                .andExpect(content().string("author birthDate can't be future day"))
        }

        @Test
        fun `should return 400 BadRequest when without author`() {
            // Given: Invalid DTO with empty author name
            val bookDto = BookRegisterDto(
                title = "Valid Book Title",
                price = 19.99,
                publishStatus = true,
                author = emptyList<AuthorRegisterDto>()
            )

            // When: Send POST request with invalid data
            mockMvc.perform(
                post("/api/v1/book")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(bookDto))
            )
                // Then: Expect 400 Bad Request with validation errors
                .andExpect(status().isBadRequest)
                .andExpect(content().string("at least one author required"))
        }
    }

    /**
     * test for get author by id
     */
    @Nested
    inner class GetAuthorById{

        @Test
        fun `should get author by ID`() {
            given(authorService.getAuthorService(1)).willReturn(author)

            mockMvc.perform(get("/api/v1/author/1"))
                .andExpect(status().isOk)
                .andExpect(content().json(objectMapper.writeValueAsString(author)))
        }
    }

    /**
     * test for get all author
     */
    @Nested
    inner class GetAllAuthor{

        @Test
        fun `get all authors`() {
            val authors = listOf(author)
            given(authorService.getAuthorsService()).willReturn(authors)

            mockMvc.perform(get("/api/v1/authors"))
                .andExpect(status().isOk)
                .andExpect(content().json(objectMapper.writeValueAsString(authors)))
        }
    }

    /**
     * test for insert author
     */
    @Nested
    inner class InsertAuthor{

        @Test
        fun `register a new author`() {
            val authorRegisterDto = AuthorRegisterDto(
                "author",
                LocalDate.of(1998,1,1)
            )

            given(authorService.insertAuthorService(authorRegisterDto)).willReturn(author)

            mockMvc.perform(
                post("/api/v1/author")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(authorRegisterDto))
            )
                .andExpect(status().isOk)
                .andExpect(content().json(objectMapper.writeValueAsString(author)))

            verify(authorService).insertAuthorService(authorRegisterDto)
        }

        @Test
        fun `register a new author with blank name`() {
            val authorRegisterDto = AuthorRegisterDto(
                "",
                LocalDate.of(1998,1,1)
            )

            mockMvc.perform(
                post("/api/v1/author")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(authorRegisterDto))
            )
                // Then: Expect 400 Bad Request with validation errors
                .andExpect(status().isBadRequest)
                .andExpect(content().string("author can't not be blank"))
        }
    }

    /**
     * test for update author
     */
    @Nested
    inner class UpdateAuthor{
        @Test
        fun `should update an author`() {

            val updateDto = AuthorUpdateDto("Jane Doe Updated", LocalDate.of(1999,1,1))
            given(authorService.updateAuthorService(1, updateDto)).willReturn(author)

            mockMvc.perform(
                put("/api/v1/author/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateDto))
            )
                .andExpect(status().isOk)
                .andExpect(content().json(objectMapper.writeValueAsString(author)))
        }
    }
}
