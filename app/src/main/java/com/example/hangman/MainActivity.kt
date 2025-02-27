package com.example.hangman

import android.content.res.Configuration
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hangman.ui.theme.HangmanTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HangmanTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    HangmanGame(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun HangmanGame(modifier: Modifier = Modifier) {
    // Game state
    var currentWord by rememberSaveable { mutableStateOf(getRandomWord()) }
    var guessedLetters by rememberSaveable { mutableStateOf(setOf<Char>()) }
    var incorrectGuesses by rememberSaveable { mutableIntStateOf(0) }
    var hintCount by rememberSaveable { mutableIntStateOf(0) }
    var gameOver by rememberSaveable { mutableStateOf(false) }
    var isWin by rememberSaveable { mutableStateOf(false) }
    var hintMessage by rememberSaveable { mutableStateOf("") }

    // Helper to get disabled letters for second hint
    val disabledForHint = rememberSaveable { mutableStateOf(setOf<Char>()) }

    val context = LocalContext.current

    // Function to make a guess
    fun makeGuess(letter: Char) {
        if (gameOver) return

        if (letter !in guessedLetters) {
            val newGuessedLetters = guessedLetters + letter
            guessedLetters = newGuessedLetters

            if (letter.lowercaseChar() !in currentWord.lowercase()) {
                incorrectGuesses++

                if (incorrectGuesses >= 6) {
                    gameOver = true
                    isWin = false
                }
            } else {
                val allLettersGuessed = currentWord.lowercase().all {
                    it == ' ' || it in newGuessedLetters.map { it.lowercaseChar() }
                }
                if (allLettersGuessed) {
                    gameOver = true
                    isWin = true
                }
            }
        }
    }

    // Function to get a hint
    fun getHint() {
        if (incorrectGuesses >= 5 && hintCount >= 1) {
            Toast.makeText(context, "Hint not available", Toast.LENGTH_SHORT).show()
            return
        }

        when (hintCount) {
            0 -> {
                hintMessage = "Baseball related word"
                hintCount++
            }
            1 -> {
                val usedLetters = guessedLetters.map { it.lowercaseChar() }.toSet()
                val wordLetters = currentWord.lowercase().toSet()

                val remainingIncorrectLetters = ('a'..'z')
                    .filter { it !in wordLetters && it !in usedLetters }
                    .toList()

                val toDisable = remainingIncorrectLetters
                    .shuffled()
                    .take(remainingIncorrectLetters.size / 2)
                    .toSet()

                disabledForHint.value = toDisable
                hintCount++
                incorrectGuesses++
            }
            2 -> {
                val vowels = listOf('a', 'e', 'i', 'o', 'u')
                val newGuessed = guessedLetters.toMutableSet()

                vowels.forEach { vowel ->
                    if (vowel !in guessedLetters.map { it.lowercaseChar() }) {
                        newGuessed.add(vowel)
                    }
                }

                guessedLetters = newGuessed
                hintCount++
                incorrectGuesses++
            }
        }
    }

    // Function to start a new game
    fun newGame() {
        currentWord = getRandomWord()
        guessedLetters = setOf()
        incorrectGuesses = 0
        hintCount = 0
        hintMessage = ""
        gameOver = false
        isWin = false
        disabledForHint.value = setOf()
    }

    // Detect orientation
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    // Create the UI based on orientation
    if (isLandscape) {
        // Landscape layout - 3 panels side by side
        Row(modifier = modifier.fillMaxSize()) {
            // Panel 1: Letter buttons
            Card(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "CHOOSE A LETTER",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    LetterGrid(
                        guessedLetters = guessedLetters,
                        disabledForHint = disabledForHint.value,
                        onLetterSelected = { makeGuess(it) },
                        gameOver = gameOver
                    )
                }
            }

            // Panel 2: Hint
            Card(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Button(
                        onClick = { getHint() },
                        enabled = !gameOver && hintCount < 3,
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        Text("HINT")
                    }

                    if (hintMessage.isNotEmpty()) {
                        Text(
                            text = "HINT: $hintMessage",
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { newGame() },
                        modifier = Modifier.padding(top = 16.dp)
                    ) {
                        Text("NEW GAME")
                    }
                }
            }

            // Panel 3: Main game play
            Card(
                modifier = Modifier
                    .weight(2f)
                    .fillMaxHeight()
                    .padding(8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Hangman figure
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        HangmanFigure(incorrectGuesses = incorrectGuesses)
                    }

                    // Word display
                    WordDisplay(
                        word = currentWord,
                        guessedLetters = guessedLetters,
                        gameOver = gameOver
                    )

                    // Game status message
                    if (gameOver) {
                        Text(
                            text = if (isWin) "You Won!" else "You Lost! The word was: $currentWord",
                            fontWeight = FontWeight.Bold,
                            color = if (isWin) Color.Green else Color.Red,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }
                }
            }
        }
    } else {
        // Portrait layout - stacked vertically, no hint button
        Column(
            modifier = modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Main game play - Hangman and word
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Hangman figure
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        HangmanFigure(incorrectGuesses = incorrectGuesses)
                    }

                    // Word display
                    WordDisplay(
                        word = currentWord,
                        guessedLetters = guessedLetters,
                        gameOver = gameOver
                    )

                    // Game status message
                    if (gameOver) {
                        Text(
                            text = if (isWin) "You Won!" else "You Lost! The word was: $currentWord",
                            fontWeight = FontWeight.Bold,
                            color = if (isWin) Color.Green else Color.Red,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    }

                    // New Game button
                    Button(
                        onClick = { newGame() },
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text("NEW GAME")
                    }
                }
            }

            // Letter grid
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "CHOOSE A LETTER",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    LetterGrid(
                        guessedLetters = guessedLetters,
                        disabledForHint = disabledForHint.value,
                        onLetterSelected = { makeGuess(it) },
                        gameOver = gameOver
                    )
                }
            }
        }
    }
}

