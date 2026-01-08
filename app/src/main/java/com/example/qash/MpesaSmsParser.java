package com.example.qash;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MpesaSmsParser {

    public static class ParsedTransaction {
        public String mpesaCode;
        public double amount;
        public String type;
        public String description;
        public String category;
        public double newBalance;
        public boolean isValid;

        public boolean usedFuliza;
        public double fulizaOutstanding;
        public double fulizaFee;

        public ParsedTransaction() {
            this.isValid = false;
            this.usedFuliza = false;
        }
    }

    public static ParsedTransaction parse(String smsBody, AppDatabase db) {
        ParsedTransaction result = new ParsedTransaction();

        if (smsBody == null || smsBody.isEmpty()) {
            return result;
        }

        try {
            // Extract M-Pesa code (e.g., QJK5LMNO8P)
            result.mpesaCode = extractMpesaCode(smsBody);

            // Extract amount
            result.amount = extractAmount(smsBody);

            // Determine transaction type and description
            determineTypeAndDescription(smsBody, result);

            // Auto-categorize with database
            result.category = autoCategorize(smsBody, result.description, db);

            // Extract new balance
            result.newBalance = extractBalance(smsBody);

            // Mark as valid if we have amount
            result.isValid = result.amount > 0;

        } catch (Exception e) {
            e.printStackTrace();
            result.isValid = false;
        }

        return result;
    }

    private static String extractMpesaCode(String sms) {
        // M-Pesa codes are typically 10 uppercase alphanumeric characters
        Pattern pattern = Pattern.compile("([A-Z0-9]{10})");
        Matcher matcher = pattern.matcher(sms);

        if (matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }

    private static double extractAmount(String sms) {
        // Match patterns like "Ksh500.00", "Ksh5,000.00", "KSh1,234.56"
        Pattern pattern = Pattern.compile("Ksh?\\s?([0-9,]+\\.?[0-9]*)");
        Matcher matcher = pattern.matcher(sms);

        if (matcher.find()) {
            String amountStr = matcher.group(1).replace(",", "");
            return Double.parseDouble(amountStr);
        }
        return 0.0;
    }

//    private static void determineTypeAndDescription(String sms, ParsedTransaction result) {
//        String smsLower = sms.toLowerCase();
//
//        // Check if this is a Fuliza notification SMS
//        if (smsLower.contains("fuliza m-pesa amount is")) {
//            // This is the second SMS - extract Fuliza info
//            result.usedFuliza = true;
//            result.description = "Fuliza Statement";
//            result.type = "Expense";
//
//            // Extract total outstanding amount
//            Pattern pattern = Pattern.compile("outstanding amount is Ksh\\s?([0-9,]+\\.?[0-9]*)");
//            Matcher matcher = pattern.matcher(sms);
//            if (matcher.find()) {
//                String amountStr = matcher.group(1).replace(",", "");
//                result.fulizaOutstanding = Double.parseDouble(amountStr);
//                result.newBalance = -result.fulizaOutstanding; // Negative balance!
//            }
//
//            // Extract Fuliza fee
//            Pattern feePattern = Pattern.compile("Access Fee charged Ksh\\s?([0-9,]+\\.?[0-9]*)");
//            Matcher feeMatcher = feePattern.matcher(sms);
//            if (feeMatcher.find()) {
//                String feeStr = feeMatcher.group(1).replace(",", "");
//                result.fulizaFee = Double.parseDouble(feeStr);
//            }
//
//            result.isValid = true;
//            return;
//        }
//
//        // Regular transaction types
//        if (smsLower.contains("sent to")) {
//            result.type = "Expense";
//            result.description = extractRecipient(sms, "sent to");
//        }
//
//
//        // Sent money
//        if (smsLower.contains("sent to")) {
//            result.type = "Expense";
//            result.description = extractRecipient(sms, "sent to");
//        }
//        // Received money
//        else if (smsLower.contains("received from") || smsLower.contains("you have received")) {
//            result.type = "Income";
//            result.description = extractRecipient(sms, "received from");
//        }
//        // Withdrew cash
//        else if (smsLower.contains("withdraw") || smsLower.contains("withdrawn")) {
//            result.type = "Expense";
//            result.description = "Cash Withdrawal";
//        }
//        // Bought airtime
//        else if (smsLower.contains("airtime")) {
//            result.type = "Expense";
//            result.description = "Airtime Purchase";
//        }
//        // Paid to merchant (Lipa na M-Pesa)
//        else if (smsLower.contains("paid to")) {
//            result.type = "Expense";
//            result.description = extractMerchant(sms, "paid to");
//        }
//        // Bought goods
//        else if (smsLower.contains("bought goods")) {
//            result.type = "Expense";
//            result.description = extractMerchant(sms, "bought goods");
//        }
//        // Give customer
//        else if (smsLower.contains("give") && smsLower.contains("cash")) {
//            result.type = "Expense";
//            result.description = "Cash Given to Customer";
//        }
//        // PayBill
//        else if (smsLower.contains("paybill")) {
//            result.type = "Expense";
//            result.description = extractPaybillDetails(sms);
//        }
//        // Default
//        else {
//            result.type = "Expense";
//            result.description = "M-Pesa Transaction";
//        }
//    }

    private static void determineTypeAndDescription(String sms, ParsedTransaction result) {
        String smsLower = sms.toLowerCase();

        // Check if this is a Fuliza notification SMS
        if (smsLower.contains("Fuliza M-Pesa amount is")) {
            // This is the second SMS - extract ONLY the Fuliza fee
            result.usedFuliza = true;
            result.description = "Fuliza Access Fee";
            result.type = "Expense";
            result.category = "Fuliza";

            // Extract Fuliza fee (NOT the total amount!)
            Pattern feePattern = Pattern.compile("Access Fee charged Ksh\\s?([0-9,]+\\.?[0-9]*)");
            Matcher feeMatcher = feePattern.matcher(sms);
            if (feeMatcher.find()) {
                String feeStr = feeMatcher.group(1).replace(",", "");
                result.amount = Double.parseDouble(feeStr);
            }

            // Extract total outstanding amount for balance
            Pattern pattern = Pattern.compile("outstanding amount is Ksh\\s?([0-9,]+\\.?[0-9]*)");
            Matcher matcher = pattern.matcher(sms);
            if (matcher.find()) {
                String amountStr = matcher.group(1).replace(",", "");
                result.fulizaOutstanding = Double.parseDouble(amountStr);
                result.newBalance = -result.fulizaOutstanding; // Negative balance!
            }

            result.isValid = true;
            return;
        }

        // Regular transaction types (First SMS)
        // Sent money
        if (smsLower.contains("sent to")) {
            result.type = "Expense";
            result.description = extractRecipient(sms, "sent to");
        }
        // Received money
        else if (smsLower.contains("received from") || smsLower.contains("you have received")) {
            result.type = "Income";
            result.description = extractRecipient(sms, "received from");
        }
        // Withdrew cash
        else if (smsLower.contains("withdraw") || smsLower.contains("withdrawn")) {
            result.type = "Expense";
            result.description = "Cash Withdrawal";
        }
        // Bought airtime
        else if (smsLower.contains("airtime")) {
            result.type = "Expense";
            result.description = "Airtime Purchase";
        }
        // Paid to merchant (Lipa na M-Pesa)
        else if (smsLower.contains("paid to")) {
            result.type = "Expense";
            result.description = extractMerchant(sms, "paid to");
        }
        // Bought goods
        else if (smsLower.contains("bought goods")) {
            result.type = "Expense";
            result.description = extractMerchant(sms, "bought goods");
        }
        // Give customer
        else if (smsLower.contains("give") && smsLower.contains("cash")) {
            result.type = "Expense";
            result.description = "Cash Given to Customer";
        }
        // PayBill
        else if (smsLower.contains("paybill")) {
            result.type = "Expense";
            result.description = extractPaybillDetails(sms);
        }
        // Default
        else {
            result.type = "Expense";
            result.description = "M-Pesa Transaction";
        }
    }

    private static String extractRecipient(String sms, String keyword) {
        try {
            int startIndex = sms.toLowerCase().indexOf(keyword) + keyword.length();
            String remaining = sms.substring(startIndex).trim();

            // Extract name (usually before phone number or "on")
            Pattern pattern = Pattern.compile("([A-Z][A-Z\\s]+?)(?:\\s+07|\\s+\\d{10}|\\s+on|\\.)");
            Matcher matcher = pattern.matcher(remaining);

            if (matcher.find()) {
                return matcher.group(1).trim();
            }

            // Fallback: take first few words
            String[] words = remaining.split("\\s+");
            if (words.length >= 2) {
                return words[0] + " " + words[1];
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Unknown";
    }

    private static String extractMerchant(String sms, String keyword) {
        try {
            int startIndex = sms.toLowerCase().indexOf(keyword) + keyword.length();
            String remaining = sms.substring(startIndex).trim();

            // Extract until "on" or period
            int endIndex = remaining.indexOf(" on ");
            if (endIndex == -1) {
                endIndex = remaining.indexOf(".");
            }

            if (endIndex > 0) {
                return remaining.substring(0, endIndex).trim();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Unknown Merchant";
    }

    private static String extractPaybillDetails(String sms) {
        try {
            // Try to extract business name
            Pattern pattern = Pattern.compile("for account ([A-Z0-9\\s]+)");
            Matcher matcher = pattern.matcher(sms);

            if (matcher.find()) {
                return "PayBill - " + matcher.group(1).trim();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "PayBill Payment";
    }

    private static String autoCategorize(String sms, String description, AppDatabase db) {
        String smsLower = sms.toLowerCase();
        String descLower = description.toLowerCase();

        // FIRST: Check if this is a Fuliza statement
        if (smsLower.contains("Fuliza M-Pesa amount is")) {
            return "Fuliza";
        }

        // Check database for learned merchant mappings
        try {
            MerchantCategory match = db.merchantCategoryDao().findByMerchantName(descLower);
            if (match != null) {
                return match.getCategory();
            }

            // Also check in SMS body
            match = db.merchantCategoryDao().findByMerchantName(smsLower);
            if (match != null) {
                return match.getCategory();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // DEFAULT CATEGORIZATION based on transaction type

//        // Person-to-person transfers (sent money to someone with phone number)
//        if (smsLower.contains("sent to")) {
//            // Check if it contains a Kenyan phone number
//            if (smsLower.contains(" 07") ||
//                    smsLower.contains(" 254") ||
//                    smsLower.matches(".*\\s0[17]\\d{8}.*")) {
//                return "Personal Transfer";
//            }
//        }

        // Cash withdrawals
        if (smsLower.contains("withdraw")) {
            return "Withdrawal";
        }

        // PayBill payments (usually bills)
//        if (smsLower.contains("paybill")) {
//            return "Bills & Utilities";
//        }

        // Buy Goods or Till Number (usually shopping)
//        if (smsLower.contains("bought goods") || smsLower.contains("till number")) {
//            return "Shopping";
//        }

        // Lipa na M-Pesa / Paid to merchant
//        if (smsLower.contains("paid to")) {
//            return "Shopping";
//        }

        // Airtime purchases
        if (smsLower.contains("airtime")) {
            return "Airtime & Data";
        }

        // Received money from someone
        if (smsLower.contains("received from") || smsLower.contains("you have received")) {
            return "Personal Transfer";
        }

        // Default fallback for everything else
        return null;
    }

    private static double extractBalance(String sms) {
        // Match "New M-PESA balance is Ksh5,000.00"
        Pattern pattern = Pattern.compile("balance is Ksh?\\s?([0-9,]+\\.?[0-9]*)");
        Matcher matcher = pattern.matcher(sms);

        if (matcher.find()) {
            String balanceStr = matcher.group(1).replace(",", "");
            return Double.parseDouble(balanceStr);
        }
        return 0.0;
    }
}