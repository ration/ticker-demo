package tickerdemo.ticker

import com.fasterxml.jackson.databind.ObjectMapper
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import io.rsocket.kotlin.*
import io.rsocket.kotlin.transport.netty.server.NettyContextCloseable
import io.rsocket.kotlin.transport.netty.server.WebsocketServerTransport
import io.rsocket.kotlin.util.AbstractRSocket
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller

/**
 * RSocket endpoint for news. Provides a reactive stream of
 * JSON encoded News items
 */
@Controller
class NewsSocket(@Autowired private val newsProvider: NewsProvider) {
    private val LOG = LoggerFactory.getLogger(this.javaClass.name)
    private val port = 9988
    private val mapper = ObjectMapper()

    // Initializer for the RSocket. Incoming WebSocket requests are handled
    // by this RSocket receiver and handled by the handler() method
    private val closeable: Single<NettyContextCloseable> = RSocketFactory
            .receive()
            .acceptor { { setup, rSocket -> handler(setup, rSocket) } } // server handler RSocket
            .transport(WebsocketServerTransport.create("localhost", port))  // Netty websocket transport
            .start()
    private val subscription = closeable.subscribe({
        LOG.info("subscribed = $it")
    }, {
        LOG.error("it = $it")
    })

    // Handler for the socket. Connects the NewsProvider to the RSocket
    private fun handler(setup: Setup, rSocket: RSocket): Single<RSocket> {
        return Single.just(object : AbstractRSocket() {
            override fun requestStream(payload: Payload): Flowable<Payload> {
                return newsProvider.news().observeOn(Schedulers.io()).map {
                    DefaultPayload.text(mapper.writeValueAsString(it)) }
            }
        })
    }
}