import { useEffect, useState } from 'react'
import { useParams } from 'react-router-dom'

function LiveBiddingRoomPage({ auctionId: propId, currentUserId = 2 }) {
    const { auctionId: paramId } = useParams()
    const id = propId || paramId

    const [auction, setAuction] = useState(null)
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState('')
    const [message, setMessage] = useState('')

    // 1. Cargar la subasta desde el backend
    useEffect(() => {
        if (!id) return

        fetch(`/api/auctions/${id}`)
            .then((res) => {
                if (!res.ok) throw new Error(`Error ${res.status}`)
                return res.json()
            })
            .then((data) => setAuction(data))
            .catch((err) => setError(err.message))
            .finally(() => setLoading(false))
    }, [id])

    if (loading) return <div className="container py-4">Cargando subasta #{id}...</div>
    if (error || !auction) return <div className="container py-4 text-danger">Error: {error || 'No encontrada'}</div>

    // 2. Valores calculados con datos directos de la BD
    const currentPrice = Number(auction.highestBid ?? auction.basePrice ?? 0)
    const minIncrement = Number(auction.minIncrement ?? auction.minimumIncrement ?? 0)
    const nextBid = currentPrice + minIncrement

    // 3. Enviar la oferta mínima
    const handleBid = async () => {
        setMessage('Enviando oferta...')
        try {
            const res = await fetch(`/api/auctions/${id}/bids`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    bidderId: currentUserId,
                    amount: nextBid,
                }),
            })

            if (res.ok) {
                setMessage(`¡Oferta de $${nextBid} enviada con éxito!`)
                setAuction({ ...auction, highestBid: nextBid, highestBidderId: currentUserId })
            } else {
                setMessage(`Rechazada por el servidor (Código ${res.status})`)
            }
        } catch (e) {
            setMessage('Error de conexión al ofertar')
        }
    }

    return (
        <div className="container py-4" style={{ maxWidth: '500px' }}>
            <h2>{auction.title}</h2>
            <p className="text-muted">{auction.description}</p>
            <hr />

            <div className="mb-3">
                <div>Precio actual: <strong>${currentPrice}</strong></div>
                <div>Incremento mínimo (BD): <strong>+${minIncrement}</strong></div>
            </div>

            <button
                onClick={handleBid}
                className="btn btn-success btn-lg w-100"
            >
                Ofertar Mínimo: ${nextBid}
            </button>

            {message && <div className="alert alert-info mt-3">{message}</div>}
        </div>
    )
}

export default LiveBiddingRoomPage