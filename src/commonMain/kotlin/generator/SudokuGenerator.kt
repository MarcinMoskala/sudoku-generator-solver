package generator

import SudokuState
import filterFilledCells
import solver.SudokuSolver


class SudokuGenerator(
    private val methods: List<SudokuRemoverMethod> = listOf(
        RemoveLastMissingInRowColSquare,
        RemoveOnlyPossibilityForCell,
        RemoveOnlyPossibleForColRowSquare,
    ),
) {
    constructor(vararg methods: SudokuRemoverMethod) : this(methods.toList())

    // Generate first using removers, and then try to remove until solver is not able to solve
    fun generate(solver: SudokuSolver, minFilled: Int = 0): GeneratorResult {
        val firstResult = generate(minFilled)
        var state = firstResult.sudoku
        var solverUser = 0
        fun result() = GeneratorResult(
            sudoku = state,
            solved = firstResult.solved,
            methodsUsedCounter = firstResult.methodsUsedCounter +
                    mapOf("solver" to solverUser).takeIf { solverUser > 0 }.orEmpty()
        )

        while (true) {
            if (state.countFilled() <= minFilled) return result()
            state = state.cells
                .filterFilledCells()
                .map { (pos, _) -> pos }
                .shuffled()
                .asSequence()
                .map { state.withEmptyUpdatePoss(it) }
                .firstOrNull { solver.solve(it).isSolved }
                ?.also { solverUser++ }
                ?: return result()
        }
    }

    fun generate(minFilled: Int = 0): GeneratorResult {
        val solved = generateSolved()
        var state = solved
        var methodsUsedCounter = methods.associate { it.name to 0 }
        fun result() = GeneratorResult(state, solved, methodsUsedCounter)
        while (true) {
            val (newState, method) = makeStep(state) ?: return result()
            state = newState
            methodsUsedCounter += (method.name to (methodsUsedCounter[method.name] ?: 0) + 1)
            if (state.countFilled() <= minFilled) return result()
        }
    }

    fun generateSolved(): SudokuState {
        // Sudoku is not always correct, so we need to try until we get a correct one
        while (true) {
            runCatching {
                var state = SudokuState.empty()
                SudokuState.ALL_NUMBERS.shuffled().forEachIndexed { col, value ->
                    state = state.withValueAndUpdatedPoss(SudokuState.Position(0, col), value)
                }
                repeat(8) { row ->
                    repeat(9) { col ->
                        state = state.fillWithRandomUpdatePoss(SudokuState.Position(row + 1, col))
                    }
                }
                return state
            }
        }
    }

    fun makeStep(state: SudokuState): Pair<SudokuState, SudokuRemoverMethod>? = methods
        .firstNotNullOfOrNull { method -> method.apply(state)?.let { it to method } }
}











