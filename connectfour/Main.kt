package connectfour

import java.lang.NumberFormatException

fun main() {
    println("Connect Four")
    println("First player's name:")
    val name1 = readLine()!!
    println("Second player's name:")
    val name2 = readLine()!!

    while (true) {
        val board = getBoard()
        getGameType(name1, name2, board)
        println("Game over!")
        break
    }
}

private fun getGameType(name1: String, name2: String, board: Board) {
    while (true) {
        try {
            println("Do you want to play single or multiple games?")
            println("For a single game, input 1 or press Enter")
            println("Input a number of games:")
            val numberOfGames = readLine()!!

            val player1 = Player(name1, 'o')
            val player2 = Player(name2, '*')
            val players: MutableList<Player> = mutableListOf(player1, player2)

            val game: Game = if (numberOfGames == "" || numberOfGames == "1") {
                SingleGame(players, board)
            } else {
                MultipleGame(players, board, numberOfGames)
            }
            game.play()
            break
        } catch (e: IncorrectNumberOfGamesException) {
            println(e.message)
        }
    }
}

private fun getBoard(): Board {
    while (true) {
        try {
            println("Set the board dimensions (Rows x Columns)")
            println("Press Enter for default (6 x 7)")
            val dimensions = readLine()!!
            return Board(dimensions)
        } catch (e: Exception) {
            when (e) {
                is BoardDimensionsException, is XMissingException -> {
                    println(e.message)
                }
                else -> throw e
            }
        }
    }
}

class Player(val name: String, val sign: Char, var score: Int = 0)

class GameUtil(private val board: Board) {
    fun validateColumn(command: String) : Int {
        try {
            val column = command.toInt()
            if (column < 1 || column > board.columns) {
                throw ColumnIsOutOfRangeException("The column number is out of range (1 - ${board.columns})")
            }
            return column
        } catch (e: NumberFormatException) {
            throw IncorrectColumnException("Incorrect column number")
        }
    }

    fun checkIfPlayerWon(currentPlayer: Player): Boolean {
        val char = currentPlayer.sign
        val state = board.state
        for (i in state.indices) {
            for (j in 0 until state[i].size) {
                if (state[i][j] == currentPlayer.sign) {
                    if (checkHorizontal(i, j, char) || checkVertical(i, j, char) || checkDiagonal(i, j, char)) {
                        return true
                    }
                }
            }
        }
        return false
    }

    private fun checkDiagonal(i: Int, j: Int, char: Char): Boolean {
        val state = board.state
        var count = 0
        //-1 -1; +1 +1
        var i1 = 3
        var j1 = 3
        while (i1 >= 0 && j1 >= 0) {
            if (i - i1 < 0 || j - j1 < 0) {
                break
            }
            if (state[i - i1][j - j1] == char) {
                count++
            }
            if (count == 4) {
                return true
            }
            i1--
            j1--
        }

        //-1 +1; +1 -1
        count = 0
        var i2 = 3
        var j2 = 3
        while (i2 >= 0 && j2 <= 3) {
            if (i - i2 < 0 || j + j2 >= state[i].size) {
                break
            }
            if (state[i - i2][j + j2] == char) {
                count++
            }
            if (count == 4) {
                return true
            }
            i2--
            j2--
        }
        return false
    }

    private fun checkVertical(i: Int, j: Int, char: Char): Boolean {
        var count = 0
        for (k in i downTo i - 3) {
            if (k < 0) {
                break
            }
            if (board.state[k][j] == char) {
                count++
            } else {
                break
            }
        }
        if (count == 4) {
            return true
        }
        return false
    }

    private fun checkHorizontal(i: Int, j: Int, char: Char): Boolean {
        var count = 0
        for (k in j - 3..j) {
            if (k < 0) {
                break
            }
            if (board.state[i][k] == char) {
                count++
            } else {
                break
            }
        }
        if (count == 4) {
            return true
        }
        return false
    }
}

interface Game {
    fun play()
}

