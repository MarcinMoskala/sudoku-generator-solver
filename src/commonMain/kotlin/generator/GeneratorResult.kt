package generator

import SudokuState

data class GeneratorResult(
    val sudoku: SudokuState,
    val solved: SudokuState,
    val methodsUsedCounter: Map<String, Int>,
)
