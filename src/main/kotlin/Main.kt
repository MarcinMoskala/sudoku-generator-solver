data class SudokuState(
    val cells: Map<Position, CellState>,
    val countOnlyPossibleInColRowSquare: Int = 0,
    val countOnlyPossibilityForCell: Int = 0,
) {
    data class Position(val row: Int, val col: Int) {
        override fun toString(): String = "($row, $col)"
        val squareId get() = (row / 3) * 10 + (col / 3)
    }

    fun countFilled(): Int = cells.count { it.value is CellState.Filled }

    override fun toString(): String = cells
        .toList()
        .groupBy { it.first.row }
        .map { (_, row) ->
            row.joinToString(separator = " ") { cell ->
                when (val value = cell.second) {
                    is CellState.Empty -> " "
                    is CellState.Filled -> value.value.toString()
                }
            }
        }
        .joinToString(separator = "\n")

    sealed interface CellState {
        data class Empty(val possibilities: Set<Int> = ALL_NUMBERS) : CellState
        data class Filled(val value: Int) : CellState
    }

    fun removeOnlyPossibilityForCell(): SudokuState? {
        return cells
            .asSequence()
            .filter { it.value is CellState.Filled }
            .shuffled()
            .mapNotNull { (pos, original) ->
                val stateAfterRemoval = this.withEmptyWithPossibilities(pos.row, pos.col)
                val isOnlyPossibility = (stateAfterRemoval.cells[pos] as? CellState.Empty)?.possibilities?.size == 1
                if (isOnlyPossibility) {
                    stateAfterRemoval.copy(countOnlyPossibilityForCell = countOnlyPossibilityForCell + 1)
                } else null
            }
            .firstOrNull()
    }

    // Should remove an element, that is the only possibility in a square, row or column,
    // because this value can't be in any other cell in this square, row or column
    fun removeOnlyPossibleInColRowSquare(): SudokuState? {
        return cells
            .asSequence()
            .filter { it.value is CellState.Filled }
            .shuffled()
            .mapNotNull { (pos, original) ->
                val originalValue = (original as? CellState.Filled)?.value ?: return@mapNotNull null
                val stateAfterRemoval = this.withEmptyWithPossibilities(pos.row, pos.col)
                val isOnlyPossibility = stateAfterRemoval.isOnlyPossibleInColRowSquare(pos, originalValue)
                if (isOnlyPossibility) {
                    stateAfterRemoval.copy(countOnlyPossibleInColRowSquare = countOnlyPossibleInColRowSquare + 1)
                } else null
            }
            .firstOrNull()
    }

    fun isOnlyPossibleInColRowSquare(pos: Position, number: Int): Boolean {
        val emptyCells = cells.filter { it.value is CellState.Empty && it.key != Position(pos.row, pos.col) }
        fun isOnlyMissingOrThereAreOtherPoss(cells: Map<Position, CellState>): Boolean =
            cells.isEmpty() || cells.any { number in (it.value as CellState.Empty).possibilities }

        return !(
                isOnlyMissingOrThereAreOtherPoss(emptyCells.filter { it.key.col == pos.col }) ||
                        isOnlyMissingOrThereAreOtherPoss(emptyCells.filter { it.key.row == pos.row }) ||
                        isOnlyMissingOrThereAreOtherPoss(emptyCells.filter {
                            theSameSquare(
                                it.key.row,
                                it.key.col,
                                pos.row,
                                pos.col
                            )
                        })
                )
    }

    // Should remove the element that is the only possibility for this cell based on possible options

    fun removeOneMissing(): SudokuState? {
        val filledColumns: List<Pair<String, Int>> = cells.toList().groupBy { it.first.col }
            .filter { (_, cells) ->
                cells.all { it.second is CellState.Filled }
            }
            .map { (col, _) -> "c" to col }

        val filledRows: List<Pair<String, Int>> = cells.toList().groupBy { it.first.row }
            .filter { (_, cells) ->
                cells.all { it.second is CellState.Filled }
            }
            .map { (row, _) -> "r" to row }

        val filledSquares: List<Pair<String, Int>> =
            cells.toList().groupBy { (it.first.row / 3) * 10 + (it.first.col / 3) }
                .filter { (_, cells) ->
                    cells.all { it.second is CellState.Filled }
                }
                .map { (square, _) -> "s" to square }

        val options: List<Pair<String, Int>> = (filledColumns + filledRows + filledSquares)
        val (type, num) = options.toList().randomOrNull() ?: return null
        return when (type) {
            "c" -> withEmptyWithoutPossibilities((0..8).random(), num)
            "r" -> withEmptyWithoutPossibilities(num, (0..8).random())
            "s" -> {
                val rowStart = (num / 10) * 3
                val colStart = (num % 10) * 3
                withEmptyWithoutPossibilities((rowStart..rowStart + 2).random(), (colStart..colStart + 2).random())
            }

            else -> error("Unknown type $type")
        }
    }

    fun withNumberUpdatePossibilities(row: Int, col: Int, number: Int): SudokuState {
        val newCells = cells.map { (currPos, cellState) ->
            currPos to if (currPos.row == row && currPos.col == col) {
                CellState.Filled(number)
            } else {
                if (theSameRowColumnSquare(currPos.col, col, currPos.row, row)) {
                    when (cellState) {
                        is CellState.Empty ->
                            cellState.copy(possibilities = cellState.possibilities - number)

                        is CellState.Filled -> {
                            cellState
                        }
                    }
                } else {
                    cellState
                }
            }
        }.toMap()
        return copy(cells = newCells)
    }

    fun withEmptyWithoutPossibilities(row: Int, col: Int): SudokuState {
        return copy(cells = cells + (Position(row, col) to CellState.Empty()))
    }

    fun withEmptyWithPossibilities(row: Int, col: Int) =
        copy(cells = cells + (Position(row, col) to CellState.Empty()))
            .withUpdatedPossibilities()

    private fun withUpdatedPossibilities(): SudokuState = copy(
        cells = cells.mapValues { (pos, cellState) ->
            if (cellState is CellState.Empty) {
                val possibilities = ALL_NUMBERS - cells.asSequence()
                    .filter { (innerPos, _) -> theSameRowColumnSquare(pos, innerPos) }
                    .mapNotNull { (_, value) -> (value as? CellState.Filled)?.value }
                    .toSet()
                CellState.Empty(possibilities)
            } else {
                cellState
            }
        }
    )

    fun fillWithRandom(row: Int, col: Int): SudokuState =
        when (val cellState = cells.toList().find { it.first.row == row && it.first.col == col }?.second) {
            is CellState.Empty -> {
                val number = cellState.possibilities.random()
                withNumberUpdatePossibilities(row, col, number)
            }

            null, is CellState.Filled -> this
        }

    fun isFilled() = cells.all { it.value is CellState.Filled }

    private fun withFirstRow(elements: List<Int>): SudokuState {
        require(elements.size == 9) { "First row must have 9 elements" }
        var state = this
        elements.forEachIndexed { col, value ->
            state = state.withNumberUpdatePossibilities(0, col, value)
        }
        return state
    }

    fun toOutputString(): String = cells
        .toList()
        .groupBy { it.first.row }
        .map { (_, row) ->
            row.joinToString(separator = "") { cell ->
                when (val value = cell.second) {
                    is CellState.Empty -> "0"
                    is CellState.Filled -> value.value.toString()
                }
            }
        }
        .joinToString(separator = ",")

    fun withValueAndUpdatedPossibilities(pos: Position, value: Int) =
        copy(cells = (cells + (pos to CellState.Filled(value))))
            .withUpdatedPossibilities()
    // TODO
//            .mapValues { (currPos, currState) ->
//                if (theSameRowColumnSquare(pos, currPos) && currState is CellState.Empty) {
//                    currState.copy(possibilities = currState.possibilities - value)
//                } else {
//                    currState
//                }
//            }


    companion object {
        val ALL_NUMBERS = (1..9).toSet()

        fun init(): SudokuState {
            while (true) {
                runCatching {
                    var state = SudokuState.empty()
                    state = state.withFirstRow(ALL_NUMBERS.shuffled())
                    repeat(8) { row ->
                        repeat(9) { col ->
                            state = state.fillWithRandom(row + 1, col)
                        }
                    }
                    return state
                }
            }
        }

        fun empty() = SudokuState(
            (0..8).flatMap { row ->
                (0..8).map { col ->
                    Position(row, col) to CellState.Empty()
                }
            }.toMap()
        )
    }

}

