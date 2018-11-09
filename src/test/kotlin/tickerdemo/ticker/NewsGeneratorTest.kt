package tickerdemo.ticker

import org.junit.Test
import ticker.ticker.NewsGenerator


internal class NewsGeneratorTest {

    @Test
    fun subscribe() {
        val generator = NewsGenerator()
        generator.news().subscribe {
            println("it = $it")
        }
        Thread.sleep(10000)
    }
}