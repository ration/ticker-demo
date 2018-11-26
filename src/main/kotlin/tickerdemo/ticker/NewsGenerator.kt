package tickerdemo.ticker

import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import org.springframework.stereotype.Component
import java.time.Duration
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong


/**
 * Fake news generator. Generates news fake news where 10% is breaking. Variable speed..
 */
@Component
class NewsGenerator : NewsProvider {

    private val source = PublishSubject.create<News>()
    private val generator = Random()
    private var generationSpeed = 100L
    private var tickerSubject = BehaviorSubject.createDefault<Long>(generationSpeed)
    private val counter = AtomicLong(0)


    init {
        createIntervalWithVariableTimer()
    }

    private fun createIntervalWithVariableTimer() {
        tickerSubject.switchMap { Observable.interval(generationSpeed, TimeUnit.MILLISECONDS) }.subscribe {
            val id = counter.incrementAndGet()
            source.onNext(fakeNews(id))
            tickerSubject.onNext(generationSpeed)
        }
    }

    private fun fakeNews(id: Long): News {
        val breaking = generator.nextInt(10) > 8
        return if (breaking) {
            News(id, breaking, System.currentTimeMillis(), "Article $id: Super Important News", "The world is on fire! The World is on Fire!")
        } else {
            News(id, breaking, System.currentTimeMillis(), "Article $id: Fake News", "Some boring news article")
        }
    }

    override fun news(): Flowable<News> {
        return source.toFlowable(BackpressureStrategy.DROP).onBackPressureFilter(Duration.ofSeconds(10)) { !it.breaking }
    }

    override fun setSpeed(speed: Long) {
        generationSpeed = speed
    }
}