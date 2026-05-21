package com.example.studybuddyapp

data class StudySession(
    var sessionId: String = "",
    var title: String = "",
    var subject: String = "",
    var dateTime: String = "",
    var location: String = "",
    var createdBy: String = "",
    var status: String = "Open"
)