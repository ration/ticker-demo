package tickerdemo.ticker

import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.TimeUnit
import io.reactivex.subjects.BehaviorSubject
import java.util.concurrent.atomic.AtomicLong


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
            source.onNext(generateNews(id))
            tickerSubject.onNext(generationSpeed)
        }
    }

    private fun generateNews(id: Long): News {
        val priority = generator.nextInt(2)
        return if (priority == 0) {
            News(id, priority, System.currentTimeMillis(), "Article $id: Super Important News", "The world is on fire! The World is on Fire!")
        } else {
            News(id, priority, System.currentTimeMillis(), "Article $id: Fake News", "Some boring news article")
        }
    }

    override fun news(): Flowable<News> {
        return source.toFlowable(BackpressureStrategy.DROP)
    }

    override fun setSpeed(speed: Long) {
        generationSpeed = speed
    }

}