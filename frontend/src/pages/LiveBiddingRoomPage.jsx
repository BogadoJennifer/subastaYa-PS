import { useEffect, useState } from 'react'
import { useParams, useSearchParams } from 'react-router-dom'
import { Client } from '@stomp/stompjs'
import Countdown from '../components/Countdown.jsx'
import BidHistory from "../components/BidHistory.jsx";
import { BidConsole } from '../components/BidConsole.jsx'
import BidStatus from "../components/BidStatus.jsx";
import { Toast, ToastContainer } from 'react-bootstrap'

function LiveBiddingRoomPage({ auctionId: propId }) {
    const { auctionId: paramId } = useParams()
    const [searchParams] = useSearchParams()

    const id = propId || paramId

    const demoUserId = searchParams.get('demoUserId')

    const currentUserId =
        import.meta.env.DEV && demoUserId === '3' ? 3 : 2

    const [auction, setAuction] = useState(null)
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState('')
    const [message, setMessage] = useState('')
    const [isSubmitting, setIsSubmitting] = useState(false)
    const [isConnected, setIsConnected] = useState(false)
    const [currentTime, setCurrentTime] = useState(() => Date.now())
    const [bids, setBids] = useState([])
    const [feedbackVariant, setFeedbackVariant] = useState('info')
    const [extensionMessage, setExtensionMessage] = useState('')

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
        let latestEndTimestamp = null

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
                        const [auctionResponse, bidsResponse] = await Promise.all([
                            fetch(
                                `/api/auctions/${id}/details?userId=${encodeURIComponent(currentUserId)}`,
                                {
                                    signal: controller.signal,
                                }
                            ),
                            fetch(`/api/auctions/${id}/bids`, {
                                signal: controller.signal,
                            }),
                        ])

                        if (!auctionResponse.ok) {
                            throw new Error(
                                `No se pudo actualizar la subasta (${auctionResponse.status})`
                            )
                        }

                        if (!bidsResponse.ok) {
                            throw new Error(
                                `No se pudo cargar el historial (${bidsResponse.status})`
                            )
                        }

                        const [data, bidHistory] = await Promise.all([
                            auctionResponse.json(),
                            bidsResponse.json(),
                        ])

                        if (!Array.isArray(bidHistory)) {
                            throw new Error('El historial recibido tiene un formato inesperado')
                        }

                        if (!active) return

                        const updatedEndTimestamp = new Date(data.endDate).getTime()

                        if (Number.isFinite(updatedEndTimestamp)) {
                            if (
                                latestEndTimestamp !== null &&
                                updatedEndTimestamp > latestEndTimestamp
                            ) {
                                const formattedEndTime = new Intl.DateTimeFormat('es-AR', {
                                    hour: '2-digit',
                                    minute: '2-digit',
                                    second: '2-digit',
                                    hour12: false,
                                }).format(new Date(updatedEndTimestamp))

                                setExtensionMessage(
                                    `Se extendió el tiempo de la subasta. Nuevo cierre: ${formattedEndTime}.`
                                )
                            }

                            latestEndTimestamp = Math.max(
                                latestEndTimestamp ?? updatedEndTimestamp,
                                updatedEndTimestamp
                            )
                        }

                        setBids(bidHistory)

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
                                    highestBidderId: previousAuction.highestBidderId,
                                    currentUserHasBid: previousAuction.currentUserHasBid,
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
    }, [id, currentUserId])

    if (loading) return <div className="container py-4">Cargando subasta #{id}...</div>
    if (error || !auction) return <div className="container py-4 text-danger">Error: {error || 'No encontrada'}</div>

    // 2. Valores calculados con datos directos de la BD
    const currentPrice = Number(auction.highestBid ?? auction.basePrice ?? 0)
    const minIncrement = Number(auction.minimumIncrement ?? 0)
    //const nextBid = Number((currentPrice + minIncrement).toFixed(2))

    const startTimestamp = new Date(auction.startDate).getTime()
    const endTimestamp = new Date(auction.endDate).getTime()

    const canBid =
        auction.state === 'ACTIVE' &&
        Number.isFinite(startTimestamp) &&
        Number.isFinite(endTimestamp) &&
        currentTime >= startTimestamp &&
        currentTime < endTimestamp

    // 3. Enviar la oferta mínima
    const handleBid = async (amount) => {
        if (isSubmitting || !canBid) return false

        const minimumAmount = Number(
            (currentPrice + minIncrement).toFixed(2)
        )

        if (!Number.isFinite(amount) || amount < minimumAmount) {
            setFeedbackVariant('danger')
            setMessage(
                `La oferta debe ser de al menos $${minimumAmount}.`
            )
            return false
        }

        setIsSubmitting(true)
        setMessage('')

        try {
            const response = await fetch(`/api/auctions/${id}/bids`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    bidderId: currentUserId,
                    amount,
                }),
            })

            const result = await response.json().catch(() => null)

            if (!response.ok) {
                setFeedbackVariant('danger')
                setMessage(
                    result?.message ??
                    `No se pudo registrar la oferta (${response.status})`
                )
                return false
            }

            if (result?.amount == null || !result?.endDate) {
                setFeedbackVariant('warning')
                setMessage(
                    'El servidor respondió sin los datos esperados. Recargá la página para verificar si la oferta se registró.'
                )
                return false
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
                    highestBidderId: currentUserId,
                    currentUserHasBid: true,
                }
            })

            const formattedAmount = new Intl.NumberFormat('es-AR', {
                style: 'currency',
                currency: 'ARS',
            }).format(result.amount)

            setFeedbackVariant('success')
            setMessage(`¡Oferta de ${formattedAmount} registrada!`)

            return true
        } catch {
            setFeedbackVariant('warning')
            setMessage(
                'No se pudo confirmar la respuesta del servidor. Recargá la página para verificar si la oferta se registró.'
            )
            return false
        } finally {
            setIsSubmitting(false)
        }
    }

    return (
        <div className="container py-4" style={{ maxWidth: '500px' }}>
            <h2>{auction.title}</h2>

            {import.meta.env.DEV && (
                <div className="alert alert-secondary py-2">
                    Usuario de prueba: <strong>Postor {currentUserId}</strong>
                </div>
            )}

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

            <BidStatus
                auction={auction}
                currentUserId={currentUserId}
            />

            <BidConsole
                currentPrice={currentPrice}
                minIncrement={minIncrement}
                onBid={handleBid}
                disabled={!canBid}
                isSubmitting={isSubmitting}
            />

            <ToastContainer
                position="bottom-end"
                className="p-3"
                style={{
                    position: 'fixed',
                    zIndex: 1080,
                    maxWidth: '100vw',
                }}
            >
                <Toast
                    show={Boolean(message)}
                    onClose={() => setMessage('')}
                    bg={feedbackVariant}
                    autohide={feedbackVariant === 'success'}
                    delay={6000}
                    role={feedbackVariant === 'danger' ? 'alert' : 'status'}
                    aria-live={feedbackVariant === 'danger' ? 'assertive' : 'polite'}
                    aria-atomic="true"
                >
                    <Toast.Header>
                        <strong className="me-auto">
                            {feedbackVariant === 'success'
                                ? 'Oferta registrada'
                                : feedbackVariant === 'danger'
                                    ? 'Oferta rechazada'
                                    : 'Verificá el resultado'}
                        </strong>
                    </Toast.Header>

                    <Toast.Body
                        className={
                            feedbackVariant === 'success' ||
                            feedbackVariant === 'danger'
                                ? 'text-white'
                                : 'text-dark'
                        }
                    >
                        {message}
                    </Toast.Body>
                </Toast>

                <Toast
                    show={Boolean(extensionMessage)}
                    onClose={() => setExtensionMessage('')}
                    bg="warning"
                    role="status"
                    aria-live="polite"
                    aria-atomic="true"
                >
                    <Toast.Header>
                        <strong className="me-auto">Tiempo extendido</strong>
                    </Toast.Header>

                    <Toast.Body className="text-dark">
                        {extensionMessage}
                    </Toast.Body>
                </Toast>
            </ToastContainer>

            <BidHistory
                bids={bids}
                currentUserId={currentUserId}
            />
        </div>
    )
}

export default LiveBiddingRoomPage