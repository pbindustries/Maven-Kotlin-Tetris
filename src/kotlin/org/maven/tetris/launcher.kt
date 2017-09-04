package org.maven.tetris

import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import javax.swing.JFrame

private val serialVersionUID = -8715353373678321308L

fun launch() {
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