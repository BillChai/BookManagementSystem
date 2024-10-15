package org.example.quocardcodingtest.repository

import org.example.quocardcodingtest.dto.BookRegisterDto
import org.example.quocardcodingtest.dto.BookUpdateDto
import org.example.quocardcodingtest.model.Book
import org.jooq.Record
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.jooq.model.tables.Authors.Companion.AUTHORS
import org.jooq.model.tables.BookAuthor.Companion.BOOK_AUTHOR
import org.jooq.model.tables.Books.Companion.BOOKS
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

interface BookRepository {
    fun findById(id: Long): Book?
    fun findAll(): List<Book>
    fun findAllByAuthor(authorName: String): List<Book>
    fun insert(bookRegisterDto: BookRegisterDto): Long
    fun update(id: Long, bookUpdateDto: BookUpdateDto)
}

@Transactional
@Repository
class BookRepositoryImpl(
    private val dslContext: DSLContext,
) : BookRepository {

    /**
     * get all book
     * combine book and author from book table/author table/book_author table
     */
    override fun findAll(): List<Book> = wrapDatabaseOperation {
         dslContext.select(
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

    /**
     * get all book by author name (all same)
     * combine book and author from book table/author table/book_author table
     * @param authorName author name
     */
    override fun findAllByAuthor(authorName: String): List<Book> = wrapDatabaseOperation{
        dslContext.select(
            BOOKS.ID,
            BOOKS.TITLE,
            BOOKS.PRICE,
            BOOKS.PUBLISHED_STATUS,
            DSL.field("STRING_AGG(${AUTHORS.NAME}, ', ')").`as`("authors") // Specify the alias explicitly
        )
            .from(BOOKS)
            .leftJoin(BOOK_AUTHOR).on(BOOKS.ID.eq(BOOK_AUTHOR.BOOK_ID))
            .leftJoin(AUTHORS).on(BOOK_AUTHOR.AUTHOR_ID.eq(AUTHORS.ID))
            .where(AUTHORS.NAME.eq(authorName))
            .groupBy(BOOKS.ID) // Group by book ID to aggregate authors
            .fetch()
            .map { record -> toModel(record) }
    }

    /**
     * get book by id
     * combine book and author from book table/author table/book_author table
     * @param id book id
     */
    override fun findById(id: Long): Book? = wrapDatabaseOperation {
         dslContext.select(
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

    /**
     * insert book
     * combine book and author from book table/author table/book_author table
     * @param bookRegisterDto contain title/price/publish_status and author list
     */
    override fun insert(bookRegisterDto: BookRegisterDto): Long = wrapDatabaseOperation {
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

        return@wrapDatabaseOperation bookId
    }

    /**
     * update book with book param and author
     * if author != null: remove all author and replace with new one
     */
    override fun update(id: Long, bookUpdateDto: BookUpdateDto) = wrapDatabaseOperation {
        // 1. Update the books table
        if (bookUpdateDto.title != null || bookUpdateDto.price != null || bookUpdateDto.publishStatus != null){
            val record = dslContext.newRecord(BOOKS)

            bookUpdateDto.title?.let { record[BOOKS.TITLE] = it }
            bookUpdateDto.price?.let { BigDecimal(it).let { record[BOOKS.PRICE] = it } }
            bookUpdateDto.publishStatus?.let { record[BOOKS.PUBLISHED_STATUS] = it }

            dslContext.update(BOOKS).set(record).where(BOOKS.ID.eq(id)).execute()
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

    /**
     * transfer book data to entity
     */
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