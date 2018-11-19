package tickerdemo.ticker

import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.TimeUnit

@Component
class NewsGenerator : NewsProvider {

    private val source = PublishSubject.create<News>()
    private val generator = Random()

    init {
        val ticker = Observable.interval(10, TimeUnit.MILLISECONDS)
        ticker.subscribe{
            source.onNext(News(it, generator.nextInt(5), System.currentTimeMillis(), "Item $it"))
        }
    }

    override fun news(): Flowable<News> {
        return source.toFlowable(BackpressureStrategy.DROP)
    }

}