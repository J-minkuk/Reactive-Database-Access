package com.sample.demo.person

import org.springframework.data.annotation.Id
import org.springframework.data.r2dbc.repository.R2dbcRepository

interface R2dbcPersonRepository : R2dbcRepository<Person, Long> {
}

data class Person(
    @Id
    val id: Long? = null,
    val name: String,
    var age: Int,
)
