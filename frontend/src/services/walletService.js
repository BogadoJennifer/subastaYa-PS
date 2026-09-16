const API_URL = 'http://localhost:8080/api/v1/wallets';

// Módulo 4: Consultar el desglose de saldos
export async function getWalletBalance(userId) {
    const response = await fetch(`${API_URL}/balance?userId=${userId}`);
    if (!response.ok) {
        throw new Error('Error al obtener el saldo');
    }
    return await response.json();
}

// Módulo 4: Carga de saldo simulada
export async function deposit(userId, amount) {
    const response = await fetch(`${API_URL}/deposits`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ userId, amount })
    });

    if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.message || 'Error al acreditar el saldo');
    }
    return await response.json();
}

// Módulo 4: Historial de movimientos
export async function getWalletHistory(userId) {
    const response = await fetch(`${API_URL}/history?userId=${userId}`);
    if (!response.ok) {
        throw new Error('Error al obtener el historial');
    }
    return await response.json();
}