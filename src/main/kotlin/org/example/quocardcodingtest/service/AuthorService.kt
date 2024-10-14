package org.example.quocardcodingtest.service

import org.example.quocardcodingtest.dto.AuthorRegisterDto
import org.example.quocardcodingtest.dto.AuthorUpdateDto
import org.example.quocardcodingtest.exception.InternalServerErrorException
import org.example.quocardcodingtest.exception.ResourceNotFoundException
import org.example.quocardcodingtest.exception.UpdateBadRequestException
import org.example.quocardcodingtest.model.Author
import org.example.quocardcodingtest.repository.AuthorRepository
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpServerErrorException.InternalServerError

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
    override fun getAuthorsService(): List<Author> {
        return authorRepository.findAll()
    }

    override fun getAuthorService(id: Long): Author {
        val author = authorRepository.findById(id)
        if (author === null){
            throw ResourceNotFoundException("author not found")
        }
        return author
    }

    override fun insertAuthorService(authorRegisterDto: AuthorRegisterDto): Author {
        return authorRepository.insert(authorRegisterDto.name, authorRegisterDto.birthDate)
    }

    override fun updateAuthorService(id: Long, authorUpdateDto: AuthorUpdateDto): Author {
        // check authorUpdateDto at least one parameter not null
        if (authorUpdateDto.allPropertiesAreNullExceptAge()) {
            // Throw BadRequestException if both parameters are invalid
            throw UpdateBadRequestException("Author update request must contain at least one valid field.")
        }

        // update record
        authorRepository.update(id, authorUpdateDto.name, authorUpdateDto.birthDate)
        val author = authorRepository.findById(id)
        if (author === null){
            throw InternalServerErrorException("author not found")
        }
        return author
    }
}