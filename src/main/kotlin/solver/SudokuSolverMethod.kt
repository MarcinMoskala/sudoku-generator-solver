package solver

import SudokuState

interface SudokuSolverMethod {
    val name: String
    fun apply(sudokuState: SudokuState): SudokuState?
}
