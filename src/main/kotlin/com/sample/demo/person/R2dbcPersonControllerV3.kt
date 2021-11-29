package com.sample.demo.person

import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty

@RestController
@RequestMapping("v3")
class R2dbcPersonControllerV3(
    private val r2dbcPersonRepository: R2dbcPersonRepository,
) {
    @GetMapping("/people")
    fun getPeople(): Flux<Person> {
        return r2dbcPersonRepository.findAll()
    }

    @GetMapping("/person/{id}")
    fun getPerson(@PathVariable id: Long): Mono<Person> {
        return r2dbcPersonRepository.findById(id)
            .switchIfEmpty { throw IllegalArgumentException() }
    }

    @PostMapping("/person")
    fun create(@RequestParam name: String): Mono<Person> {
        return r2dbcPersonRepository.save(Person(name = name, age = 100))
    }

    @PutMapping("/person/{id}")
    fun update(@PathVariable id: Long, @RequestParam age: Int): Mono<Person> {
        return r2dbcPersonRepository.findById(id)
            .switchIfEmpty { throw IllegalArgumentException() }
            .flatMap {
                it.age = age
                r2dbcPersonRepository.save(it)
            }
    }

    @DeleteMapping("/person/{id}")
    fun delete(@PathVariable id: Long): Mono<Void> {
        return r2dbcPersonRepository.deleteById(id)
            .onErrorMap { IllegalArgumentException() }
    }
}