private fun theSameRowColumnSquare(pos1: SudokuState.Position, pos2: SudokuState.Position) =
    pos1 != pos2 && (pos1.col == pos2.col || pos1.row == pos2.row || theSameSquare(pos1, pos2))

private fun theSameSquare(pos1: SudokuState.Position, pos2: SudokuState.Position) =
    pos1 != pos2 && (pos1.row / 3 == pos2.row / 3 && pos1.col / 3 == pos2.col / 3)


private fun theSameRowColumnSquare(currCol: Int, col: Int, currRow: Int, row: Int) =
    currCol == col || currRow == row || theSameSquare(currCol, col, currRow, row)

private fun theSameSquare(currCol: Int, col: Int, currRow: Int, row: Int) =
    (currRow / 3 == row / 3 && currCol / 3 == col / 3)


fun main() {
    // Find sudoku that can be solved with as many last possible in row/col/square assuming the person solving it knows last possible for cell
//    val solver = SudokuSolver(methods = listOf(LastMissingInRowColSquare, OnlyPossibleForColRowSquare, OnlyPossibilityForCell))
//    List(1000) {
//        var state = SudokuState.init()
//        while (true) {
//            state = state.removeOneMissing()
//                ?: state.removeOnlyPossibleInColRowSquare()
//                ?: state.removeOnlyPossibilityForCell()
//                ?: break
//        }
//        val solvedResult = solver.solve(state)
//        print(".")
//        state to solvedResult
//    }.sortedByDescending { it.second.methodsUsedCounter[OnlyPossibleForColRowSquare.name] }
//        .take(5)
//        .forEach { (state, solvedResult) ->
//            println("-------------------------------------")
//            println(state)
//            println(state.toOutputString())
//            println(solvedResult.methodsUsedCounter)
//            val result = solver.solve(state)
//            println("Confirmed, that can be solved: ${result.isSolved}")
//            println(solvedResult.state)
//        }

    // Find sudoku that can be solved with as many last only for cell as possible
    val solver = SudokuSolver(methods = listOf(LastMissingInRowColSquare, OnlyPossibilityForCell))
    List(1000) {
        var state = SudokuState.init()
        while (true) {
            state = state.removeOneMissing()
                ?: state.removeOnlyPossibilityForCell()
                ?: break
        }
        val solvedResult = solver.solve(state)
        print(".")
        state to solvedResult
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

    // Find sudoku that can be solved with as many remove one missing as possible
//    List(1000) {
//        var state = SudokuState.init()
//        while (true) {
//            state = state.removeOneMissing() ?: break
//        }
//        state
//    }.sortedBy { it.countFilled() }
//        .take(5)
//        .forEach { state ->
//            println("-------------------------------------")
//            println(state)
//            println(state.countFilled())
//            println(state.toOutputString())
//            val solver = SudokuSolver(methods = listOf(LastMissingInRowColSquare))
//            val solvedResult = solver.solve(state)
//            val result = solver.solve(state)
//            println("Confirmed, that can be solved with remove one missing: ${result.isSolved}")
//            println(solvedResult.state)
//        }
}

class SudokuSolver(
    private val methods: List<SudokuSolverMethod> = listOf(
        LastMissingInRowColSquare,
        OnlyPossibilityForCell,
        OnlyPossibleForColRowSquare,
    )
) {
    fun solve(sudokuState: SudokuState): Result {
        var state = sudokuState
        val methodsUsedCounter = mutableMapOf<String, Int>()
        while (true) {
            val (method, newState) = methods.firstNotNullOfOrNull { method ->
                method.apply(state)?.let { method to it }
            } ?: methods.firstNotNullOfOrNull { method -> // TODO: Remove
                method.apply(state)?.let { method to it }
            }
            ?: return Result(state, state.isFilled(), methodsUsedCounter)

            state = newState
            methodsUsedCounter[method.name] = (methodsUsedCounter[method.name] ?: 0) + 1
            if (state.isFilled()) {
                return Result(state, true, methodsUsedCounter)
            }
        }
    }

    class Result(
        val state: SudokuState,
        val isSolved: Boolean,
        val methodsUsedCounter: Map<String, Int>
    )
}

interface SudokuSolverMethod {
    val name: String
    fun apply(sudokuState: SudokuState): SudokuState?
}

object OnlyPossibilityForCell : SudokuSolverMethod {
    override val name: String = "OnlyPossibilityForCell"

    override fun apply(sudokuState: SudokuState): SudokuState? {
        return sudokuState.cells
            .asSequence()
            .filter { it.value is SudokuState.CellState.Empty }
            .shuffled()
            .mapNotNull { (pos, value) ->
                if (value as? SudokuState.CellState.Empty == null) return@mapNotNull null
                val onlyCorrectValue = value.possibilities.takeIf { it.size == 1 }?.single() ?: return@mapNotNull null
                sudokuState.withValueAndUpdatedPossibilities(pos, onlyCorrectValue)
            }
            .firstOrNull()
    }
}

object OnlyPossibleForColRowSquare : SudokuSolverMethod {
    override val name: String = "OnlyPossibleForColRowSquare"

    override fun apply(sudokuState: SudokuState): SudokuState? {
        return sudokuState.cells
            .asSequence()
            .filter { it.value is SudokuState.CellState.Empty }
            .shuffled()
            .mapNotNull { (pos, value) ->
                if (value as? SudokuState.CellState.Empty == null) return@mapNotNull null
                val onlyPossibleValue =
                    value.possibilities.shuffled().find { sudokuState.isOnlyPossibleInColRowSquare(pos, it) }
                        ?: return@mapNotNull null
                sudokuState.withValueAndUpdatedPossibilities(pos, onlyPossibleValue)
            }
            .firstOrNull()
    }
}

object LastMissingInRowColSquare : SudokuSolverMethod {
    override val name: String = "LastMissingInRowColSquare"

    override fun apply(sudokuState: SudokuState): SudokuState? = sudokuState.cells
        .toList()
        .let {
            it.groupBy { it.first.row }.mapKeys { "R${it.key}" } +
                    it.groupBy { it.first.col }.mapKeys { "C${it.key}" } +
                    it.groupBy { it.first.squareId }
        }.mapKeys { "S${it.key}" }
        .toList()
        .shuffled()
        .firstNotNullOfOrNull { (_, cellsInGroup: List<Pair<SudokuState.Position, SudokuState.CellState>>) ->
            val missingInRow = cellsInGroup.filter { it.second is SudokuState.CellState.Empty }
            if (missingInRow.size == 1) {
                val missingCell: Pair<SudokuState.Position, SudokuState.CellState> = missingInRow.single()
                val presentValues =
                    cellsInGroup.mapNotNull { it.second as? SudokuState.CellState.Filled }.map { it.value }.toSet()
                val missingValue = SudokuState.ALL_NUMBERS.first { it !in presentValues }
                sudokuState.withValueAndUpdatedPossibilities(missingCell.first, missingValue)
            } else {
                null
            }
        }

}