class MultipleGame(
    private val players: MutableList<Player>,
    private val board: Board,
    private val numberOfGames: String): Game {
    init {
        try {
            numberOfGames.toInt()
        } catch (e: NumberFormatException) {
            throw IncorrectNumberOfGamesException("Invalid input")
        }
        require(numberOfGames.toInt() > 0) { throw IncorrectNumberOfGamesException("Invalid input") }
    }
    override fun play() {
        gameInfo()
        var playerCounter = 1
        for (i in 1..numberOfGames.toInt()) {
            board.clear()
            println("Game #$i")
            println(board.drawBoard())
            while (true) {
                val currentPlayer: Player = if (playerCounter % 2 == 1) {
                    players[0]
                } else {
                    players[1]
                }
                println("${currentPlayer.name}'s turn:")
                val command = readLine()!!
                if (command == "end") {
                    println("Game over!")
                    break
                }

                try {
                    val gameUtil = GameUtil(board)
                    val column = gameUtil.validateColumn(command)
                    board.addPlayerToken(column, currentPlayer)
                    println(board.drawBoard())
                    val isWon = gameUtil.checkIfPlayerWon(currentPlayer)
                    if (isWon) {
                        println("Player ${currentPlayer.name} won")
                        currentPlayer.score += 2
                        break
                    } else if (board.isFull()) {
                        println("It is a draw")
                        players[0].score++
                        players[1].score++
                        break
                    }
                } catch (e: Exception) {
                    when(e) {
                        is ColumnIsOutOfRangeException, is IncorrectColumnException -> {
                            println(e.message)
                        }
                        else -> throw e
                    }
                    continue
                } finally {
                    playerCounter++
                }
            }
            println(score())
        }
    }

    private fun score() = "Score\n${players[0].name}: ${players[0].score} ${players[1].name}: ${players[1].score}"

    private fun gameInfo() {
        println(this)
    }

    override fun toString(): String {
        return "${players[0].name} VS ${players[1].name}\n$board\nTotal $numberOfGames games"
    }
}

class SingleGame(private val players: MutableList<Player>, private val board: Board): Game {
    override fun play() {
        gameInfo()
        println(board.drawBoard())
        var playerCounter = 1
        while (true) {
            val currentPlayer: Player = if (playerCounter % 2 == 1) {
                players[0]
            } else {
                players[1]
            }
            println("${currentPlayer.name}'s turn:")
            val command = readLine()!!
            if (command == "end") {
                println("Game over!")
                break
            }

            try {
                val gameUtil = GameUtil(board)
                val column = gameUtil.validateColumn(command)
                board.addPlayerToken(column, currentPlayer)
                println(board.drawBoard())
                val isWon = gameUtil.checkIfPlayerWon(currentPlayer)
                if (isWon) {
                    println("Player ${currentPlayer.name} won")
                    break
                } else if (board.isFull()) {
                    println("It is a draw")
                    break
                }
            } catch (e: Exception) {
                println(e.message)
                continue
            }
            playerCounter++
        }
    }

    private fun gameInfo() {
        println(this)
    }

    override fun toString(): String {
        return "${players[0].name} VS ${players[1].name}\n$board\nSingle game"
    }
}

class Board(dimensions: String) {
    var state: Array<CharArray>
    private val rows: Int
    val columns: Int
    private val dimensions = dimensions.lowercase().replace("\\s".toRegex(), "")
    init {
        if (this.dimensions != "") {
            if (this.dimensions.matches(Regex("\\d+x\\d+"))) {
                val params = this.dimensions.split("x").map { it.toInt() }
                rows = params[0]
                columns = params[1]
                if (rows > 9 || rows < 5) {
                    throw BoardDimensionsException("Board rows should be from 5 to 9")
                }
                if (columns > 9 || columns < 5) {
                    throw BoardDimensionsException("Board columns should be from 5 to 9")
                }
            } else {
                throw XMissingException("Invalid input")
            }
        } else {
            rows = 6
            columns = 7
        }
        state = Array(rows) { CharArray(columns){' '} }
    }

    override fun toString(): String {
        return "$rows X $columns board"
    }

    fun drawBoard(): String {
        val header = " " + (1..columns).joinToString(" ")
        var body = ""
        for (i in 1..rows) {
            body += state[i - 1].joinToString ("║", "║","║\n")
        }
        var footer = "╚"
        for (i in 0..columns - 2) {
            footer += "═╩"
        }
        footer += "═╝"
        return header + "\n" + body + footer
    }

    fun addPlayerToken(column: Int, currentPlayer: Player) {
        val emptyRow = checkFirstEmptyRowInColumn(column)
        if (emptyRow == -1) {
            throw ColumnFullException("Column $column is full")
        }
        if (state[emptyRow - 1][column - 1] == ' ') {
            state[emptyRow - 1][column - 1] = currentPlayer.sign
        }
    }

    private fun checkFirstEmptyRowInColumn(column: Int): Int {
        for (i in rows downTo 1) {
            if (state[i - 1][column - 1] == ' ') {
                return i
            }
        }
        return -1
    }

    fun isFull(): Boolean {
        for (i in 0 until rows) {
            for (j in 0 until columns) {
                if (state[i][j] == ' ') {
                    return false
                }
            }
        }
        return true
    }

    fun clear() {
        state = Array(rows) { CharArray(columns){' '} }
    }
}

class XMissingException(message: String) : Exception(message)
class BoardDimensionsException(message: String) : Exception(message)
class ColumnFullException(message: String) : Exception(message)
class ColumnIsOutOfRangeException(message: String) : Exception(message)
class IncorrectColumnException(message: String) : Exception(message)
class IncorrectNumberOfGamesException(message: String) : Exception(message)
