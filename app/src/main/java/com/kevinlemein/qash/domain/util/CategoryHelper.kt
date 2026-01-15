package com.kevinlemein.qash.domain.util

object CategoryHelper {

    // The Map: Keyword -> Category Name
    private val merchantMap = mapOf(
        // BILLS & UTILITIES
        "KENYA POWER" to "Bills & Utilities",
        "KPLC" to "Bills & Utilities",
        "ZUKU" to "Bills & Utilities",
        "DSTV" to "Bills & Utilities",
        "GOTV" to "Bills & Utilities",
        "STARLINK" to "Bills & Utilities",
        "WIFI" to "Bills & Utilities",

        // AIRTIME & DATA
        "SAFARICOM DATA" to "Airtime & Data",
        "DATA BUNDLES" to "Airtime & Data",
        "AIRTIME" to "Airtime & Data",
        "AIRTEL" to "Airtime & Data",
        "TELKOM" to "Airtime & Data",
        "FAIBA" to "Airtime & Data",

        // FOOD & GROCERIES
        "NAIVAS" to "Food & Groceries",
        "CARREFOUR" to "Food & Groceries",
        "QUICKMART" to "Food & Groceries",
        "CHANDARANA" to "Food & Groceries",
        "CLEANSHELF" to "Food & Groceries",
        "EASTMATT" to "Food & Groceries",
        "JAZA" to "Food & Groceries",

        // DINING
        "JAVA HOUSE" to "Dining",
        "ARTCAFFE" to "Dining",
        "KFC" to "Dining",
        "PIZZA INN" to "Dining",
        "GALITOS" to "Dining",
        "UBER EATS" to "Dining",
        "GLOVO" to "Dining",

        // TRANSPORT
        "UBER" to "Transport",
        "BOLT" to "Transport",
        "LITTLE" to "Transport",
        "SHELL" to "Transport",
        "TOTAL" to "Transport",
        "RUBIS" to "Transport",
        "OLA" to "Transport",

        // ENTERTAINMENT
        "SHOWMAX" to "Entertainment",
        "NETFLIX" to "Entertainment",
        "SPOTIFY" to "Entertainment",

        // GOVERNMENT
        "ECITIZEN" to "Government",
        "NTSA" to "Government",
        "NHIF" to "Insurance",
        "NSSF" to "Insurance"
    )

    fun categorize(description: String): String {
        val upperDesc = description.uppercase()

        // Loop through map keys. If description contains "KPLC", return "Bills & Utilities"
        for ((keyword, category) in merchantMap) {
            if (upperDesc.contains(keyword)) {
                return category
            }
        }

        return "Uncategorised"
    }
}