package tickerdemo.ticker

import io.reactivex.BackpressureOverflowStrategy
import io.reactivex.Flowable
import io.reactivex.FlowableOperator
import io.reactivex.FlowableSubscriber
import io.reactivex.functions.Action
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription
import java.time.Duration
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.schedule

/**
 * Extension function for flowables that on backpressure performs given filter.
 *
 * Note that this is not is very reactive as it has state.
 */
fun <T> Flowable<T>.onBackPressureFilter(duration: Duration, filter: (T) -> Boolean): Flowable<T> {
    val backPressureOperator = BackPressureFilterOperator(filter)
    return this.lift(backPressureOperator)
        .onBackpressureBuffer(100L, backPressureOperator.handleBackPressure(duration), BackpressureOverflowStrategy.DROP_OLDEST)
}


class BackPressureFilterOperator<T>(filter: (T) -> Boolean) : FlowableOperator<T, T> {
    private var backPressureSubscriber: BackPressureFilterSubscriber<T> = BackPressureFilterSubscriber(filter, null)
    override fun apply(subscriber: Subscriber<in T>): Subscriber<in T> {
        backPressureSubscriber.downstream = subscriber
        return backPressureSubscriber
    }

    fun handleBackPressure(duration: Duration): Action {
        return backPressureSubscriber.handleBackPressure(duration)
    }


    class BackPressureFilterSubscriber<T>(private val filter: (T) -> Boolean, var downstream: Subscriber<in T>?) : FlowableSubscriber<T>, Subscription {
        private var filterTimer: TimerTask? = null
        private var enabled = AtomicBoolean(false)
        private var upstream: Subscription? = null

        override fun cancel() {
            upstream?.cancel()
        }

        override fun request(n: Long) {
            upstream?.request(n)
        }


        fun handleBackPressure(duration: Duration) = Action {
            if (!enabled.get()) {
                filterTimer = Timer("", false).schedule(duration.toMillis()) { enabled.set(true) }
            }
            enabled.set(true)
        }


        override fun onComplete() {
            downstream?.onComplete()
        }

        override fun onSubscribe(s: Subscription) {
            if (upstream != null) {
                s.cancel()
            } else {
                upstream = s
                downstream?.onSubscribe(this)
            }
        }

        override fun onNext(t: T) {
            // If the filter does drop the element, we need to request for another from upstream to
            // to make the request counts match
            if (enabled.get() && filter.invoke(t)) {
                upstream?.request(1)
            } else {
                downstream?.onNext(t)
            }
        }

        override fun onError(t: Throwable?) {
            downstream?.onError(t)
        }
    }
}