@Composable
fun LetterGrid(
    guessedLetters: Set<Char>,
    disabledForHint: Set<Char>,
    onLetterSelected: (Char) -> Unit,
    gameOver: Boolean
) {
    val letters = ('A'..'Z').toList()

    // Group letters into rows (7, 7, 7, 5 letters per row)
    val rows = listOf(
        letters.subList(0, 7),
        letters.subList(7, 14),
        letters.subList(14, 21),
        letters.subList(21, 26)
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        rows.forEach { rowLetters ->
            Row(
                modifier = Modifier.padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
            ) {
                rowLetters.forEach { letter ->
                    val isGuessed = letter.lowercaseChar() in guessedLetters.map { it.lowercaseChar() }
                    val isDisabledByHint = letter.lowercaseChar() in disabledForHint
                    val isEnabled = !isGuessed && !isDisabledByHint && !gameOver

                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .border(
                                width = 1.dp,
                                color = Color.Gray,
                                shape = CircleShape
                            )
                            .clickable(enabled = isEnabled) { onLetterSelected(letter) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = letter.toString(),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isEnabled) Color.Black else Color.LightGray
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WordDisplay(
    word: String,
    guessedLetters: Set<Char>,
    gameOver: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        word.forEach { char ->
            val isGuessed = char.lowercaseChar() in guessedLetters.map { it.lowercaseChar() }
            val displayChar = when {
                char == ' ' -> " "
                isGuessed || gameOver -> char.toString()
                else -> "_"
            }

            Text(
                text = displayChar,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.width(24.dp)
            )

            Spacer(modifier = Modifier.width(4.dp))
        }
    }
}

@Composable
fun HangmanFigure(incorrectGuesses: Int) {
    Canvas(
        modifier = Modifier
            .size(200.dp)
            .background(Color.White)
    ) {
        val width = size.width
        val height = size.height
        val strokeWidth = 8f

        // Always draw the gallows (base, pole, beam, rope)
        // Base
        drawLine(
            color = Color.Black,
            start = Offset(width * 0.2f, height * 0.9f),
            end = Offset(width * 0.8f, height * 0.9f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )

        // Vertical pole
        drawLine(
            color = Color.Black,
            start = Offset(width * 0.3f, height * 0.9f),
            end = Offset(width * 0.3f, height * 0.1f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )

        // Horizontal beam
        drawLine(
            color = Color.Black,
            start = Offset(width * 0.3f, height * 0.1f),
            end = Offset(width * 0.7f, height * 0.1f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )

        // Rope
        drawLine(
            color = Color.Black,
            start = Offset(width * 0.7f, height * 0.1f),
            end = Offset(width * 0.7f, height * 0.2f),
            strokeWidth = strokeWidth / 2,
            cap = StrokeCap.Round
        )

        // Draw body parts based on incorrect guesses
        if (incorrectGuesses >= 1) {
            // Head
            drawCircle(
                color = Color.Black,
                radius = width * 0.08f,
                center = Offset(width * 0.7f, height * 0.28f),
                style = Stroke(strokeWidth / 2)
            )
        }

        if (incorrectGuesses >= 2) {
            // Body
            drawLine(
                color = Color.Black,
                start = Offset(width * 0.7f, height * 0.36f),
                end = Offset(width * 0.7f, height * 0.6f),
                strokeWidth = strokeWidth / 2,
                cap = StrokeCap.Round
            )
        }

        if (incorrectGuesses >= 3) {
            // Left arm
            drawLine(
                color = Color.Black,
                start = Offset(width * 0.7f, height * 0.45f),
                end = Offset(width * 0.6f, height * 0.38f),
                strokeWidth = strokeWidth / 2,
                cap = StrokeCap.Round
            )
        }

        if (incorrectGuesses >= 4) {
            // Right arm
            drawLine(
                color = Color.Black,
                start = Offset(width * 0.7f, height * 0.45f),
                end = Offset(width * 0.8f, height * 0.38f),
                strokeWidth = strokeWidth / 2,
                cap = StrokeCap.Round
            )
        }

        if (incorrectGuesses >= 5) {
            // Left leg
            drawLine(
                color = Color.Black,
                start = Offset(width * 0.7f, height * 0.6f),
                end = Offset(width * 0.6f, height * 0.75f),
                strokeWidth = strokeWidth / 2,
                cap = StrokeCap.Round
            )
        }

        if (incorrectGuesses >= 6) {
            // Right leg
            drawLine(
                color = Color.Black,
                start = Offset(width * 0.7f, height * 0.6f),
                end = Offset(width * 0.8f, height * 0.75f),
                strokeWidth = strokeWidth / 2,
                cap = StrokeCap.Round
            )
        }
    }
}

// Function to get a random word for the game
fun getRandomWord(): String {
    val words = listOf(
        "Lucas", "Chen", "Is", "The", "Best",
        "At", "Stuff"
    )
    return words.random()
}

@Preview(showBackground = true)
@Composable
fun HangmanGamePreview() {
    HangmanTheme {
        HangmanGame()
    }
}