package com.sample.demo.person

import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.relational.core.query.Criteria
import org.springframework.data.relational.core.query.Query
import org.springframework.data.relational.core.query.Update
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

@RestController
@RequestMapping("v2")
class R2dbcPersonControllerV2(
    private val r2dbcEntityTemplate: R2dbcEntityTemplate,
) {
    @GetMapping("/people")
    fun getPeople2(): Flux<Person> {
        return r2dbcEntityTemplate.select(Person::class.java)
            .all()
    }

    @GetMapping("/people/{id}")
    fun getPeople2ById(@PathVariable id: Long): Mono<Person> {
        return r2dbcEntityTemplate.selectOne(
            Query.query(Criteria.where("id").`is`(id)),
            Person::class.java,
        ).switchIfEmpty(Mono.error(IllegalArgumentException()))
    }

    @PostMapping("/person")
    fun createPerson(@RequestParam name: String): Mono<Person> {
        return r2dbcEntityTemplate.insert(Person::class.java)
            .using(Person(name = name, age = 15))
    }

    @PutMapping("/person/{id}")
    fun update(@PathVariable id: Long, @RequestParam age: Int): Mono<Int> {
        return r2dbcEntityTemplate.selectOne(
            Query.query(Criteria.where("id").`is`(id)),
            Person::class.java,
        ).switchIfEmpty(Mono.error(IllegalArgumentException()))
            .flatMap {
                r2dbcEntityTemplate.update(Person::class.java)
                    .matching(Query.query(Criteria.where("id").`is`(id)))
                    .apply(Update.update("age", age))
            }
    }

    @DeleteMapping("/person/{id}")
    fun delete(@PathVariable id: Long): Mono<Int> {
        return r2dbcEntityTemplate.delete(Person::class.java)
            .matching(Query.query(Criteria.where("id").`is`(id)))
            .all()
            .onErrorMap { IllegalArgumentException() }
    }
}
