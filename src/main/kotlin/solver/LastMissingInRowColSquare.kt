package solver

import SudokuState

object LastMissingInRowColSquare : SudokuSolverMethod {
    override val name: String = "LastMissingInRowColSquare"

    override fun apply(sudokuState: SudokuState): SudokuState? {
        val filledCells = sudokuState.cells
            .toList()
            .filter { it.second is SudokuState.CellState.Empty }

        val rowsColsSquares = listOf(
            filledCells.groupBy { it.first.row }.values,
            filledCells.groupBy { it.first.col }.values,
            filledCells.groupBy { it.first.squareId }.values,
        ).flatten()

        val (pos, state) = rowsColsSquares
            .mapNotNull { it.singleOrNull() }
            .randomOrNull()
            ?: return null

        return sudokuState.withValueAndUpdatedPoss(pos, (state as SudokuState.CellState.Empty).possibilities.single())
    }

}
