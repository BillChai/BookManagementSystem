package org.example.quocardcodingtest.service

import org.example.quocardcodingtest.dto.AuthorRegisterDto
import org.example.quocardcodingtest.dto.AuthorUpdateDto
import org.example.quocardcodingtest.exception.InternalServerErrorException
import org.example.quocardcodingtest.exception.ResourceNotFoundException
import org.example.quocardcodingtest.exception.UpdateBadRequestException
import org.example.quocardcodingtest.model.Author
import org.example.quocardcodingtest.repository.AuthorRepository
import org.springframework.stereotype.Service

interface AuthorService{
    fun getAuthorsService() : List<Author>
    fun getAuthorService(id: Long) : Author
    fun updateAuthorService(id: Long, authorUpdateDto: AuthorUpdateDto) : Author
    fun insertAuthorService(authorRegisterDto: AuthorRegisterDto) : Author
}

@Service
class AuthorServiceImpl (
    private val authorRepository: AuthorRepository
): AuthorService{

    /**
     * get all authors
     */
    override fun getAuthorsService(): List<Author> {
        return authorRepository.findAll()
    }

    /**
     * get author by id
     * @param id author id
     * @return author if author exist else throw not found error
     */
    override fun getAuthorService(id: Long): Author {
        val author = authorRepository.findById(id)
        if (author === null){
            throw ResourceNotFoundException("author not found")
        }
        return author
    }

    /**
     * insert author
     * @param authorRegisterDto
     */
    override fun insertAuthorService(authorRegisterDto: AuthorRegisterDto): Author {
        return authorRepository.insert(authorRegisterDto.name, authorRegisterDto.birthDate)
    }

    /**
     * update author
     * @param id author id
     * @param authorUpdateDto updateDto with name and birthDate
     * @return author
     * if authorUpdateDto.name and authorUpdateDto.birthDate all null throw error
     * if author not found throw not found error
     */
    override fun updateAuthorService(id: Long, authorUpdateDto: AuthorUpdateDto): Author {
        // check authorUpdateDto at least one parameter not null
        if (authorUpdateDto.allPropertiesAreNullExceptAge()) {
            // Throw BadRequestException if both parameters are invalid
            throw UpdateBadRequestException("Author update request must contain at least one valid field.")
        }

        // check author exist
        var author = authorRepository.findById(id)
        if (author === null){
            throw ResourceNotFoundException("author not found")
        }

        // update record
        authorRepository.update(id, authorUpdateDto.name, authorUpdateDto.birthDate)

        // return author
        author = authorRepository.findById(id)
        if (author === null){
            throw InternalServerErrorException("author not found")
        }
        return author
    }
}