import { Badge, Card } from 'react-bootstrap'
import Countdown from './Countdown.jsx'
import { Link } from 'react-router-dom'

const auctionStates = {
    SCHEDULED: { text: 'Próxima', color: 'primary' },
    ACTIVE: { text: 'Activa', color: 'success' },
    FINISHED: { text: 'Finalizada', color: 'secondary' },
    UNSOLD: { text: 'Desierta', color: 'secondary' },
}

const priceFormatter = new Intl.NumberFormat('es-AR', {
    style: 'currency',
    currency: 'ARS',
})

function AuctionCard({ auction }) {
    const statusInfo = auctionStates[auction.state] ?? {
        text: auction.state,
        color: 'secondary',
    }

    const hasBids = auction.highestBid != null

    return (
        <Card className="auction-card h-100">
            <div className="auction-media">
                {auction.imageUrl ? (
                    <img
                        className="auction-image"
                        src={auction.imageUrl}
                        alt={auction.title}
                        loading="lazy"
                    />
                ) : (
                    <div className="auction-image-placeholder">
                        Sin imagen disponible
                    </div>
                )}

                <Badge bg={statusInfo.color} className="auction-status">
                    {statusInfo.text}
                </Badge>

                <div className="auction-countdown">
                    <Countdown
                        state={auction.state}
                        startDate={auction.startDate}
                        endDate={auction.endDate}
                    />
                </div>
            </div>

            <Card.Body className="auction-body">
                <p className="auction-category">
                    {auction.categoryName ?? 'Sin categoría'}
                </p>

                <Card.Title as="h3" className="auction-title">
                    {auction.title}
                </Card.Title>

                <Card.Text className="auction-description">
                    {auction.description || 'Sin descripción.'}
                </Card.Text>

                <div className="auction-price-section">
                    <span className="auction-price-label">
                        {hasBids ? 'Oferta más alta' : 'Precio base'}
                    </span>

                    <strong className="auction-price">
                        {priceFormatter.format(
                            auction.highestBid ?? auction.basePrice
                        )}
                    </strong>

                    <span className="auction-bid-count">
                        {auction.bidCount === 0
                            ? 'Sin ofertas todavía'
                            : `${auction.bidCount} ${
                                auction.bidCount === 1
                                    ? 'oferta realizada'
                                    : 'ofertas realizadas'
                            }`}
                    </span>
                </div>

                {/* SALA DE SUBASTA EN VIVO */}
                <div className="mt-3">
                    <Link
                        to={`/auctions/${auction.id}/live`}
                        className="btn btn-primary w-100"
                    >
                        Acceder a la sala en vivo
                    </Link>
                </div>
            </Card.Body>
        </Card>
    )
}

export default AuctionCard