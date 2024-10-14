package org.example.quocardcodingtest.repository

import org.example.quocardcodingtest.dto.BookRegisterDto
import org.example.quocardcodingtest.dto.BookUpdateDto
import org.example.quocardcodingtest.model.Author
import org.example.quocardcodingtest.model.Book
import org.jooq.Record
import org.jooq.DSLContext
import org.jooq.TableField
import org.jooq.impl.DSL
import org.jooq.model.tables.Authors.Companion.AUTHORS
import org.jooq.model.tables.BookAuthor.Companion.BOOK_AUTHOR
import org.jooq.model.tables.Books.Companion.BOOKS
import org.jooq.model.tables.records.BooksRecord
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

interface BookRepository {
    fun findById(id: Long): Book?
    fun findAll(): List<Book>
    fun insert(bookRegisterDto: BookRegisterDto): Long
    fun update(id: Long, bookUpdateDto: BookUpdateDto)
}

@Transactional
@Repository
class BookRepositoryImpl (
    private val dslContext: DSLContext
) : BookRepository {
    override fun findAll(): List<Book> {
        return dslContext.select(
            BOOKS.ID,
            BOOKS.TITLE,
            BOOKS.PRICE,
            BOOKS.PUBLISHED_STATUS,
            DSL.field("STRING_AGG(${AUTHORS.NAME}, ', ')").`as`("authors") // Specify the alias explicitly
        )
            .from(BOOKS)
            .leftJoin(BOOK_AUTHOR).on(BOOKS.ID.eq(BOOK_AUTHOR.BOOK_ID))
            .leftJoin(AUTHORS).on(BOOK_AUTHOR.AUTHOR_ID.eq(AUTHORS.ID))
            .groupBy(BOOKS.ID) // Group by book ID to aggregate authors
            .fetch()
            .map { record -> toModel(record) }
    }

    override fun findById(id: Long): Book? {
        return dslContext.select(
            BOOKS.ID,
            BOOKS.TITLE,
            BOOKS.PRICE,
            BOOKS.PUBLISHED_STATUS,
            DSL.field("STRING_AGG(${AUTHORS.NAME}, ', ')").`as`("authors") // Specify the alias explicitly
        )
            .from(BOOKS)
            .leftJoin(BOOK_AUTHOR).on(BOOKS.ID.eq(BOOK_AUTHOR.BOOK_ID))
            .leftJoin(AUTHORS).on(BOOK_AUTHOR.AUTHOR_ID.eq(AUTHORS.ID))
            .where(BOOKS.ID.eq(id))
            .groupBy(BOOKS.ID, BOOKS.TITLE, BOOKS.PRICE, BOOKS.PUBLISHED_STATUS) // Group by all non-aggregated fields
            .fetch()
            .map { record -> toModel(record)
            }
            .firstOrNull() // Return the first (or null if not found)
    }

    override fun insert(bookRegisterDto: BookRegisterDto): Long {
        // 1. Insert book into books table and get the generated book ID
        val bookId = dslContext.insertInto(BOOKS)
            .set(BOOKS.TITLE, bookRegisterDto.title)
            .set(BOOKS.PRICE, bookRegisterDto.price.toBigDecimal())
            .set(BOOKS.PUBLISHED_STATUS, bookRegisterDto.publishStatus)
            .returning(BOOKS.ID)
            .fetchOne()
            ?.getValue(BOOKS.ID)
            ?: throw IllegalStateException("Failed to insert book")

        // 2. Insert authors into authors table and collect their IDs
        val authorIds = bookRegisterDto.author.map { authorDto ->
            dslContext.insertInto(AUTHORS)
                .set(AUTHORS.NAME, authorDto.name)
                .set(AUTHORS.BIRTHDATE, authorDto.birthDate)
                .returning(AUTHORS.ID)
                .fetchOne()
                ?.getValue(AUTHORS.ID)
                ?: throw IllegalStateException("Failed to insert author")
        }

        // 3. Insert records into book_author table
        authorIds.forEach { authorId ->
            dslContext.insertInto(BOOK_AUTHOR)
                .set(BOOK_AUTHOR.BOOK_ID, bookId)
                .set(BOOK_AUTHOR.AUTHOR_ID, authorId)
                .execute()
        }

        return bookId
    }

    override fun update(id: Long, bookUpdateDto: BookUpdateDto) {
        // 1. Update the books table
        if (bookUpdateDto.title != null || bookUpdateDto.price != null || bookUpdateDto.publishStatus != null){
            // Initialize SQL builder
            val sqlBuilder = StringBuilder("UPDATE $BOOKS SET ")
            val params = mutableListOf<Any>()  // Store parameters for binding

            // Add fields dynamically
            if (bookUpdateDto.title != null) {
                sqlBuilder.append("title = ?, ")
                params.add(bookUpdateDto.title)
            }
            if (bookUpdateDto.price != null) {
                sqlBuilder.append("price = ?, ")
                params.add(bookUpdateDto.price)
            }
            if (bookUpdateDto.publishStatus != null) {
                sqlBuilder.append("publishStatus = ?, ")
                params.add(bookUpdateDto.publishStatus)
            }

            // Remove the trailing ", " and add the WHERE clause
            sqlBuilder.setLength(sqlBuilder.length - 2)  // Remove last ", "
            sqlBuilder.append(" WHERE id = ?")
            params.add(id)

            // Execute the query with parameter binding
            dslContext.execute(sqlBuilder.toString(), *params.toTypedArray())
        }

        // 2. Delete all existing authors for this book (optional: only if authors were provided)
        if (bookUpdateDto.author?.isNotEmpty() == true) {
            dslContext.deleteFrom(BOOK_AUTHOR)
                .where(BOOK_AUTHOR.BOOK_ID.eq(id))
                .execute()

            // 3. Re-insert new authors into book_author table
            bookUpdateDto.author.forEach { authorDto ->
                val authorId = dslContext.insertInto(AUTHORS)
                    .set(AUTHORS.NAME, authorDto.name)
                    .set(AUTHORS.BIRTHDATE, authorDto.birthDate)
                    .returning(AUTHORS.ID)
                    .fetchOne()
                    ?.getValue(AUTHORS.ID)
                    ?: throw IllegalStateException("Failed to insert author")

                dslContext.insertInto(BOOK_AUTHOR)
                    .set(BOOK_AUTHOR.BOOK_ID, id)
                    .set(BOOK_AUTHOR.AUTHOR_ID, authorId)
                    .execute()
            }
        }
    }

    private fun toModel(record: Record): Book {
        val authorNames = (record["authors"] as String).split(", ") // Split concatenated author names into a list
        return Book(
            id = record[BOOKS.ID]?.toInt() ?: 0,
            title = record[BOOKS.TITLE] ?: "",
            price = record[BOOKS.PRICE]?.toDouble() ?: 0.0,
            publishStatus = record[BOOKS.PUBLISHED_STATUS] ?: false,
            author = authorNames // Use the list of author names
        )
    }

}