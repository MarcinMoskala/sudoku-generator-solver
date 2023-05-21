package solver

import SudokuState

object OnlyPossibleForColRowSquare : SudokuSolverMethod {
    override val name: String = "OnlyPossibleForColRowSquare"

    override fun apply(state: SudokuState): SudokuState? {
        return state.cells
            .asSequence()
            .filter { it.value is SudokuState.CellState.Empty }
            .shuffled()
            .mapNotNull { (pos, value) ->
                if (value as? SudokuState.CellState.Empty == null) return@mapNotNull null
                val onlyPossibleValue =
                    value.possibilities.shuffled().find { isOnlyPossibleInColRowSquare(state, pos, it) }
                        ?: return@mapNotNull null
                state.withValueAndUpdatedPoss(pos, onlyPossibleValue)
            }
            .firstOrNull()
    }

    fun isOnlyPossibleInColRowSquare(state: SudokuState, pos: SudokuState.Position, number: Int): Boolean {
        val emptyCellsInTheSameColRowSquare = state.cells.filter {
            it.value is SudokuState.CellState.Empty &&
                    it.key != SudokuState.Position(pos.row, pos.col) &&
                    (it.key.col == pos.col || it.key.row == pos.row || it.key.squareId == pos.squareId)
        }
        val none = emptyCellsInTheSameColRowSquare.none { number in (it.value as SudokuState.CellState.Empty).possibilities }
        return none
    }
}
