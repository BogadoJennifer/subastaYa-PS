const priceFormatter = new Intl.NumberFormat('es-AR', {
    style: 'currency',
    currency: 'ARS',
})

const dateFormatter = new Intl.DateTimeFormat('es-AR', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hour12: false,
})

function formatBidDate(value) {
    const date = new Date(value)

    return Number.isNaN(date.getTime())
        ? 'Fecha no disponible'
        : dateFormatter.format(date)
}

function BidHistory({ bids, currentUserId }) {
    return (
        <section className="mt-4" aria-labelledby="bid-history-title">
            <h3 id="bid-history-title" className="h5">
                Historial de ofertas
            </h3>

            <p className="small text-secondary">
                Últimas 50 ofertas, de la más reciente a la más antigua.
            </p>

            {bids.length === 0 ? (
                <div className="alert alert-light border">
                    Todavía no hay ofertas.
                </div>
            ) : (
                <ul
                    className="list-group"
                    style={{ maxHeight: '360px', overflowY: 'auto' }}
                >
                    {bids.map((bid) => {
                        const isCurrentUser =
                            String(bid.bidderId) === String(currentUserId)

                        return (
                            <li key={bid.id} className="list-group-item">
                                <div className="d-flex justify-content-between gap-3">
                                    <span>
                                        {bid.bidderAlias}

                                        {isCurrentUser && (
                                            <span className="badge bg-primary ms-2">
                                                Vos
                                            </span>
                                        )}
                                    </span>

                                    <strong className="text-nowrap">
                                        {priceFormatter.format(bid.amount)}
                                    </strong>
                                </div>

                                <time
                                    dateTime={bid.bidDate}
                                    className="small text-secondary"
                                >
                                    {formatBidDate(bid.bidDate)}
                                </time>
                            </li>
                        )
                    })}
                </ul>
            )}
        </section>
    )
}

export default BidHistory