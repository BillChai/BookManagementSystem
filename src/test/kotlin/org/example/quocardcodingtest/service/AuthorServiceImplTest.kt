package org.example.quocardcodingtest.service

import org.example.quocardcodingtest.dto.AuthorRegisterDto
import org.example.quocardcodingtest.dto.AuthorUpdateDto
import org.example.quocardcodingtest.exception.InternalServerErrorException
import org.example.quocardcodingtest.exception.ResourceNotFoundException
import org.example.quocardcodingtest.exception.UpdateBadRequestException
import org.example.quocardcodingtest.model.Author
import org.example.quocardcodingtest.repository.AuthorRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import java.time.LocalDate

class AuthorServiceImplTest {

    private lateinit var authorRepository: AuthorRepository
    private lateinit var authorService: AuthorServiceImpl

    @BeforeEach
    fun setUp() {
        authorRepository = mock()
        authorService = AuthorServiceImpl(authorRepository)
    }


    /**
     * test for author service getAuthorsService function
     */
    @Nested
    inner class GetAuthorService{
        @Test
        fun `getAuthorsService should return all authors`() {
            // Arrange
            val authors = listOf(
                Author(1, "Author One", LocalDate.of(1990, 1, 1)),
                Author(2, "Author Two", LocalDate.of(1992, 2, 2))
            )
            `when`(authorRepository.findAll()).thenReturn(authors)

            // Act
            val result = authorService.getAuthorsService()

            // Assert
            assertEquals(2, result.size)
            assertEquals("Author One", result[0].name)
        }

        @Test
        fun `getAuthorService should return author when found by id`() {
            // Arrange
            val author = Author(1, "Author One", LocalDate.of(1990, 1, 1))
            `when`(authorRepository.findById(1)).thenReturn(author)

            // Act
            val result = authorService.getAuthorService(1)

            // Assert
            assertEquals("Author One", result.name)
        }

        @Test
        fun `getAuthorService should throw ResourceNotFoundException when author not found`() {
            // Arrange
            `when`(authorRepository.findById(1)).thenReturn(null)

            // Act & Assert
            assertThrows(ResourceNotFoundException::class.java) {
                authorService.getAuthorService(1)
            }
        }
    }

    /**
     * test for insert author Service
     */
    @Nested
    inner class InsertAuthorService{
        @Test
        fun `insertAuthorService should insert author and return it`() {
            // Arrange
            val authorDto = AuthorRegisterDto("New Author", LocalDate.of(2000, 5, 5))
            val author = Author(1, "New Author", LocalDate.of(2000, 5, 5))
            `when`(authorRepository.insert(authorDto.name, authorDto.birthDate)).thenReturn(author)

            // Act
            val result = authorService.insertAuthorService(authorDto)

            // Assert
            assertEquals("New Author", result.name)
        }
    }

    /**
     test for update author Service
     */
    @Nested
    inner class UpdateAuthorService{
        @Test
        fun `updateAuthorService should update and return author`() {
            // Arrange
            val author1 = Author(1, "Author One", LocalDate.of(1990, 1, 1))
            val author2 = Author(1, "Updated Author", LocalDate.of(1991, 1, 1))
            val updateDto = AuthorUpdateDto(name = author2.name, birthDate = author2.birthDate)

            `when`(authorRepository.findById(1)).thenReturn(author1).thenReturn(author2) // Mock the findById call to return an existing author
            doNothing().`when`(authorRepository).update(1, updateDto.name, updateDto.birthDate) // Mock the update call

            // Act
            val result = authorService.updateAuthorService(1, updateDto)

            // Assert
            assertEquals(author2.name, result.name)
            assertEquals(LocalDate.of(1991, 1, 1), result.birthDate)
        }

        @Test
        fun `updateAuthorService should throw ResourceNotFoundException if author not found`() {
            // Arrange
            val updateDto = AuthorUpdateDto(name = "Updated Author", birthDate = LocalDate.of(1991, 1, 1))
            `when`(authorRepository.findById(1)).thenReturn(null)

            // Act & Assert
            assertThrows(ResourceNotFoundException::class.java) {
                authorService.updateAuthorService(1, updateDto)
            }
        }

        @Test
        fun `updateAuthorService should throw UpdateBadRequestException if updateDto is invalid`() {
            // Arrange
            val invalidDto = AuthorUpdateDto(name = null, birthDate = null)

            // Act & Assert
            assertThrows(UpdateBadRequestException::class.java) {
                authorService.updateAuthorService(1, invalidDto)
            }
        }

        @Test
        fun `updateAuthorService should throw InternalServerErrorException if author not found after update`() {
            // Arrange
            val updateDto = AuthorUpdateDto(name = "Updated Author", birthDate = LocalDate.of(1991, 1, 1))
            val author = Author(1, "Author One", LocalDate.of(1990, 1, 1))

            `when`(authorRepository.findById(1)).thenReturn(author).thenReturn(null) // First for existence, second after update
            doNothing().`when`(authorRepository).update(1, updateDto.name, updateDto.birthDate) // Mock the update call

            // Act & Assert
            assertThrows(InternalServerErrorException::class.java) {
                authorService.updateAuthorService(1, updateDto)
            }
        }
    }
}