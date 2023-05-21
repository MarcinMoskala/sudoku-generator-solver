package generator

import SudokuState

class SudokuGenerator(
    private val methods: List<SudokuRemoverMethod> = listOf(
        RemoveLastMissingInRowColSquare,
        RemoveOnlyPossibilityForCell,
        RemoveOnlyPossibleForColRowSquare,
    )
) {
    constructor(vararg methods: SudokuRemoverMethod) : this(methods.toList())

    fun generate(): GeneratorResult {
        val solved = generateSolved()
        var state = solved
        var methodsUsedCounter = methods.associate { it.name to 0 }
        while (true) {
            state = methods.firstNotNullOfOrNull { method ->
                method.apply(state)?.also {
                    methodsUsedCounter += (method.name to (methodsUsedCounter[method.name] ?: 0) + 1)
                }
            } ?: return GeneratorResult(
                sudoku = state,
                solved = solved,
                methodsUsedCounter = methodsUsedCounter,
            )
        }
    }

    data class GeneratorResult(
        val sudoku: SudokuState,
        val solved: SudokuState,
        val methodsUsedCounter: Map<String, Int>
    )

    fun generateSolved(): SudokuState {
        // Not always sudoku is correct, so we need to try until we get a correct one
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
}
