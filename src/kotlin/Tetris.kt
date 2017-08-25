import java.awt.Color
import java.awt.Graphics
import java.awt.Point
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import java.util.ArrayList
import java.util.Collections

import javax.swing.JFrame
import javax.swing.JPanel

class Tetris : JPanel() {

    private val Tetraminos = arrayOf(
            // I-Piece
            arrayOf(arrayOf(Point(0, 1), Point(1, 1), Point(2, 1), Point(3, 1)), arrayOf(Point(1, 0), Point(1, 1), Point(1, 2), Point(1, 3)), arrayOf(Point(0, 1), Point(1, 1), Point(2, 1), Point(3, 1)), arrayOf(Point(1, 0), Point(1, 1), Point(1, 2), Point(1, 3))),

            // J-Piece
            arrayOf(arrayOf(Point(0, 1), Point(1, 1), Point(2, 1), Point(2, 0)), arrayOf(Point(1, 0), Point(1, 1), Point(1, 2), Point(2, 2)), arrayOf(Point(0, 1), Point(1, 1), Point(2, 1), Point(0, 2)), arrayOf(Point(1, 0), Point(1, 1), Point(1, 2), Point(0, 0))),

            // L-Piece
            arrayOf(arrayOf(Point(0, 1), Point(1, 1), Point(2, 1), Point(2, 2)), arrayOf(Point(1, 0), Point(1, 1), Point(1, 2), Point(0, 2)), arrayOf(Point(0, 1), Point(1, 1), Point(2, 1), Point(0, 0)), arrayOf(Point(1, 0), Point(1, 1), Point(1, 2), Point(2, 0))),

            // O-Piece
            arrayOf(arrayOf(Point(0, 0), Point(0, 1), Point(1, 0), Point(1, 1)), arrayOf(Point(0, 0), Point(0, 1), Point(1, 0), Point(1, 1)), arrayOf(Point(0, 0), Point(0, 1), Point(1, 0), Point(1, 1)), arrayOf(Point(0, 0), Point(0, 1), Point(1, 0), Point(1, 1))),

            // S-Piece
            arrayOf(arrayOf(Point(1, 0), Point(2, 0), Point(0, 1), Point(1, 1)), arrayOf(Point(0, 0), Point(0, 1), Point(1, 1), Point(1, 2)), arrayOf(Point(1, 0), Point(2, 0), Point(0, 1), Point(1, 1)), arrayOf(Point(0, 0), Point(0, 1), Point(1, 1), Point(1, 2))),

            // T-Piece
            arrayOf(arrayOf(Point(1, 0), Point(0, 1), Point(1, 1), Point(2, 1)), arrayOf(Point(1, 0), Point(0, 1), Point(1, 1), Point(1, 2)), arrayOf(Point(0, 1), Point(1, 1), Point(2, 1), Point(1, 2)), arrayOf(Point(1, 0), Point(1, 1), Point(2, 1), Point(1, 2))),

            // Z-Piece
            arrayOf(arrayOf(Point(0, 0), Point(1, 0), Point(1, 1), Point(2, 1)), arrayOf(Point(1, 0), Point(0, 1), Point(1, 1), Point(0, 2)), arrayOf(Point(0, 0), Point(1, 0), Point(1, 1), Point(2, 1)), arrayOf(Point(1, 0), Point(0, 1), Point(1, 1), Point(0, 2))))

    private val tetraminoColors = arrayOf(Color.cyan, Color.blue, Color.orange, Color.yellow, Color.green, Color.pink, Color.red)

    private var pieceOrigin: Point? = null
    private var currentPiece: Int = 0
    private var rotation: Int = 0
    private val nextPieces = ArrayList<Int>()

    private var score: Long = 0
    private var well: Array<Array<Color>>? = null

    // Creates a border around the well and initializes the dropping piece
    private fun init() {

        well = Array<Array<Color>>(12 , { Array<Color>(24) { Color.BLACK } })
        for (i in 0..11) {
            for (j in 0..23) {
                if (i == 0 || i == 11 || j == 22) {
                    well!![i][j] = Color.GRAY
                } else {
                    well!![i][j] = Color.BLACK
                }
            }
        }
        newPiece()
    }

    // Put a new, random piece into the dropping position
    fun newPiece() {
        pieceOrigin = Point(5, 2)
        rotation = 0
        if (nextPieces.isEmpty()) {
            Collections.addAll(nextPieces, 0, 1, 2, 3, 4, 5, 6)
            Collections.shuffle(nextPieces)
        }
        currentPiece = nextPieces[0]
        nextPieces.removeAt(0)
    }

