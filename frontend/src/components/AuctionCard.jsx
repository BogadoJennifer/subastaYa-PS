import { Badge, Card, Button } from 'react-bootstrap'
import Countdown from './Countdown.jsx'
import { Link } from 'react-router-dom';

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

    return (
        <Card className="h-100 shadow-sm overflow-hidden">
            {auction.imageUrl ? (
                <Card.Img
                    variant="top"
                    src={auction.imageUrl}
                    alt={auction.title}
                    loading="lazy"
                    style={{ height: '200px', objectFit: 'cover' }}
                />
            ) : (
                <div
                    className="bg-light text-secondary d-flex align-items-center justify-content-center"
                    style={{ height: '200px' }}
                >
                    Sin imagen disponible
                </div>
            )}


            <Card.Body>
                <div className="d-flex justify-content-between gap-2 mb-3">
          <span className="text-secondary small">
            {auction.categoryName ?? 'Sin categoría'}
          </span>

                    <Badge bg={statusInfo.color}>
                        {statusInfo.text}
                    </Badge>
                </div>

                <Card.Title as="h2" className="h5">
                    {auction.title}
                </Card.Title>

                <Card.Text className="text-secondary">
                    {auction.description || 'Sin descripción.'}
                </Card.Text>

                <div className="border-top pt-3">
                    <div className="small text-secondary">
                        {auction.highestBid != null
                            ? 'Oferta más alta'
                            : 'Precio base'}
                    </div>

                    <div className="fs-4 fw-bold">
                        {priceFormatter.format(
                            auction.highestBid ?? auction.basePrice
                        )}
                    </div>

                    <div className="small text-secondary mt-2">
                        {auction.bidCount === 0
                            ? 'Sin ofertas todavía'
                            : `${auction.bidCount} ${
                                auction.bidCount === 1
                                    ? 'oferta realizada'
                                    : 'ofertas realizadas'
                            }`}
                    </div>
                </div>

                <div className="border-top mt-3 pt-3">
                    <Countdown
                        state={auction.state}
                        startDate={auction.startDate}
                        endDate={auction.endDate}
                    />
                </div>

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