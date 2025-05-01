package com.pfe.nova.services;

import com.pfe.nova.models.ChatMessage;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import okhttp3.*;
import okio.BufferedSource;
import org.json.JSONObject;

import java.io.IOException;
import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class MercureService {
    private static final String HUB_URL = "http://localhost:3000/.well-known/mercure";
    private static final String JWT_SECRET = "!ChangeThisMercureHubJWTSecretKey!";
    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();
    
    private static Key getSigningKey() {
        return Keys.hmacShaKeyFor(JWT_SECRET.getBytes());
    }
    
    // Générer un JWT pour la publication
    public static String generatePublisherJWT() {
        return Jwts.builder()
                .setSubject("http://example.com/user/")
                .claim("mercure", new JSONObject()
                        .put("publish", new ArrayList<>(List.of("*")))
                        .toMap())
                .setIssuedAt(Date.from(Instant.now()))
                .setExpiration(Date.from(Instant.now().plus(1, ChronoUnit.HOURS)))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }
    
    // Générer un JWT pour l'abonnement
    public static String generateSubscriberJWT() {
        return Jwts.builder()
                .setSubject("http://example.com/user/")
                .claim("mercure", new JSONObject()
                        .put("subscribe", new ArrayList<>(List.of("*")))
                        .toMap())
                .setIssuedAt(Date.from(Instant.now()))
                .setExpiration(Date.from(Instant.now().plus(1, ChronoUnit.HOURS)))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }
    
    // Publier un message sur le hub Mercure
    public static void publishMessage(ChatMessage message) {
        try {
            String topic = "chat/" + message.getChannelName();
            
            JSONObject data = new JSONObject();
            data.put("id", message.getId());
            data.put("userId", message.getUserId());
            data.put("username", message.getUsername());
            data.put("channelName", message.getChannelName());
            data.put("content", message.getContent());
            data.put("timestamp", message.getTimestamp().toString());
            
            RequestBody formBody = new FormBody.Builder()
                    .add("topic", topic)
                    .add("data", data.toString())
                    .build();
            
            Request request = new Request.Builder()
                    .url(HUB_URL)
                    .addHeader("Authorization", "Bearer " + generatePublisherJWT())
                    .post(formBody)
                    .build();
            
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    System.err.println("Erreur lors de la publication du message: " + response.code());
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la publication du message: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // S'abonner aux mises à jour d'un canal
    public static void subscribeToChannel(String channelName, Consumer<ChatMessage> onMessageReceived) {
        String topic = "chat/" + channelName;
        String url = HUB_URL + "?topic=" + topic;
        
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + generateSubscriberJWT())
                .build();
        
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                System.err.println("Erreur de connexion au hub Mercure: " + e.getMessage());
                e.printStackTrace();
                
                // Réessayer après un délai
                try {
                    Thread.sleep(5000);
                    subscribeToChannel(channelName, onMessageReceived);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
            
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try (ResponseBody responseBody = response.body()) {
                    if (!response.isSuccessful()) {
                        System.err.println("Erreur d'abonnement: " + response.code());
                        return;
                    }
                    
                    if (responseBody == null) {
                        System.err.println("Corps de réponse vide");
                        return;
                    }
                    
                    // Lire le flux SSE (Server-Sent Events)
                    BufferedSource source = responseBody.source();
                    while (!source.exhausted()) {
                        String line = source.readUtf8Line();
                        if (line == null) break;
                        
                        if (line.startsWith("data:")) {
                            String data = line.substring(5).trim();
                            try {
                                JSONObject json = new JSONObject(data);
                                ChatMessage message = ChatMessage.fromJson(json);
                                
                                // Appeler le callback sur le thread JavaFX
                                javafx.application.Platform.runLater(() -> {
                                    onMessageReceived.accept(message);
                                });
                            } catch (Exception e) {
                                System.err.println("Erreur de parsing du message: " + e.getMessage());
                            }
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Erreur lors de la lecture des événements: " + e.getMessage());
                    e.printStackTrace();
                } finally {
                    // Réessayer de se connecter si la connexion est perdue
                    try {
                        Thread.sleep(1000);
                        subscribeToChannel(channelName, onMessageReceived);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        });
    }

    public void unsubscribe() {
    }
}