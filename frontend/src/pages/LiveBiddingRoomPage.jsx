import { useEffect, useState } from 'react'
import { useParams } from 'react-router-dom'
import { Alert, Badge, Button, Card, Form, Spinner } from 'react-bootstrap'

function LiveBiddingRoomPage() {

    const { auctionId } = useParams()

    const [auction, setAuction] = useState(null)
    const [isLoading, setIsLoading] = useState(true)
    const [error, setError] = useState('')

    const [customAmount, setCustomAmount] = useState('')
    const [isSubmitting, setIsSubmitting] = useState(false)
    const [feedback, setFeedback] = useState(null)

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

    const currentPrice = Number(auction?.highestBid ?? auction?.basePrice ?? 0)
    const minIncrement = find
    const nextMinBid = currentPrice + minIncrement

    // Indicador de liderazgo
    let currentUserId;
    const isLeading = auction?.highestBidderId === currentUserId

    // Disparador de la puja vía POST
    const handleBid = async (amountToSend) => {
        setFeedback(null)
        setIsSubmitting(true)

        try {
            const response = await fetch(`/api/auctions/${auctionId}/bids`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    buyerId: currentUserId,
                    amount: amountToSend,
                }),
            })

            const data = await response.json().catch(() => ({}))

            if (response.status === 201) {
                setFeedback({
                    variant: 'success',
                    message: `¡Oferta aceptada por $${amountToSend.toLocaleString()}! Ahora estás liderando.`,
                })
                // Actualizamos el estado local para reflejar la puja
                setAuction((prev) => ({
                    ...prev,
                    highestBid: amountToSend,
                    highestBidderId: currentUserId,
                }))
                setCustomAmount('')
            } else if (response.status === 400) {
                setFeedback({
                    variant: 'danger',
                    message: data.message || `La oferta debe ser de al menos $${nextMinBid}.`,
                })
            } else if (response.status === 409) {
                setFeedback({
                    variant: 'warning',
                    message: 'Conflicto: otro usuario ofertó una cifra mayor justo antes. Actualizando datos...',
                })
            } else if (response.status === 422) {
                setFeedback({
                    variant: 'danger',
                    message: 'Saldo insuficiente en tu billetera para retener la garantía (escrow).',
                })
            } else {
                setFeedback({
                    variant: 'danger',
                    message: data.message || `Error inesperado (${response.status}).`,
                })
            }
        } catch (err) {
            setFeedback({
                variant: 'danger',
                message: 'No se pudo conectar con el servidor.',
            })
        } finally {
            setIsSubmitting(false)
        }
    }

    const handleCustomSubmit = (e) => {
        e.preventDefault()
        const parsedAmount = parseFloat(customAmount)

        if (!parsedAmount || parsedAmount < nextMinBid) {
            setFeedback({
                variant: 'warning',
                message: `El monto ingresado debe ser de al menos $${nextMinBid.toLocaleString()}.`,
            })
            return
        }

        handleBid(parsedAmount)
    }

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
        <div className="container py-4" style={{ maxWidth: '720px' }}>
            <h1 className="mb-3">Sala de Subasta en Vivo</h1>

            <Card className="mb-4">
                <Card.Body>
                    <div className="d-flex justify-content-between align-items-start mb-2">
                        <div>
                            <Card.Title className="h3 mb-1">{auction.title}</Card.Title>
                            <Card.Subtitle className="text-muted">
                                ID: #{auction.id}
                            </Card.Subtitle>
                        </div>

                        {/* Indicador de liderazgo */}
                        <div>
                            {auction.highestBidderId ? (
                                isLeading ? (
                                    <Badge bg="success" className="p-2">
                                        🟢 Estás liderando
                                    </Badge>
                                ) : (
                                    <Badge bg="danger" className="p-2">
                                        🔴 Fuiste superado (Outbid)
                                    </Badge>
                                )
                            ) : (
                                <Badge bg="secondary" className="p-2">
                                    Sin ofertas aún
                                </Badge>
                            )}
                        </div>
                    </div>

                    <Card.Text className="text-secondary">
                        {auction.description}
                    </Card.Text>

                    <hr />

                    <div className="my-3">
                        <div className="text-muted small">Puja más alta actual:</div>
                        <div className="display-6 fw-bold text-primary">
                            ${currentPrice.toLocaleString()}
                        </div>
                    </div>

                    {/* Feedback contextual de la puja */}
                    {feedback && (
                        <Alert
                            variant={feedback.variant}
                            dismissible
                            onClose={() => setFeedback(null)}
                            className="mt-3"
                        >
                            {feedback.message}
                        </Alert>
                    )}

                    {/* Consola de Oferta */}
                    <div className="border rounded p-3 bg-light mt-3">
                        <h5 className="mb-3">Consola de Oferta</h5>

                        <Button
                            variant="primary"
                            className="w-100 py-2 mb-3 fw-bold"
                            disabled={isSubmitting}
                            onClick={() => handleBid(nextMinBid)}
                        >
                            {isSubmitting ? (
                                <Spinner size="sm" className="me-2" />
                            ) : (
                                `Pujar Mínimo Sugerido ($${nextMinBid.toLocaleString()})`
                            )}
                        </Button>

                        <Form onSubmit={handleCustomSubmit} className="d-flex gap-2">
                            <Form.Control
                                type="number"
                                step="0.01"
                                placeholder={`Monto personalizado >= $${nextMinBid}`}
                                value={customAmount}
                                disabled={isSubmitting}
                                onChange={(e) => setCustomAmount(e.target.value)}
                            />
                            <Button
                                type="submit"
                                variant="dark"
                                disabled={isSubmitting || !customAmount}
                            >
                                Ofertar
                            </Button>
                        </Form>
                    </div>
                </Card.Body>
            </Card>
        </div>
    )
}

export default LiveBiddingRoomPage