import generator.RemoveLastMissingInRowColSquare
import generator.RemoveOnlyPossibilityForCell
import generator.RemoveOnlyPossibleForColRowSquare
import generator.SudokuGenerator
import kotlinx.coroutines.*
import solver.*


suspend fun main() = withContext(Dispatchers.Default) {
    val solver = SudokuSolver(
        LastMissingInRowColSquare,
        OnlyPossibleForColRowSquare,
        OnlyPossibilityForCell,
        InRowInSquare,
        HiddenPair,
        NakedPair,
    )
    val generator = SudokuGenerator(
        RemoveLastMissingInRowColSquare,
        InRowInSquare,
        NakedPair,
        HiddenPair,
        RemoveOnlyPossibilityForCell,
        RemoveOnlyPossibleForColRowSquare,
    )
    List(100) {
        async {
            val generationResult = generator.generate()
            val solvedResult = solver.solve(generationResult.sudoku)
            print(".")
            generationResult to solvedResult
        }
    }.awaitAll()
        .sortedBy { it.first.sudoku.countFilled() }
        .take(5)
        .forEach { (generationResult, solvedResult) ->
            println("------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------")
            println("-- Next one ------------------------------------------------------------------------------------------------------------------------------------------------------------------------")
            println("------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------")
            println(generationResult.sudoku.toClearString())
            println(generationResult.sudoku)
            println(generationResult.sudoku.toOutputString())
            println("Filled: " + generationResult.sudoku.countFilled())
            println(generationResult.methodsUsedCounter)
            println(solvedResult.methodsUsedCounter)
            val result = solver.solve(generationResult.sudoku)
            println("Confirmed, that can be solved: ${result.isSolved}")
            println(solvedResult.state)
        }
}

