package tickerdemo.ticker

import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
class NewsGenerator : NewsProvider {

    private val source = PublishSubject.create<News>()

    init {
        val ticker = Observable.interval(1, TimeUnit.SECONDS)
        ticker.subscribe{
            source.onNext(News(it, System.currentTimeMillis(), "Item $it", "Longer description of $it"))
        }
    }

    override fun news(): Flowable<News> {
        return source.toFlowable(BackpressureStrategy.DROP)
    }

}