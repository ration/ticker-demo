package tickerdemo.ticker

import io.reactivex.Flowable

interface NewsProvider {
    fun news(): Flowable<News>
}