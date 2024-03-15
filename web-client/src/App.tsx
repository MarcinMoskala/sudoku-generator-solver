import React, {
  useCallback,
  useEffect,
  useMemo,
  useRef,
  useState
} from "react";
import cx from "classnames";
import domtoimage from "dom-to-image";
import { Nullable, Sudoku, SudokuSolver } from "sudoku-generator";

/* eslint import/no-webpack-loader-syntax: off */
// import SudokuSolver from "worker-loader!./workers/solve-sudoku-worker"
// import SudokuGenerator from "worker-loader!./workers/generate-sudoku-worker"
import "./App.scss";
import "antd/dist/antd.min.css";
import { Button, message } from "antd";

type GridDataType = Array<Array<Nullable<number>>>;

export default function App() {
  const solver = useMemo(() => new SudokuSolver(), []);
  const [sudoku, setSudoku] = useState<Sudoku | undefined>();
  const [solutionState, setSolutionState] = useState<GridDataType>([]);
  const inputRefs = useRef<{ [index: string]: HTMLInputElement | null }>({});
  const [editingCell, setEditingCell] = useState<string | null>();
  const [markers, setMarkers] = useState<string[]>([]);
  const [greyCells, setGreyCells] = useState<string[]>([]);
  const removerMethods: string[] = useMemo(() => solver.listRemoverMethods(), [
    solver
  ]);
  const solverMethods: string[] = useMemo(() => solver.listSolverMethods(), [
    solver
  ]);
  const [chosenRemoverMethods, setChosenRemoverMethods] = useState<string[]>(
    SudokuSolver.Companion.DEFAULT_REMOVER_METHODS
  );
  const [chosenSolverMethods, setChosenSolverMethods] = useState<string[]>(
    SudokuSolver.Companion.DEFAULT_SOLVER_METHODS
  );

  const generateSudoku = async () => {
    console.log("generateSudoku");
    setSudoku(undefined);
    const s = solver.generateSudoku(removerMethods, solverMethods);
    setSudoku(s);
    setSolutionState(s.sudoku);
  };

  useEffect(() => {
    generateSudoku();
  }, []);

  const takeScreenshot = useCallback(() => {
    // const node = document.getElementById('app');
    const node = document.getElementById("sudoku-table");
    // @ts-ignore
    if (node === null) return;
    domtoimage
      .toJpeg(node, { quality: 1.0, bgcolor: "#fff" })
      .then(function(dataUrl) {
        const link = document.createElement("a");
        link.download = "my-image-name.jpeg";
        link.href = dataUrl;
        link.click();
      });
  }, []);

  useEffect(() => {
    if (editingCell) {
      if (inputRefs.current) {
        const inputRef = inputRefs.current[editingCell];
        inputRef && inputRef.focus();
      }
    }
  }, [editingCell]);

  useEffect(() => {
    const listener = (event: any) => {
      if (!event.target.closest(".sudoku-tips")) {
        setEditingCell(null);
      }
    };

    document.addEventListener("click", listener);

    return () => {
      document.removeEventListener("click", listener);
    };
  }, []);

  const setValue = (x: number, y: number, value: string) => {
    if (!value.length) {
      message.info(`Only numbers are allowed to input here!`);
      return false;
    }

    const parsedValue = parseInt(value);

    if (!(parsedValue >= 1 && parsedValue <= 9)) {
      message.info(`Only 1-9 numbers are allowed to input here!`);
      return false;
    }

    setSolutionState(prev => {
      const copy = prev.map(row => row.slice());
      copy[y][x] = parsedValue || 0;
      return copy;
    });
  };

  const resetSudoku = () => {
    setMarkers([]);
    setGreyCells([]);
    if (sudoku === null) return;
    setSolutionState(sudoku!.sudoku);
  };

  const toggleMarker = (editingCell: string) => {
    if (!editingCell) return;
    const isMarked = markers.some(m => m === editingCell);
    if (isMarked) {
      setMarkers(markers.filter(m => m !== editingCell));
    } else {
      setMarkers([...markers, editingCell]);
    }
  };

  const toggleGrey = (editingCell: string) => {
    if (!editingCell) return;
    const isMarked = greyCells.some(m => m === editingCell);
    if (isMarked) {
      setGreyCells(greyCells.filter(m => m !== editingCell));
    } else {
      setGreyCells([...greyCells, editingCell]);
    }
  };

  const markerLetterFor = (cell: string) => {
    const markerIndex = markers.indexOf(cell);
    if (markerIndex === -1) return "";
    return String.fromCharCode(65 + markerIndex);
  };

  if (!sudoku) {
    return <div>Loading...</div>;
  }

  return (
    // @ts-ignore
    <div className="app" id="app">
      <div className="app-sudoku">
        <div className="sudoku-container" id="sudoku-container">
          <table className="sudoku-table" id="sudoku-table">
            <tbody>
              {solutionState.map((row, y) => (
                <tr
                  key={y}
                  className={cx({
                    "block-boder": (y + 1) % 9 === 0
                  })}
                >
                  {row.map((value, x) => (
                    <td
                      key={x}
                      className={cx({
                        "block-boder": (x + 1) % 9 === 0,
                        solved: solutionState.some(
                          arr => arr.join() === [x, y].join()
                        ),
                        marked: markers.indexOf([x, y].join()) !== -1,
                        grey: greyCells.indexOf([x, y].join()) !== -1
                      })}
                      onClick={() => setEditingCell([x, y].join())}
                    >
                      <div
                        className={cx({
                          cell: true
                        })}
                      >
                        {markerLetterFor([x, y].join()) !== "" &&
                          markers.length > 1 && (
                            <div className="marker-letter">
                              <span>{markerLetterFor([x, y].join())}</span>
                            </div>
                          )}
                        {greyCells.indexOf([x, y].join()) === -1 &&
                        !value &&
                        editingCell !== [x, y].join() ? (
                          <div className="sudoku-tips">
                            {/*{NUMBERS.map(num => (*/}
                            {/*    <span key={num}>*/}
                            {/*        {sudoku.allowedNumbers(x, y).includes(num) && num}*/}
                            {/*    </span>*/}
                            {/*))}*/}
                          </div>
                        ) : (
                          <input
                            type="text"
                            value={value || ""}
                            onChange={e => setValue(x, y, e.target.value)}
                            onKeyDown={e => {
                              if (e.key === "m") {
                                toggleMarker([x, y].join());
                              } else if (e.key === "g") {
                                toggleGrey([x, y].join());
                              }
                            }}
                            ref={input =>
                              (inputRefs.current[[x, y].join()] = input)
                            }
                          />
                        )}
                      </div>
                    </td>
                  ))}
                </tr>
              ))}
            </tbody>
          </table>
          <div className="sudoku-actions">
            <Button.Group>
              <Button
                type="primary"
                size="large"
                onClick={() => setSolutionState(sudoku!.solved)}
                loading={!sudoku}
              >
                {"Solve Now!"}
              </Button>
              <Button size="large" onClick={() => generateSudoku()}>
                New Sudoku
              </Button>
              <Button size="large" onClick={resetSudoku} loading={!sudoku}>
                Clear All
              </Button>
              <Button size="large" onClick={takeScreenshot}>
                Screenshoot
              </Button>
            </Button.Group>
          </div>
          <div className="methods-choice">
            <div>Remover methods:</div>
            <div>
              {removerMethods.map(method => (
                <span
                  key={method}
                  onClick={() => {
                    if (chosenRemoverMethods.includes(method)) {
                      setChosenRemoverMethods(
                        chosenRemoverMethods.filter(m => m !== method)
                      );
                    } else {
                      setChosenRemoverMethods([
                        ...chosenRemoverMethods,
                        method
                      ]);
                    }
                  }}
                  style={{
                    color: chosenRemoverMethods.includes(method)
                      ? "blue"
                      : "grey"
                  }}
                >
                  {method}{" "}
                </span>
              ))}
            </div>

            <div>Solver methods:</div>
            <div>
              {solverMethods.map(method => (
                <span
                  key={method}
                  onClick={() => {
                    console.log("clicked method", method);
                    if (chosenSolverMethods.includes(method)) {
                      setChosenSolverMethods(
                        chosenSolverMethods.filter(m => m !== method)
                      );
                    } else {
                      setChosenSolverMethods([...chosenSolverMethods, method]);
                    }
                  }}
                  style={{
                    color: chosenSolverMethods.includes(method)
                      ? "blue"
                      : "grey"
                  }}
                >
                  {method}{" "}
                </span>
              ))}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
