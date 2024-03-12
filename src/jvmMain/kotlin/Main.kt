import generator.RemoveLastMissingInRowColSquare
import generator.RemoveOnlyPossibilityForCell
import generator.RemoveOnlyPossibleForColRowSquare
import generator.SudokuGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import solver.*


suspend fun main() = withContext(Dispatchers.Default) {
    val generator = SudokuGenerator(
        RemoveLastMissingInRowColSquare,
        RemoveOnlyPossibleForColRowSquare,
        RemoveOnlyPossibilityForCell,
        InRowInSquare,
        HiddenPair,
        NakedPair,
        NakedN,
        HiddenN,
        YWing,
    )

    generator
        .generate(40)
        .let {
            println(it)
        }
}

