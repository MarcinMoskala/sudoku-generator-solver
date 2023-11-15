package solver

import SudokuState
import firstRepetitionOrNull
import generator.SudokuRemoverMethod

object NakedPair : SudokuSolverMethod, SudokuRemoverMethod {
    override val name: String = "NakedPair"

    override fun apply(state: SudokuState): SudokuState? {
        val emptyCells = state.cells
            .toList()
            .filter { it.second is SudokuState.CellState.Empty }
            .shuffled()

        val rowsColsSquares = listOf(
            emptyCells.groupBy { it.first.row }.values,
            emptyCells.groupBy { it.first.col }.values,
            emptyCells.groupBy { it.first.squareId }.values,
        ).flatten()

        rowsColsSquares
            .forEach { group ->
                val repeatingPair = group.mapNotNull { (_, cell) -> (cell as? SudokuState.CellState.Empty)?.possibilities }
                    .filter { it.size == 2 }
                    .firstRepetitionOrNull()
                    ?: return@forEach

                val otherPositions = group
                    .filter {
                        val poss = (it.second as SudokuState.CellState.Empty).possibilities
                        poss  != repeatingPair && poss.any { it in repeatingPair }
                    }
                    .map { it.first }
                    .toSet()
                if (otherPositions.isEmpty()) return@forEach
                val newState = state.removePossibilities(otherPositions, *repeatingPair.toIntArray())
                return newState
            }
        return null
    }
}

