package com.sample.demo.person

import io.r2dbc.spi.Row
import io.r2dbc.spi.RowMetadata
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux

@RestController
@RequestMapping("v1")
class R2dbcPersonControllerV1(
    private val databaseClient: DatabaseClient,
) {
    @GetMapping("/people")
    fun getPeople(): Flux<Person> {
        return databaseClient.sql("select * from person")
            .map { row: Row, _: RowMetadata ->
                Person(
                    id = row.get("id", Long::class.java)!!,
                    name = row.get("name", String::class.java)!!,
                    age = row.get("age", Int::class.java)!!,
                )
            }.all()
    }
}