    // Collision test for the dropping piece
    private fun collidesAt(x: Int, y: Int, rotation: Int): Boolean {
        for (p in Tetraminos[currentPiece][rotation]) {
            if (well!![p.x + x][p.y + y] !== Color.BLACK) {
                return true
            }
        }
        return false
    }

    // Rotate the piece clockwise or counterclockwise
    fun rotate(i: Int) {
        var newRotation = (rotation + i) % 4
        if (newRotation < 0) {
            newRotation = 3
        }
        if (!collidesAt(pieceOrigin!!.x, pieceOrigin!!.y, newRotation)) {
            rotation = newRotation
        }
        repaint()
    }

    // Move the piece left or right
    fun move(i: Int) {
        if (!collidesAt(pieceOrigin!!.x + i, pieceOrigin!!.y, rotation)) {
            pieceOrigin!!.x += i
        }
        repaint()
    }

    // Drops the piece one line or fixes it to the well if it can't drop
    fun dropDown() {
        if (!collidesAt(pieceOrigin!!.x, pieceOrigin!!.y + 1, rotation)) {
            pieceOrigin!!.y += 1
        } else {
            fixToWell()
        }
        repaint()
    }

    // Make the dropping piece part of the well, so it is available for
    // collision detection.
    fun fixToWell() {
        for (p in Tetraminos[currentPiece][rotation]) {
            well!![pieceOrigin!!.x + p.x][pieceOrigin!!.y + p.y] = tetraminoColors[currentPiece]
        }
        clearRows()
        newPiece()
    }

    fun deleteRow(row: Int) {
        for (j in row - 1 downTo 1) {
            for (i in 1..10) {
                well!![i][j + 1] = well!![i][j]
            }
        }
    }

    // Clear completed rows from the field and award score according to
    // the number of simultaneously cleared rows.
    fun clearRows() {
        var gap: Boolean
        var numClears = 0

        var j = 21
        while (j > 0) {
            gap = false
            for (i in 1..10) {
                if (well!![i][j] === Color.BLACK) {
                    gap = true
                    break
                }
            }
            if (!gap) {
                deleteRow(j)
                j += 1
                numClears += 1
            }
            j--
        }

        when (numClears) {
            1 -> score += 100
            2 -> score += 300
            3 -> score += 500
            4 -> score += 800
        }
    }

    // Draw the falling piece
    private fun drawPiece(g: Graphics) {
        g.color = tetraminoColors[currentPiece]
        for (p in Tetraminos[currentPiece][rotation]) {
            g.fillRect((p.x + pieceOrigin!!.x) * 26,
                    (p.y + pieceOrigin!!.y) * 26,
                    25, 25)
        }
    }

    public override fun paintComponent(g: Graphics) {
        // Paint the well
        g.fillRect(0, 0, 26 * 12, 26 * 23)
        for (i in 0..11) {
            for (j in 0..22) {
                g.color = well!![i][j]
                g.fillRect(26 * i, 26 * j, 25, 25)
            }
        }

        // Display the score
        g.color = Color.WHITE
        g.drawString("" + score, 19 * 12, 25)

        // Draw the currently falling piece
        drawPiece(g)
    }

    companion object {

        private val serialVersionUID = -8715353373678321308L

        @JvmStatic fun main(args: Array<String>) {
            val f = JFrame("Tetris")
            f.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
            f.setSize(12 * 26 + 10, 26 * 23 + 25)
            f.isVisible = true

            val game = Tetris()
            game.init()
            f.add(game)

            // Keyboard controls
            f.addKeyListener(object : KeyListener {
                override fun keyTyped(e: KeyEvent) {}

                override fun keyPressed(e: KeyEvent) {
                    when (e.keyCode) {
                        KeyEvent.VK_UP -> game.rotate(-1)
                        KeyEvent.VK_DOWN -> game.rotate(+1)
                        KeyEvent.VK_LEFT -> game.move(-1)
                        KeyEvent.VK_RIGHT -> game.move(+1)
                        KeyEvent.VK_SPACE -> {
                            game.dropDown()
                            game.score += 1
                        }
                    }
                }

                override fun keyReleased(e: KeyEvent) {}
            })

            // Make the falling piece drop every second
            object : Thread() {
                override fun run() {
                    while (true) {
                        try {
                            Thread.sleep(1000)
                            game.dropDown()
                        } catch (e: InterruptedException) {
                        }

                    }
                }
            }.start()
        }
    }
}