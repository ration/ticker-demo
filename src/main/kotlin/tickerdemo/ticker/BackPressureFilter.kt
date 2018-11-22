package tickerdemo.ticker

import io.reactivex.BackpressureOverflowStrategy
import io.reactivex.Flowable
import io.reactivex.functions.Action
import java.time.Duration
import java.util.*
import java.util.concurrent.atomic.AtomicLong
import kotlin.concurrent.schedule

private val sub = AtomicLong(0)
private val subs = mutableSetOf<Long>()
private var priorityFylters = mutableSetOf<Long>()
private var filterTimer: TimerTask? = null

/**
 * Extension function for flowables that on backpressure performs given filter.
 *
 * Note that this is not is very reactive as it has state.
 */
fun <T> Flowable<T>.onBackPressureFilter(duration: Duration, filter: (T) -> Boolean): Flowable<T> {
    val subId = sub.incrementAndGet()
    subs.add(subId)
    return this.doAfterTerminate { subs.remove(subId) }.onBackpressureBuffer(100L, handleBackPressure(duration, subId), BackpressureOverflowStrategy.DROP_OLDEST).filter {
        !priorityFylters.contains(subId) || filter.invoke(it)
    }
}

private fun handleBackPressure(duration: Duration, subId: Long): Action {
    return Action {
        if (!priorityFylters.contains(subId)) {
            filterTimer = Timer("", false).schedule(duration.toMillis()) { priorityFylters.remove(subId) }
        }
        priorityFylters.add(subId)
    }
}

