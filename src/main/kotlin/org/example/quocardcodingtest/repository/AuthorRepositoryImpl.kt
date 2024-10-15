package org.example.quocardcodingtest.repository

import org.example.quocardcodingtest.model.Author
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.model.tables.references.AUTHORS
import org.springframework.stereotype.Repository
import java.time.LocalDate

interface AuthorRepository {
    fun findById(id: Long): Author?
    fun findAll(): List<Author>
    fun insert(name: String, birthDate: LocalDate): Author
    fun update(id: Long, name: String?, birthDate: LocalDate?)
}

@Repository
class AuthorRepositoryImpl(
    private val dslContext: DSLContext
) : AuthorRepository {
    /**
     * get author by id in author table
     * @param id
     */
    override fun findById(id: Long): Author? = wrapDatabaseOperation{
        this.dslContext.select()
            .from(AUTHORS)
            .where(AUTHORS.ID.eq(id))
            .fetchOne()?.let { toModel(it) }
    }

    /**
     * get all authors in author table
     */
    override fun findAll(): List<Author> = wrapDatabaseOperation{
        this.dslContext.select()
            .from(AUTHORS)
            .fetch().map { toModel(it) }
    }

    /**
     * insert author to author table
     * @param name author name
     * @param birthDate author birthday
     */
    override fun insert( name: String, birthDate: LocalDate): Author = wrapDatabaseOperation{
        val record = this.dslContext.newRecord(AUTHORS).also {
            it.name = name
            it.birthdate = birthDate
            it.store()
        }
        return@wrapDatabaseOperation Author(record.id!!, record.name!!, record.birthdate!!)
    }

    /**
     * update author for which param exist in author table
     * @param id author id
     * @param name author name
     * @param birthDate author birthday
     */
    override fun update(id: Long, name: String?, birthDate: LocalDate?): Unit = wrapDatabaseOperation{
        val record = dslContext.newRecord(AUTHORS)

        name?.let { record[AUTHORS.NAME] = it }
        birthDate?.let { record[AUTHORS.BIRTHDATE] = it }

        dslContext.update(AUTHORS).set(record).where(AUTHORS.ID.eq(id)).execute()
    }

    /**
     * transfer author data to entity
     */
    private fun toModel(record: Record) = Author(
        record.getValue(AUTHORS.ID)!!,
        record.getValue(AUTHORS.NAME)!!,
        record.getValue(AUTHORS.BIRTHDATE)!!
    )
}