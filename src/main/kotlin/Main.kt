import generator.RemoveLastMissingInRowColSquare
import generator.RemoveOnlyPossibilityForCell
import generator.RemoveOnlyPossibleForColRowSquare
import generator.SudokuGenerator
import solver.LastMissingInRowColSquare
import solver.OnlyPossibilityForCell
import solver.OnlyPossibleForColRowSquare
import solver.SudokuSolver


fun main() {
    val solver = SudokuSolver(
        LastMissingInRowColSquare,
        OnlyPossibleForColRowSquare,
        OnlyPossibilityForCell,
    )
    val generator = SudokuGenerator(
        RemoveLastMissingInRowColSquare,
        RemoveOnlyPossibilityForCell,
        RemoveOnlyPossibleForColRowSquare,
    )
    List(100) {
        val generationResult = generator.generate()
        val solvedResult = solver.solve(generationResult.sudoku)
        print(".")
        generationResult.sudoku to solvedResult
    }.sortedByDescending { it.second.methodsUsedCounter[OnlyPossibilityForCell.name] }
        .take(5)
        .forEach { (state, solvedResult) ->
            println("-------------------------------------")
            println(state)
            println(state.toOutputString())
            println(solvedResult.methodsUsedCounter)
            val result = solver.solve(state)
            println("Confirmed, that can be solved: ${result.isSolved}")
            println(solvedResult.state)
        }
}

