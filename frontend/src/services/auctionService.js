const API_URL = 'http://localhost:8080/api/auctions';

// Módulo 3: Detalle de una subasta (para la Sala de Subasta en Vivo)
export async function getAuctionById(id) {
    const response = await fetch(`${API_URL}/${id}`);
    if (!response.ok) {
        throw new Error('Error al obtener la subasta');
    }
    return await response.json();
}

// Módulo 3: Registrar una puja
export async function placeBid(auctionId, bidderId, amount) {
    const response = await fetch(`${API_URL}/${auctionId}/bids`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ bidderId, amount })
    });

    if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.message || 'Error al registrar la puja');
    }
    return await response.json();
}