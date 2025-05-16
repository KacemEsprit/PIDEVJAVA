# OncoKidsCare - Application JavaFX de gestion hospitalière pédiatrique

## 🩺 Présentation

**OncoKidsCare** est une application développée en **JavaFX** dans le cadre d’un projet intégré à **Esprit School of Engineering**.  
Elle permet la gestion centralisée et interactive d’un hôpital spécialisé en oncologie pédiatrique, avec des interfaces dédiées aux **patients**, **médecins**, **donateurs**, **administrateurs**, et **pharmaciens**.

L’objectif principal est d’offrir un environnement **intuitif, interactif et sécurisé** pour :
- gérer les traitements,
- prendre des rendez-vous,
- commander des médicaments,
- favoriser l’expression et le soutien communautaire,
- tracer les dons,
- administrer la plateforme.

---

## 📋 Fonctionnalités principales

### 👶 Côté Patient
- Prise de **rendez-vous** selon la disponibilité des médecins.
- **Messagerie instantanée** avec les médecins.
- Espace communautaire :
  - **Partage de publications**, commentaires, likes.
  - **Envoi de messages vocaux**.
- Pharmacie :
  - **Recherche et ajout de médicaments au panier**.
  - **Passation de commandes** avec génération de **facture PDF**.
  - **QR code cadeau** après chaque commande.
  - **Évaluation** des commandes.

### 🧑‍⚕️ Côté Médecin
- Création de :
  - **Rapports médicaux**
  - **Sessions de traitement**
- Évaluation de l’**état de santé des patients**

### 💰 Côté Donateur
- Réalisation de **dons via Stripe**.
- Réception de **reçus sous forme de QR code**.

### 🧑‍💼 Côté Admin (Administrateur)
- Authentification possible via :
  - **Google Sign-up / OAuth**
  - **Connexion par email/mot de passe**
  - **Mot de passe oublié** avec lien de réinitialisation envoyé par **email**
  - **Vérification de sécurité avec Google reCAPTCHA**
- Gestion des modules :
  - **Validation ou rejet des dons**
  - **Approbation des commentaires communautaires**
  - **Suivi des avis et évaluations**
  - **Confirmation des commandes**
  - Visualisation des **statistiques globales**
- Accès à un **dashboard complet**

---

## 🧱 Architecture Technique

### Frontend
- **JavaFX** (UI moderne et responsive)
- **CSS personnalisé** (ergonomie adaptée aux enfants)

### Backend
- **Java 17**
- **JDBC + MySQL**
- **Maven**

### Intégrations
- **Google OAuth 2.0** – Connexion pour les admins
- **Email API** – Pour les notifications et récupération de mot de passe
- **Google reCAPTCHA** – Sécurisation des formulaires
- **Stripe API** – Paiement sécurisé des dons
- **QR Code** – Reçus de dons et cadeaux post-commande
- **Twilio / WhatsApp API** – Notifications vers l’administrateur
- **Reconnaissance vocale** – Chat vocal patient
- **Google Calendar API** – Gestion des créneaux médicaux
- **Météo API** – Affichage météo dans le dashboard

---

## 🗂️ Structure du Projet

```plaintext
OncoKidsCare/
│
├── .idea/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   ├── controller/
│   │   │   ├── model/
│   │   │   └── utils/
│   │   └── resources/
│   │       ├── assets/
│   │       ├── views/
│   │       ├── styles/
│   │       └── utils/
├── pom.xml
└── README.md
