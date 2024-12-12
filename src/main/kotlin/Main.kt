import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
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
                classement.add(Pair(nom, mpm))
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
        "Va, je ne te hais point.",
        "phrase d'exemple avant les phrases finales",
        "encore un exemple",
        "dernier exemple",
        "Si seulement on pouvait faire du web en C peut-etre la vie serait cool",
        "En gros luden c'est un item mythique qui donne de la pénétration magique",
        "Pourquoi on fait un projet en Kotlin déjà ?",
        "L'UBO c'est vraiment la meilleur institution du monde",
        "Même si on a un gros déficit c'est pas grave on s'amuse bien",
        "Ça fait 1 semaine ils disent qu'ils vont nommer un Premier Ministre demain",
        "Tout ce que j'aime c'est manger des cartes graphiques et des clés USB",
        "Liam Frit est un faux alcolique il sort que le jeudi soir",
        "Mon ex-copine est en surpoids"
        "Lorem ipsum est un texte qui ressemble à du latin mais en fait ça veut rien dire",
        "Les vans Volkswagen Transporter 3 souffrent de graves problèmes de corrosion",
        "Jamel Debbouze a perdu sa main en poussant quelqu'un sous un train"
    )
    var texte by remember { mutableStateOf(textes.random()) }
    var inputUtilisateur by remember { mutableStateOf("") }
    var heureDebut by remember { mutableStateOf<Long?>(null) }
    var mpm by remember { mutableStateOf(0) }
    var texteComplete by remember { mutableStateOf(false) }
    var nomJoueur by remember { mutableStateOf("") }
    var pourcentagePrecision by remember { mutableStateOf(0.0) }

    LaunchedEffect(texteComplete) {
        if (texteComplete) {
            val totalCharacters = texte.length
            var charCorrectCount = 0

            inputUtilisateur.forEachIndexed { index, char ->
                if (index < texte.length && char == texte[index]) {
                    charCorrectCount++
                }
            }

            pourcentagePrecision = (charCorrectCount.toDouble() / totalCharacters * 100).roundToInt().toDouble()
        }
    }

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
            val focusRequester = FocusRequester()

            Text("Fin de la partie", style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold))
            Text("Mots par minute : $mpm", style = TextStyle(fontSize = 20.sp, color = Color.Green))
            Text("PrÃ©cision : $pourcentagePrecision%", style = TextStyle(fontSize = 20.sp, color = Color.Blue))
            Spacer(modifier = Modifier.height(16.dp))

            BasicTextField(
                value = nomJoueur,
                onValueChange = { nomJoueur = it },
                modifier = Modifier
                    .focusRequester(focusRequester)
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

            LaunchedEffect(Unit) {
                focusRequester.requestFocus()
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { onGameEnd(nomJoueur, mpm) }) {
                Text("Sauvegarder et Retourner au menu")
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
