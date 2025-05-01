package com.pfe.nova.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONArray;
import org.json.JSONObject;

public class GeminiAPI {
    private static final String API_KEY = "AIzaSyCq1rpDN27lDAK1z0P-s5AfD6kfqLJwCBs";
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1/models/gemini-1.5-pro:generateContent?key=" + API_KEY;

    // Context about OncoKidsCare and NovaSpark
    private static final String ONCOKIDSCARE_CONTEXT =
            "OncoKidsCare is a pediatric oncology hospital website that provides tools for patients, doctors, and the hospital community to manage care, communication, and support services. " +
                    "The website was developed by the NovaSpark team, which consists of 6 developers: Rania Benali, Maha Mahsni, Amal Mansri, Aymen Ghozzi, Yazid Mrouki, and Kacem Ben Brahim. " +
                    "OncoKidsCare has the following features: " +
                    "1. Donation Management: Handles material and monetary donations. " +
                    "2. Pharmacy Management: Allows patients to order medications. " +
                    "3. Treatment Management: Patients can track reports, radiotherapy, and chemotherapy; doctors can monitor their patients. " +
                    "4. Appointment Management: Patients can book appointments. " +
                    "5. Community Space Management: A space for patients and doctors to exchange posts, comments, questions, encouragement, and testimonials.";

    public static String sendMessage(String userMessage) throws IOException {
        // Keywords to detect if the query is related to OncoKidsCare, NovaSpark, or the hospital
        String lowerCaseMessage = userMessage.toLowerCase();
        boolean isRelatedToOncoKidsCare = lowerCaseMessage.contains("oncokidscare") ||
                lowerCaseMessage.contains("novaspark") ||
                lowerCaseMessage.contains("hospital") ||
                lowerCaseMessage.contains("pediatric") ||
                lowerCaseMessage.contains("oncology") ||
                lowerCaseMessage.contains("website") ||
                lowerCaseMessage.contains("donation") ||
                lowerCaseMessage.contains("pharmacy") ||
                lowerCaseMessage.contains("treatment") ||
                lowerCaseMessage.contains("appointment") ||
                lowerCaseMessage.contains("community space");

        // If the query is related, prepend the context; otherwise, use the original message
        String messageToSend = userMessage;
        if (isRelatedToOncoKidsCare) {
            messageToSend = "Context: " + ONCOKIDSCARE_CONTEXT + "\nUser query: " + userMessage;
        }

        // Create JSON request body with the message
        String jsonRequest = String.format(
                "{\"contents\":[{\"parts\":[{\"text\":\"%s\"}]}]}",
                messageToSend.replace("\"", "\\\"").replace("\n", "\\n")
        );

        // Create connection
        URL url = new URL(API_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        // Send request
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonRequest.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        // Read response
        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(connection.getInputStream(), "utf-8"))) {
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
        } catch (IOException e) {
            // If there's an error, try to read the error stream
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(connection.getErrorStream(), "utf-8"))) {
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
            }
            return "Error: " + e.getMessage() + "\nResponse: " + response.toString();
        }

        // Parse the response using org.json
        try {
            JSONObject jsonResponse = new JSONObject(response.toString());
            JSONArray candidates = jsonResponse.getJSONArray("candidates");
            if (candidates.length() > 0) {
                JSONObject candidate = candidates.getJSONObject(0);
                JSONObject content = candidate.getJSONObject("content");
                JSONArray parts = content.getJSONArray("parts");
                if (parts.length() > 0) {
                    JSONObject part = parts.getJSONObject(0);
                    String extractedText = part.getString("text");
                    return extractedText;
                }
            }
            return "Could not extract text from response. Raw response: " + response.toString();
        } catch (Exception e) {
            return "Error parsing response: " + e.getMessage() + "\n\nRaw response: " + response.toString();
        } finally {
            connection.disconnect();
        }
    }
}