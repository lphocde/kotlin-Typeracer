import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlin.math.roundToInt

@Composable
@Preview
fun typeRacerApp() {
    var afficherMenu by remember { mutableStateOf(true) }
    val classement = remember { mutableStateListOf<Pair<String, Int>>() }

    if (afficherMenu) {
        classementMenu(classement) { afficherMenu = false }
    } else {
        typeRacerGame(
            onGameEnd = { nom, mpm ->
                classement.add(nom to mpm)
                afficherMenu = true
            }, onGameCancel = {
                afficherMenu = true
            }

        )
    }
}

@Composable
fun classementMenu(classement: List<Pair<String, Int>>, onStartGame: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Classement", style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold))
        Spacer(modifier = Modifier.height(16.dp))

        classement.sortedByDescending { it.second }.forEachIndexed { index, entry ->
            Text("${index + 1}. ${entry.first}: ${entry.second} MPM", fontSize = 18.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onStartGame) {
            Text("Commencer une partie")
        }
    }
}

@Composable
fun typeRacerGame(onGameEnd: (String, Int) -> Unit, onGameCancel: () -> Unit) {
    val textes = listOf(
        "Aujourd'hui, maman est morte.",
        "phrase d'exemple avant les phrases finales",
        "encore un exemple",
        "dernier exemple"
    )
    var texte by remember { mutableStateOf(textes.random()) }
    var inputUtilisateur by remember { mutableStateOf("") }
    var heureDebut by remember { mutableStateOf<Long?>(null) }
    var mpm by remember { mutableStateOf(0) }
    var texteComplete by remember { mutableStateOf(false) }
    var nomJoueur by remember { mutableStateOf("") }

    // Compute letter-by-letter accuracy
    val texteAnnote = buildAnnotatedString {
        inputUtilisateur.forEachIndexed { index, char ->
            val charCorrect = texte.getOrNull(index)
            val style = if (charCorrect != null && char == charCorrect) {
                SpanStyle(color = Color.Green)
            } else {
                SpanStyle(color = Color.Red)
            }
            withStyle(style) {
                if (charCorrect != null){
                    append(charCorrect)
                }

            }
        }

        // Append any remaining target text in black
        if (inputUtilisateur.length < texte.length) {
            append(texte.substring(inputUtilisateur.length))
        }
    }

    LaunchedEffect(inputUtilisateur) {
        if (inputUtilisateur.isNotEmpty() && !texteComplete) {
            if (heureDebut == null) {
                heureDebut = System.currentTimeMillis()
            }
            val tempsEcoule = (System.currentTimeMillis() - (heureDebut ?: 0)) / 1000.0
            val compteurMots = inputUtilisateur.trim().split(" ").size
            mpm = if (tempsEcoule > 0) (compteurMots / (tempsEcoule / 60)).roundToInt() else 0

            // End game when user reaches full text length
            if (inputUtilisateur.length >= texte.length) {
                texteComplete = true
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (texteComplete) {
            Text("Fin de la partie", style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold))
            Text("Mots par minute : $mpm", style = TextStyle(fontSize = 20.sp, color = Color.Green))
            Spacer(modifier = Modifier.height(16.dp))

            BasicTextField(
                value = nomJoueur,
                onValueChange = { nomJoueur = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.LightGray)
                    .padding(8.dp),
                textStyle = TextStyle(fontSize = 18.sp),
                decorationBox = { innerTextField ->
                    if (nomJoueur.isEmpty()) {
                        Text("Entrez votre nom", color = Color.Gray)
                    }
                    innerTextField()
                }
            )

            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { onGameEnd(nomJoueur, mpm) }) {
                Text("Retourner au menu")
            }
        } else {
            Text(
                text = texteAnnote,
                style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            BasicTextField(
                value = inputUtilisateur,
                onValueChange = { inputUtilisateur = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.LightGray)
                    .padding(8.dp),
                textStyle = TextStyle(fontSize = 18.sp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "mots par minute: $mpm",
                style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.SemiBold, color = Color.Green)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = { onGameCancel() }) {
                Text("Menu")
            }
        }
    }
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        typeRacerApp()
    }
}