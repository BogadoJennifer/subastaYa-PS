import { useEffect, useState } from 'react'
import { useParams } from 'react-router-dom'

function LiveBiddingRoom() {

    const { auctionId } = useParams()

    const [auction, setAuction] = useState(null)
    const [isLoading, setIsLoading] = useState(true)
    const [error, setError] = useState('')

    useEffect(() => {
        async function loadAuction() {
            try {
                const response = await fetch(`/api/auctions/${auctionId}`)

                if (!response.ok) {
                    throw new Error(
                        `No se pudo cargar la subasta (${response.status})`
                    )
                }

                const data = await response.json()

                setAuction(data)
            } catch (error) {
                setError(error.message)
            } finally {
                setIsLoading(false)
            }
        }

        loadAuction()
    }, [auctionId])

    if (isLoading) {
        return (
            <div className="container py-4">
                <p>Cargando subasta...</p>
            </div>
        )
    }

    if (error) {
        return (
            <div className="container py-4">
                <p className="text-danger">{error}</p>
            </div>
        )
    }

    return (
        <div className="container py-4">

            <h1>Sala de Subasta en Vivo</h1>

            <h2>{auction.title}</h2>

            <p>
                {auction.description}
            </p>

            <p>
                ID de la subasta: {auction.id}
            </p>

        </div>
    )
}

export default LiveBiddingRoom