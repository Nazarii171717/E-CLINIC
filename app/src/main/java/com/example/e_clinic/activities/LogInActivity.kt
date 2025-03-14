package com.example.e_clinic.activities


import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.TextField
import com.google.ai.client.generativeai.GenerativeModel
import generateChatBotSuggestions
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * An Activity that manages user login functionality. It sets the content to a Composable login screen,
 * handles navigation to other activities, and showcases a Generative AI sample function.
 */
class LogInActivity : ComponentActivity() {
    /**
     * Called when the activity is first created. Sets up edge-to-edge layout and
     * composes the login screen. Also triggers the hello2() Generative AI example.
     *
     * @param savedInstanceState The saved state of the Activity (if any).
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VaccineTrackerTheme {
                LogInScreen(
                    onSignUpClick = {
                        val intent = Intent(this, SignUpActivity::class.java)
                        startActivity(intent)
                    },
                    onSignInSuccess = {
                        val intent = Intent(this, AccountActivity::class.java)
                        startActivity(intent)
                    }
                )
            }
        }
    }
}

/**
 * Demonstrates usage of the GenerativeModel by making a sample request to generate
 * content for a given prompt. Runs asynchronously via a coroutine.
 */
/*private fun hello2() {
    val apiKey = "AIzaSyAvvfN8s_uE11KFb3TOnnlU6lHXEDCYh8w" // Replace with your actual API key
    val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = apiKey
    )

    val prompt = "Write a magic story about a magical dragon"

    MainScope().launch {
        try {
            // Assuming the method to use is 'generateContent', replace with the actual method name as needed
            val response = generativeModel.generateContent(prompt)
            println(response.text)
        } catch (e: Exception) {
            // Handle exception here
            e.printStackTrace()
            println("Error: ${e.message}")
        }
    }
}*/

/**
 * Shows a login screen that allows a user to sign in, sign up, reset password, or log in as admin.
 * Calls sign-in success or sign-up click actions after user validation.
 *
 * @param onSignUpClick A callback triggered when the user clicks the "Sign Up" button.
 * @param onSignInSuccess A callback triggered if user login is successful.
 */
@Composable
fun LogInScreen(
    onSignUpClick: () -> Unit,
    onSignInSuccess: () -> Unit
) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var logInStatus by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Welcome to Vaccine Tracker",
            fontSize = MaterialTheme.typography.headlineSmall.fontSize,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(12.dp)
        )

        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        )

        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (validate(email, password)) {
                    FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val userId = FirebaseAuth.getInstance().currentUser?.uid
                                if (userId != null) {
                                    FirebaseFirestore.getInstance().collection("users")
                                        .document(userId)
                                        .get()
                                        .addOnSuccessListener { document ->
                                            val isAdmin = document.getBoolean("admin") ?: false
                                            if (!isAdmin) {
                                                logInStatus = ""
                                                onSignInSuccess()
                                            } else {
                                                FirebaseAuth.getInstance().signOut()
                                                errorMessage = "You are not authorized as a user."
                                            }
                                        }
                                        .addOnFailureListener { exception ->
                                            errorMessage = "Error fetching user data: ${exception.message}"
                                        }
                                } else {
                                    errorMessage = "User not found."
                                }
                            } else {
                                // errorMessage = task.exception?.message ?: "Authentication failed"
                                errorMessage = "Incorrect password or email"
                            }
                        }
                } else {
                    errorMessage = "Please fill out all the fields"
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFFD700),
                contentColor = Color.Black
            ),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Sign In", fontSize = 16.sp, fontWeight = FontWeight.Medium)
        }

        OutlinedButton(
            onClick = onSignUpClick,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color(0xFFE0B400)
            )
        ) {
            Text("Sign Up")
        }

        Spacer(modifier = Modifier.padding(8.dp))

        Button(
            onClick = {
                if (email.isNotBlank()) {
                    FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                successMessage = "Password reset email sent to $email"
                            } else {
                                errorMessage = task.exception?.message
                            }
                        }
                } else {
                    errorMessage = "Please enter your email address to reset your password"
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFE0B400),
                contentColor = Color.White
            ),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Forgot Password", fontSize = 16.sp, fontWeight = FontWeight.Medium)
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                val intent = Intent(context, AdminLogInActivity::class.java)
                context.startActivity(intent)
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFE0B400),
                contentColor = Color.White
            ),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("I am admin", fontSize = 16.sp, fontWeight = FontWeight.Medium)
        }

        LaunchedEffect(errorMessage, successMessage) {
            if (errorMessage != null || successMessage != null) {
                delay(7000) // Keep message visible for 7 seconds
                errorMessage = null
                successMessage = null
            }
        }

        AnimatedVisibility(
            visible = errorMessage != null || successMessage != null,
            enter = fadeIn(animationSpec = tween(500)) + slideInVertically(initialOffsetY = { it / 2 }),
            exit = fadeOut(animationSpec = tween(500)) + slideOutVertically(targetOffsetY = { it / 2 })
        ) {
            Card(
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (errorMessage != null) Color(0xFFFFE0E0) else Color(0xFFE0FFE0),
                    contentColor = Color.Black
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (errorMessage != null) Icons.Filled.Close else Icons.Filled.CheckCircle,
                        contentDescription = "Status Icon",
                        tint = if (errorMessage != null) Color.Red else Color.Green,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = errorMessage ?: successMessage ?: "",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.weight(1f) // Allow text to expand dynamically
                    )
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Text(
    //     text = logInStatus,
    //     modifier = Modifier.fillMaxWidth(),
    //     color = Color(0xFF0073CE),
    //     fontSize = 16.sp,
    //     textAlign = TextAlign.Center
    // )
}

/**
 * Validates whether email and password are both non-empty strings.
 *
 * @param email The user's email provided in the form.
 * @param password The user's password provided in the form.
 * @return True if both strings are not blank, otherwise false.
 */
fun validate(email: String, password: String): Boolean {
    return email.isNotBlank() && password.isNotBlank()
}

/**
 * Composable preview of the LogInScreen for design-time inspection.
 */
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Preview(showBackground = true)
@Composable
fun LogInScreenPreview() {
    VaccineTrackerTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) {
            LogInScreen(
                onSignUpClick = {},
                onSignInSuccess = {}
            )
        }
    }
}