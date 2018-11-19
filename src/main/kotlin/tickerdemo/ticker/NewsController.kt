package tickerdemo.ticker

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RestController

@RestController
class NewsController(@Autowired private val newsProvider: NewsProvider) {

}
