package tickerdemo.ticker

data class News(val id: Long,
                val breaking: Boolean,
                val time: Long,
                val description: String,
                val text: String)