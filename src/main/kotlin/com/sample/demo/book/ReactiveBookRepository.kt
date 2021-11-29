package com.sample.demo.book

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface ReactiveBookRepository : ReactiveMongoRepository<Book, ObjectId> {
    fun findByTitle(title: Mono<String>): Mono<Book>

    fun findAllByAuthorsOrderByPublishingYearDesc(authors: Flux<String>): Flux<Book>

    fun findAllByAuthors(authors: Flux<String>, pageable: Pageable): Flux<Book>

    fun findAllBy(pageable: Pageable): Flux<Book>
}

@Document(collection = "book")
data class Book(
    @Id
    val id: ObjectId? = null,

    @Indexed
    val title: String,

    @Indexed
    val authors: List<String>,

    @Field("pubYear")
    var publishingYear: Int,
)
