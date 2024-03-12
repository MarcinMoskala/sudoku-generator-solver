@file:OptIn(ExperimentalJsExport::class)

import generator.*
import solver.*
import solver.SudokuSolver

@JsExport
class SudokuSolver {
    private val removerMethods: Map<String, SudokuRemoverMethod> = listOf(
        RemoveLastMissingInRowColSquare,
        RemoveOnlyPossibleForColRowSquare,
        RemoveOnlyPossibilityForCell,
        InRowInSquare,
        HiddenPair,
        NakedPair,
        NakedN,
        HiddenN,
        YWing,
    ).associateBy { it.name }

    private val solverMethods: Map<String, SudokuSolverMethod> = listOf(
        LastMissingInRowColSquare,
        OnlyPossibilityForCell,
        OnlyPossibleForColRowSquare,
        InRowInSquare,
        HiddenPair,
        NakedPair,
        NakedN,
        HiddenN,
        YWing,
    ).associateBy { it.name }

    fun generateSudoku(
        useRemoverMethods: Array<String> = DEFAULT_REMOVER_METHODS,
        useSolverMethods: Array<String> = DEFAULT_SOLVER_METHODS,
    ): Sudoku {
        val generator = SudokuGenerator(
            methods = useRemoverMethods.mapNotNull { removerMethods[it] }
        )
        val solver = SudokuSolver(
            methods = useSolverMethods.mapNotNull { solverMethods[it] }
        )
        return generator
            .generate(solver)
            .let { Sudoku(it.sudoku.toJs(), it.solved.toJs()) }
    }

    fun listRemoverMethods(): Array<String> = removerMethods.map { it.value.name }.toTypedArray()
    
    fun listSolverMethods(): Array<String> = solverMethods.map { it.value.name }.toTypedArray()

    companion object {
        val DEFAULT_REMOVER_METHODS = arrayOf(
            RemoveLastMissingInRowColSquare.name,
            RemoveOnlyPossibleForColRowSquare.name,
            RemoveOnlyPossibilityForCell.name,
        )
        val DEFAULT_SOLVER_METHODS = arrayOf(
            LastMissingInRowColSquare.name,
            OnlyPossibilityForCell.name,
            OnlyPossibleForColRowSquare.name,
        )
    }
}

@JsExport
class Sudoku(
    val sudoku: Array<Array<Int?>>,
    val solved: Array<Array<Int?>>,
)

fun SudokuState.toJs(): Array<Array<Int?>> = List(9) { row ->
    List(9) { col ->
        val cell = this.cells[SudokuState.Position(row, col)]
        when (cell) {
            is SudokuState.CellState.Filled -> cell.value
            is SudokuState.CellState.Empty, null -> null
        }
    }.toTypedArray()
}.toTypedArray()
