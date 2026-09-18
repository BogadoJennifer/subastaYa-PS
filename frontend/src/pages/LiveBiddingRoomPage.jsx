import { useEffect, useState } from 'react'
import { useParams } from 'react-router-dom'
import { Client } from '@stomp/stompjs'
import Countdown from '../components/Countdown.jsx'

function LiveBiddingRoomPage({ auctionId: propId, currentUserId = 2 }) {
    const { auctionId: paramId } = useParams()
    const id = propId || paramId

    const [auction, setAuction] = useState(null)
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState('')
    const [message, setMessage] = useState('')
    const [isSubmitting, setIsSubmitting] = useState(false)
    const [isConnected, setIsConnected] = useState(false)
    const [currentTime, setCurrentTime] = useState(() => Date.now())

    useEffect(() => {
        const intervalId = setInterval(() => {
            setCurrentTime(Date.now())
        }, 1000)

        return () => clearInterval(intervalId)
    }, [])
    // 1. Cargar la subasta desde el backend
    useEffect(() => {
        if (!id) return

        const controller = new AbortController()
        let active = true
        let refreshing = false
        let refreshPending = false

        // Serialize refreshes so older requests cannot finish after newer ones.
        async function refreshAuction() {
            if (!active) return

            refreshPending = true

            if (refreshing) return

            refreshing = true

            try {
                while (active && refreshPending) {
                    refreshPending = false

                    try {
                        const response = await fetch(
                            `/api/auctions/${id}/details`,
                            { signal: controller.signal }
                        )

                        if (!response.ok) {
                            throw new Error(
                                `No se pudo actualizar la subasta (${response.status})`
                            )
                        }

                        const data = await response.json()

                        if (!active) return

                        setAuction((previousAuction) => {
                            // Preserve a newer bid received through the POST response.
                            if (
                                previousAuction?.id === data.id &&
                                Number(previousAuction.highestBid ?? 0) >
                                Number(data.highestBid ?? 0)
                            ) {
                                return {
                                    ...data,
                                    highestBid: previousAuction.highestBid,
                                    endDate: previousAuction.endDate,
                                }
                            }

                            return data
                        })

                        setError('')
                    } catch (requestError) {
                        if (active && !controller.signal.aborted) {
                            setError(requestError.message)
                        }
                    } finally {
                        if (active) {
                            setLoading(false)
                        }
                    }
                }
            } finally {
                refreshing = false
            }
        }

        const socketUrl = new URL('/ws-live', window.location.href)
        socketUrl.protocol =
            window.location.protocol === 'https:' ? 'wss:' : 'ws:'

        const client = new Client({
            brokerURL: socketUrl.toString(),
            reconnectDelay: 5000,

            onConnect: () => {
                if (!active) return

                client.subscribe(`/topic/auctions/${id}`, () => {
                    void refreshAuction()
                })

                setIsConnected(true)

                // Recover updates missed before connecting or while disconnected.
                void refreshAuction()
            },

            onWebSocketClose: () => {
                if (active) setIsConnected(false)
            },

            onWebSocketError: () => {
                if (active) setIsConnected(false)
            },

            onStompError: () => {
                if (active) setIsConnected(false)
            },
        })

        void refreshAuction()
        client.activate()

        return () => {
            active = false
            controller.abort()
            void client.deactivate()
        }
    }, [id])

    if (loading) return <div className="container py-4">Cargando subasta #{id}...</div>
    if (error || !auction) return <div className="container py-4 text-danger">Error: {error || 'No encontrada'}</div>

    // 2. Valores calculados con datos directos de la BD
    const currentPrice = Number(auction.highestBid ?? auction.basePrice ?? 0)
    const minIncrement = Number(auction.minimumIncrement ?? 0)
    const nextBid = Number((currentPrice + minIncrement).toFixed(2))

    const startTimestamp = new Date(auction.startDate).getTime()
    const endTimestamp = new Date(auction.endDate).getTime()

    const canBid =
        auction.state === 'ACTIVE' &&
        Number.isFinite(startTimestamp) &&
        Number.isFinite(endTimestamp) &&
        currentTime >= startTimestamp &&
        currentTime < endTimestamp

    // 3. Enviar la oferta mínima
    const handleBid = async () => {
        if (isSubmitting || !canBid) return

        setIsSubmitting(true)
        setMessage('Enviando oferta...')

        try {
            const response = await fetch(`/api/auctions/${id}/bids`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    bidderId: currentUserId,
                    amount: nextBid,
                }),
            })

            const result = await response.json().catch(() => null)

            if (!response.ok) {
                setMessage(
                    result?.message ??
                    `No se pudo registrar la oferta (${response.status})`
                )
                return
            }

            if (result?.amount == null || !result?.endDate) {
                setMessage(
                    'El servidor respondió sin los datos esperados. Recargá la página para verificar si la oferta se registró.'
                )
                return
            }

            setAuction((previousAuction) => {
                if (!previousAuction) return previousAuction

                if (
                    Number(previousAuction.highestBid ?? 0) >
                    Number(result.amount)
                ) {
                    return previousAuction
                }

                return {
                    ...previousAuction,
                    highestBid: result.amount,
                    endDate: result.endDate,
                }
            })

            const formattedAmount = new Intl.NumberFormat('es-AR', {
                style: 'currency',
                currency: 'ARS',
            }).format(result.amount)

            setMessage(
                `¡Oferta de ${formattedAmount} registrada!${
                    result.wasExtended
                        ? ' La subasta se extendió 2 minutos.'
                        : ''
                }`
            )
        } catch {
            setMessage(
                'No se pudo confirmar la respuesta del servidor. Recargá la página para verificar si la oferta se registró.'
            )
        } finally {
            setIsSubmitting(false)
        }
    }

    return (
        <div className="container py-4" style={{ maxWidth: '500px' }}>
            <h2>{auction.title}</h2>

            <p
                className={`small ${
                    isConnected ? 'text-success' : 'text-warning'
                }`}
                role="status"
            >
                {isConnected
                    ? '● LIVE!'
                    : '● Sin conexión en vivo. Intentando conectar…'}
            </p>

            <div className="mb-3" role="status">
                <Countdown
                    state={auction.state}
                    startDate={auction.startDate}
                    endDate={auction.endDate}
                />
            </div>


            <div className="mb-3">
                <div>Precio actual: <strong>${currentPrice}</strong></div>
                <div>Incremento mínimo (BD): <strong>+${minIncrement}</strong></div>
            </div>

            <button
                type="button"
                onClick={handleBid}
                disabled={isSubmitting || !canBid}
                className="btn btn-success btn-lg w-100"
            >
                {isSubmitting
                    ? 'Enviando oferta...'
                    : canBid
                        ? `Ofertar mínimo: $${nextBid}`
                        : 'Ofertas no disponibles'}
            </button>

            {message && <div className="alert alert-info mt-3">{message}</div>}
        </div>
    )
}

export default LiveBiddingRoomPage