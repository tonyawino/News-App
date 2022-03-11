package co.ke.tonyoa.nytimesnews.domain.models

sealed class OrderBy(val orderDirection: OrderDirection) {
    class Title(orderDirection: OrderDirection = OrderDirection.ASCENDING) : OrderBy(orderDirection)
    class Date(orderDirection: OrderDirection = OrderDirection.DESCENDING) : OrderBy(orderDirection)
    class Category(orderDirection: OrderDirection = OrderDirection.ASCENDING) :
        OrderBy(orderDirection)

    class Author(orderDirection: OrderDirection = OrderDirection.ASCENDING) :
        OrderBy(orderDirection)

    class Source(orderDirection: OrderDirection = OrderDirection.ASCENDING) :
        OrderBy(orderDirection)
}

enum class OrderDirection {
    ASCENDING,
    DESCENDING
}
