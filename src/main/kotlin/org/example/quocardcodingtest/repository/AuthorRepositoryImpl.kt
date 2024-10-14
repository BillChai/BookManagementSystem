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
    fun update(id: Long, name: String?, birthDate: LocalDate?): Int
}

@Repository
class AuthorRepositoryImpl(
    private val dslContext: DSLContext
) : AuthorRepository {
    override fun findById(id: Long): Author? {
        return this.dslContext.select()
            .from(AUTHORS)
            .where(AUTHORS.ID.eq(id))
            .fetchOne()?.let { toModel(it) }
    }

    override fun findAll(): List<Author> {
        return this.dslContext.select()
            .from(AUTHORS)
            .fetch().map { toModel(it) }
    }

    override fun insert( name: String, birthDate: LocalDate): Author {
        val record = this.dslContext.newRecord(AUTHORS).also {
            it.name = name
            it.birthdate = birthDate
            it.store()
        }
        return Author(record.id!!, record.name!!, record.birthdate!!)
    }

    override fun update(id: Long, name: String?, birthDate: LocalDate?): Int {
        // Check if there's anything to update
        if (name == null && birthDate == null) return 0

        // Initialize SQL builder
        val sqlBuilder = StringBuilder("UPDATE authors SET ")
        val params = mutableListOf<Any>()  // Store parameters for binding

        // Add fields dynamically
        if (name != null) {
            sqlBuilder.append("name = ?, ")
            params.add(name)
        }
        if (birthDate != null) {
            sqlBuilder.append("birthdate = ?, ")
            params.add(birthDate)
        }

        // Remove the trailing ", " and add the WHERE clause
        sqlBuilder.setLength(sqlBuilder.length - 2)  // Remove last ", "
        sqlBuilder.append(" WHERE id = ?")
        params.add(id)

        // Execute the query with parameter binding
        return dslContext.execute(sqlBuilder.toString(), *params.toTypedArray())
    }

    private fun toModel(record: Record) = Author(
        record.getValue(AUTHORS.ID)!!,
        record.getValue(AUTHORS.NAME)!!,
        record.getValue(AUTHORS.BIRTHDATE)!!
    )
}