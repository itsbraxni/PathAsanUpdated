package com.pathasan.app.data

data class EmergencyContact(
    val nameEnglish: String,
    val nameFilipino: String,
    val phoneNumber: String?,
    val officialSourceUrl: String,
)

object EmergencyContacts {
    private const val BAGUIO_HOTLINES_SOURCE = "https://main.baguio.gov.ph/others/hotlines"

    val verified = listOf(
        EmergencyContact(
            nameEnglish = "National Emergency Hotline",
            nameFilipino = "Pambansang Emergency Hotline",
            phoneNumber = "911",
            officialSourceUrl = BAGUIO_HOTLINES_SOURCE,
        ),
        EmergencyContact(
            nameEnglish = "Baguio City Police Office",
            nameFilipino = "Baguio City Police Office",
            phoneNumber = "911",
            officialSourceUrl = BAGUIO_HOTLINES_SOURCE,
        ),
        EmergencyContact(
            nameEnglish = "Baguio Emergency Medical Services",
            nameFilipino = "Baguio Emergency Medical Services",
            phoneNumber = "911",
            officialSourceUrl = BAGUIO_HOTLINES_SOURCE,
        ),
        EmergencyContact(
            nameEnglish = "Bureau of Fire Protection — Baguio",
            nameFilipino = "Bureau of Fire Protection — Baguio",
            phoneNumber = "911",
            officialSourceUrl = BAGUIO_HOTLINES_SOURCE,
        ),
        EmergencyContact(
            nameEnglish = "Baguio CDRRMO",
            nameFilipino = "Baguio CDRRMO",
            phoneNumber = null,
            officialSourceUrl = BAGUIO_HOTLINES_SOURCE,
        ),
    )
}
