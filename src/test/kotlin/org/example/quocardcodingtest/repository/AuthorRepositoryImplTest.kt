package org.example.quocardcodingtest.repository

import org.example.quocardcodingtest.model.Author
import org.jooq.DSLContext
import org.jooq.model.tables.BookAuthor.Companion.BOOK_AUTHOR
import org.jooq.model.tables.references.AUTHORS
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jooq.JooqTest
import org.springframework.context.annotation.Import
import java.time.LocalDate

@JooqTest  // This sets up a jOOQ environment with an in-memory database
@Import(AuthorRepositoryImpl::class)  // Import the repository we are testing
class AuthorRepositoryTest(
    @Autowired private val authorRepository: AuthorRepository,  // Inject the repository
    @Autowired private val dslContext: DSLContext  // Inject DSLContext for setup purposes
) {
    @BeforeEach
    fun setUp() {
        // Delete records from book_author first to avoid foreign key constraint violations
        dslContext.deleteFrom(BOOK_AUTHOR).execute()
        dslContext.deleteFrom(AUTHORS).execute()
    }

    /**
     * test for authorRepository insert function
     */
    @Nested
    inner class InsertAuthor{
        @Test
        fun `insert and check id been inserted by findById function`() {
            val birthDate = LocalDate.of(1985, 5, 20)
            val author = authorRepository.insert("Jane Doe", birthDate)

            // Fetch the inserted author
            val fetchedAuthor = authorRepository.findById(author.id)

            assertNotNull(fetchedAuthor)
            assertEquals("Jane Doe", fetchedAuthor?.name)
            assertEquals(birthDate, fetchedAuthor?.birthDate)
        }
    }

    /**
     * test for authorRepository find by id function
     */
    @Nested
    inner class FindById{
        @Test
        fun `find id when id exist`() {
            val author = authorRepository.insert("Jane Doe", birthDate = LocalDate.of(1985, 5, 20))

            // Fetch the inserted author
            val fetchedAuthor = authorRepository.findById(author.id)
            assertEquals(author.name, fetchedAuthor?.name)
            assertEquals(author.birthDate, fetchedAuthor?.birthDate)
        }

        @Test
        fun `find id when id not exist`() {
            val fetchedAuthor = authorRepository.findById(999)
            assertNull(fetchedAuthor)
        }
    }

    /**
     * test for authorRepository find all function
     */
    @Nested
    inner class FindAll {
        @Test
        fun `find all author with author exist`(){

            authorRepository.insert("Jane Doe One", LocalDate.of(1985, 5, 20))
            authorRepository.insert("Jane Doe Two", LocalDate.of(1985, 5, 20))
            authorRepository.insert("Jane Doe Third", LocalDate.of(1985, 5, 20))

            val fetchedAuthor = authorRepository.findAll()
            assertEquals(fetchedAuthor.size, 3)
        }

        @Test
        fun `find all author with author not exist`(){

            val fetchedAuthor = authorRepository.findAll()
            assertEquals(emptyList<Author>(), fetchedAuthor)
        }
    }

    /**
     * test for authorRepository update function
     */
    @Nested
    inner class UpdateAuthor{
        @Test
        fun `update author with name`() {
            val birthDate = LocalDate.of(1990, 1, 1)
            val author = authorRepository.insert("John Doe", birthDate)

            // Update the author’s name
            authorRepository.update(author.id, "John Smith", null)

            // Verify the update
            val updatedAuthor = authorRepository.findById(author.id)
            assertNotNull(updatedAuthor)
            assertEquals("John Smith", updatedAuthor?.name)
            assertEquals(birthDate, updatedAuthor?.birthDate)
        }

        @Test
        fun `update author with birthday`() {
            val birthDate = LocalDate.of(1990, 1, 1)
            val newBirthDate = LocalDate.of(1991, 1, 1)
            val author = authorRepository.insert("John Doe", birthDate)

            // Update the author’s name
            authorRepository.update(author.id, "John Doe", newBirthDate)

            // Verify the update
            val updatedAuthor = authorRepository.findById(author.id)
            assertNotNull(updatedAuthor)
            assertEquals("John Doe", updatedAuthor?.name)
            assertEquals(newBirthDate, updatedAuthor?.birthDate)
        }

        @Test
        fun `update author with name and birthday`() {
            val birthDate = LocalDate.of(1990, 1, 1)
            val newBirthDate = LocalDate.of(1991, 1, 1)
            val author = authorRepository.insert("John Doe", birthDate)

            // Update the author’s name
            authorRepository.update(author.id, "John Smith", newBirthDate)

            // Verify the update
            val updatedAuthor = authorRepository.findById(author.id)
            assertNotNull(updatedAuthor)
            assertEquals("John Smith", updatedAuthor?.name)
            assertEquals(newBirthDate, updatedAuthor?.birthDate)
        }
    }
}
