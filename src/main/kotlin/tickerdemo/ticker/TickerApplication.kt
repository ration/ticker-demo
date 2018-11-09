package tickerdemo.ticker

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class TickerApplication

fun main(args: Array<String>) {
    runApplication<TickerApplication>(*args)
}
