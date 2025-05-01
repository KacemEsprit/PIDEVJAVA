<?php
ini_set('display_errors', 1);
ini_set('display_startup_errors', 1);
error_reporting(E_ALL);

require __DIR__ . '/vendor/autoload.php';
// Clé secrète Stripe
\Stripe\Stripe::setApiKey('sk_test_51QwqzoRqecOHCMYBN05lg79lkoo3NBKwsf2WGuM12bhj9lGH1pjhyHM8lZhhEZX2NFzY5WbP1iMCfQSPwNuGq0Yf00u1o1DDnU');

header('Content-Type: application/json');

// Récupère le montant envoyé par POST (en centimes)
$montant = isset($_POST['montant']) ? intval($_POST['montant']) : 5000; // 5000 par défaut
file_put_contents(__DIR__ . '/debug_montant.txt', 'Montant reçu : ' . $montant . "\n", FILE_APPEND);

try {
    // Créer une session Stripe Checkout
    $session = \Stripe\Checkout\Session::create([
        'payment_method_types' => ['card'],
        'line_items' => [[
            'price_data' => [
                'currency' => 'eur',
                'product_data' => [
                    'name' => 'Donation Amal Project',
                ],
                'unit_amount' => $montant,
            ],
            'quantity' => 1,
        ]],
        'mode' => 'payment',
        'success_url' => 'http://localhost:8080/success.html',
        'cancel_url' => 'http://localhost:8080/cancel.html',
    ]);

    echo json_encode(['url' => $session->url]);
} catch (Exception $e) {
    http_response_code(500);
    echo json_encode(['error' => $e->getMessage()]);
}
?>