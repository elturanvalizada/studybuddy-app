package com.example.studybuddyapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.studybuddyapp.ui.theme.StudyBuddyAppTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StudyBuddyAppTheme {
                StudyBuddyApp()
            }
        }
    }
}

@Composable
fun StudyBuddyApp() {
    val auth = FirebaseAuth.getInstance()
    var currentScreen by remember {
        mutableStateOf(if (auth.currentUser != null) "list" else "login")
    }

    when (currentScreen) {
        "login" -> LoginScreen(
            onLoginSuccess = { currentScreen = "list" },
            onRegisterClick = { currentScreen = "register" }
        )

        "register" -> RegisterScreen(
            onRegisterSuccess = { currentScreen = "list" },
            onBackToLogin = { currentScreen = "login" }
        )

        "list" -> SessionListScreen(
            onLogout = {
                auth.signOut()
                currentScreen = "login"
            }
        )
    }
}

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit, onRegisterClick: () -> Unit) {
    val auth = FirebaseAuth.getInstance()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.padding(24.dp).fillMaxSize(),
        verticalArrangement = Arrangement.Center
    ) {
        Text("StudyBuddy Login", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                if (email.isBlank() || password.isBlank()) {
                    error = "Email and password are required"
                } else {
                    auth.signInWithEmailAndPassword(email.trim(), password)
                        .addOnSuccessListener { onLoginSuccess() }
                        .addOnFailureListener { error = it.message ?: "Login failed" }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Login")
        }

        TextButton(onClick = onRegisterClick) {
            Text("Create new account")
        }

        if (error.isNotEmpty()) {
            Text(error, color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
fun RegisterScreen(onRegisterSuccess: () -> Unit, onBackToLogin: () -> Unit) {
    val auth = FirebaseAuth.getInstance()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.padding(24.dp).fillMaxSize(),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Create Account", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                if (email.isBlank() || password.isBlank()) {
                    error = "Email and password are required"
                } else if (password.length < 6) {
                    error = "Password must be at least 6 characters"
                } else {
                    auth.createUserWithEmailAndPassword(email.trim(), password)
                        .addOnSuccessListener { onRegisterSuccess() }
                        .addOnFailureListener { error = it.message ?: "Registration failed" }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Register")
        }

        TextButton(onClick = onBackToLogin) {
            Text("Back to login")
        }

        if (error.isNotEmpty()) {
            Text(error, color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
fun SessionListScreen(onLogout: () -> Unit) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val userId = auth.currentUser?.uid ?: ""

    var sessions by remember { mutableStateOf(listOf<StudySession>()) }
    var title by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    var dateTime by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var search by remember { mutableStateOf("") }
    var editingSessionId by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        db.collection("sessions")
            .whereEqualTo("createdBy", userId)
            .addSnapshotListener { snapshot, _ ->
                sessions = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(StudySession::class.java)?.copy(sessionId = doc.id)
                } ?: emptyList()
            }
    }

    val filteredSessions = sessions.filter {
        it.title.contains(search, ignoreCase = true) ||
                it.subject.contains(search, ignoreCase = true)
    }

    Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
        Text("StudyBuddy Sessions", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = onLogout) {
            Text("Logout")
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = search,
            onValueChange = { search = it },
            label = { Text("Search sessions") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = subject, onValueChange = { subject = it }, label = { Text("Subject") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = dateTime, onValueChange = { dateTime = it }, label = { Text("Date and time") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("Location") }, modifier = Modifier.fillMaxWidth())

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                if (title.isBlank() || subject.isBlank()) {
                    error = "Title and subject are required"
                    return@Button
                }

                val session = hashMapOf(
                    "title" to title,
                    "subject" to subject,
                    "dateTime" to dateTime,
                    "location" to location,
                    "createdBy" to userId,
                    "status" to "Open"
                )

                if (editingSessionId == null) {
                    db.collection("sessions").add(session)
                } else {
                    db.collection("sessions").document(editingSessionId!!).update(session as Map<String, Any>)
                    editingSessionId = null
                }

                title = ""
                subject = ""
                dateTime = ""
                location = ""
                error = ""
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (editingSessionId == null) "Add Session" else "Update Session")
        }

        if (error.isNotEmpty()) {
            Text(error, color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(filteredSessions) { session ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clickable {
                            editingSessionId = session.sessionId
                            title = session.title
                            subject = session.subject
                            dateTime = session.dateTime
                            location = session.location
                        }
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(session.title, style = MaterialTheme.typography.titleMedium)
                        Text("Subject: ${session.subject}")
                        Text("Date: ${session.dateTime}")
                        Text("Location: ${session.location}")
                        Text("Status: ${session.status}")

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                db.collection("sessions")
                                    .document(session.sessionId)
                                    .delete()
                            }
                        ) {
                            Text("Delete")
                        }
                    }
                }
            }
        }
    }
}