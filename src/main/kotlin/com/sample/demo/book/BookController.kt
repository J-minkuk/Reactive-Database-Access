package com.sample.demo.book

import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.data.mongodb.core.ReactiveMongoOperations
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.util.function.Tuple2
import java.time.Duration
import java.time.Duration.between
import java.time.LocalDateTime

@RestController
class BookController(
    private val reactiveBookRepository: ReactiveBookRepository,
    private val reactiveMongoOperations: ReactiveMongoOperations,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @GetMapping("/search/books")
    fun readWithMongoOperation(): Flux<Book> {
        val query = Query.query(
            Criteria("title").regex("Mars")
        ).limit(100)

        return reactiveMongoOperations.find(query, Book::class.java)
    }

    @GetMapping("/books-paging")
    fun readWithPaging(@RequestParam page: Int): Flux<Book> {
//        return reactiveBookRepository.findAllByAuthors(Flux.just("Andy Weir"), PageRequest.of(page, 1))
        return reactiveBookRepository.findAllBy(PageRequest.of(page, 1))
    }

    @GetMapping("/books")
    fun read() {
        fun reportResults(message: String, books: Flux<Book>) {
            with(books) {
                map(Book::toString)
                    .reduce(StringBuilder()) { sb: StringBuilder, b: String? ->
                        sb.append(" - ")
                            .append(b)
                            .append("\n")
                    }
                    .doOnNext { sb -> logger.info("$message \n$sb") }
                    .subscribe()
            }
        }

        val books1 = reactiveBookRepository.findAll()
        reportResults("All books in DB: ", books1)

        val books2 = reactiveBookRepository.findAllByAuthorsOrderByPublishingYearDesc(Flux.just("Andy Weir"))
        reportResults("All books by Andy Weir: ", books2)
    }

    @PostMapping("/books")
    fun create() {
        val books = Flux.just(
            Book(title = "The Martian", publishingYear = 2011, authors = listOf("Andy Weir")),
            Book(title = "Blue Mars", publishingYear = 1996, authors = listOf("Kim Stanley Robinson")),
            Book(title = "Artemis", publishingYear = 2021, authors = listOf("Toss Bank")),
        )

        reactiveBookRepository.saveAll(books)
            .then()
            .doOnSuccess { logger.info("book save onSuccess") }
            .subscribe()
    }

    @PutMapping("/books")
    fun update() {
        val start = LocalDateTime.now()
        val title = Mono.delay(Duration.ofSeconds(1L))
            .thenReturn("Artemis")
            .doOnSubscribe { logger.info("Subscribed for title") }
            .doOnNext { t -> logger.info("Book title resolved: $t") }

        val publishingYear = Mono.delay(Duration.ofSeconds(2L))
            .thenReturn(2017)
            .doOnSubscribe { logger.info("Subscribed for publishing year") }
            .doOnNext { t -> logger.info("New publishing year resolved: $t") }

        updatedBookYearByTitle3(title, publishingYear)
            .doOnNext { b -> logger.info("Publishing year updated for book: $b") }
            .hasElement()
            .doOnSuccess { status -> logger.info("Updated finished: ${if (status) "successfully" else "fail"}, ${between(start, LocalDateTime.now())}") }
            .subscribe()
    }

    private fun updatedBookYearByTitle(title: Mono<String>, newPublishingYear: Mono<Int>): Mono<Book> {
        return reactiveBookRepository.findByTitle(title)
            .flatMap { book ->
                newPublishingYear.flatMap { year ->
                    book.publishingYear = year
                    reactiveBookRepository.save(book)
                }
            }
    }

    private fun updatedBookYearByTitle2(title: Mono<String>, newPublishingYear: Mono<Int>): Mono<Book> {
        return Mono.zip(title, newPublishingYear).flatMap { data: Tuple2<String, Int> ->
            reactiveBookRepository.findByTitle(Mono.just(data.t1)).flatMap { book ->
                book.publishingYear = data.t2
                reactiveBookRepository.save(book)
            }
        }
    }

    private fun updatedBookYearByTitle3(title: Mono<String>, newPublishingYear: Mono<Int>): Mono<Book> {
        return Mono.zip(newPublishingYear, reactiveBookRepository.findByTitle(title))
            .flatMap { data: Tuple2<Int, Book> ->
                data.t2.publishingYear = data.t1
                reactiveBookRepository.save(data.t2)
            }
    }
}
