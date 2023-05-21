package generator

import SudokuState
import SudokuState.CellState
import solver.OnlyPossibleForColRowSquare

object RemoveOnlyPossibleForColRowSquare : SudokuRemoverMethod {
    override val name: String = "RemoveOnlyPossibleForColRowSquare"

    // Should remove an element, that is the only possibility in a square, row or column,
    // because this value can't be in any other cell in this square, row or column
    override fun apply(state: SudokuState): SudokuState? = state.cells
        .asSequence()
        .filter { it.value is CellState.Filled }
        .shuffled()
        .mapNotNull { (pos, original) ->
            val originalValue = (original as CellState.Filled).value
            val stateAfterRemoval = state.withEmptyUpdatePoss(pos)
            val isOnlyPossibility = isOnlyPossibleInColRowSquare(stateAfterRemoval, pos, originalValue)
            if (isOnlyPossibility) {
                stateAfterRemoval
            } else null
        }
        .firstOrNull()

    private fun isOnlyPossibleInColRowSquare(state: SudokuState, pos: SudokuState.Position, number: Int): Boolean {
        val emptyCells =
            state.cells.filter { it.value is CellState.Empty && it.key != SudokuState.Position(pos.row, pos.col) }

        fun isOnlyMissingOrThereAreOtherPoss(cells: Map<SudokuState.Position, CellState>): Boolean =
            cells.isEmpty() || cells.any { number in (it.value as CellState.Empty).possibilities }

        return !(
                isOnlyMissingOrThereAreOtherPoss(emptyCells.filter { it.key.col == pos.col }) ||
                isOnlyMissingOrThereAreOtherPoss(emptyCells.filter { it.key.row == pos.row }) ||
                isOnlyMissingOrThereAreOtherPoss(emptyCells.filter { it.key.squareId == pos.squareId })
        )
    }
}
