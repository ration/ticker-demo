package tickerdemo.ticker

import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject
import org.junit.Assert
import org.junit.Test
import java.time.Duration
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class BackPressureFilterOperatorTest {
    @Test
    fun simpleValuesPass() {
        Flowable.just(1, 2, 3)
            .onBackPressureFilter(Duration.ofSeconds(1)) { true }
            .test()
            .assertSubscribed()
            .assertValues(1, 2, 3)
    }

    @Test
    fun observedOnAnotherThread() {
        Flowable.just(1, 2, 3)
            .onBackPressureFilter(Duration.ofSeconds(1)) { true }
            .observeOn(Schedulers.io())
            .test()
            .assertSubscribed()
            .awaitDone(10, TimeUnit.SECONDS)
            .assertValues(1, 2, 3)
    }

    @Test
    fun observedAndSubscribedOnAnotherThread() {
        Flowable.just(1, 2, 3)
            .onBackPressureFilter(Duration.ofSeconds(1)) { true }
            .observeOn(Schedulers.io())
            .subscribeOn(Schedulers.computation())
            .test()
            .assertSubscribed()
            .awaitDone(10, TimeUnit.SECONDS)
            .assertValues(1, 2, 3)
    }

    @Test
    fun fastIntervalWithBlockedReceiver() {
        val ticker = fastDroppingTicker()
        val latch = CountDownLatch(1)
        ticker
            .onBackPressureFilter(Duration.ofSeconds(1)) {
                latch.countDown()
                true
            }
            .observeOn(Schedulers.io(), false, 1)
            .subscribeOn(Schedulers.computation())
            .subscribe({
                println(it)
                Thread.sleep(2000)
            }, {
                Assert.fail(it.message)
            })
        Assert.assertTrue("Backpressure filter was not triggered", latch.await(10, TimeUnit.SECONDS))

    }

    private fun fastDroppingTicker(): Flowable<Long> {
        val interval = Flowable.interval(1, TimeUnit.MILLISECONDS)
        val ticker = BehaviorSubject.createDefault(0L)
        interval.subscribe {
            ticker.onNext(it)
        }
        return ticker.toFlowable(BackpressureStrategy.DROP)
    }
}
