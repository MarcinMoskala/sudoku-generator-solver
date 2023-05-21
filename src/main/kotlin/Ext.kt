fun <T> Iterable<T>.firstRepetitionOrNull(): T? {
    val seen = mutableSetOf<T>()
    for (element in this) {
        if (element in seen) return element
        seen.add(element)
    }
    return null
}

fun <K, V> List<Pair<K, V>>.firstRepetitionOfSecond(): Pair<List<K>, V>? {
    val seen = mutableSetOf<V>()
    for (entry in this) {
        if (entry.second in seen) {
            val all = this.filter { it.second == entry.second }.map { it.first }
            return Pair(all, entry.second)
        }
        seen.add(entry.second)
    }
    return null
}
