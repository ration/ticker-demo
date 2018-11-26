package tickerdemo.ticker

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

/**
 * Simple rest controller for changing the speed
 */
@RestController
class NewsController(@Autowired private val newsProvider: NewsProvider) {
    @RequestMapping("/speed/{speed}", method = [RequestMethod.POST])
    fun speed(@PathVariable speed: Long) {
        newsProvider.setSpeed(speed)
    }
}
