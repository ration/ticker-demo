package tickerdemo.ticker

import io.reactivex.schedulers.Schedulers
import org.junit.Test


internal class NewsGeneratorTest {

    @Test
    fun subscribe() {
        val generator = NewsGenerator()
        generator.news().observeOn(Schedulers.computation(), false, 10).subscribe {
            println("${it.id}")
            Thread.sleep(100)
        }
        Thread.sleep(30000)
    }
}