function BidStatus({ auction, currentUserId }) {
    const isLeading =
        auction.highestBidderId != null &&
        String(auction.highestBidderId) === String(currentUserId)

    if (auction.state === 'FINISHED') {
        return (
            <div
                className={`alert ${
                    isLeading ? 'alert-success' : 'alert-secondary'
                }`}
                role="status"
            >
                {isLeading
                    ? '¡Ganaste la subasta!'
                    : 'La subasta finalizó.'}
            </div>
        )
    }

    if (auction.state === 'UNSOLD') {
        return (
            <div className="alert alert-secondary" role="status">
                La subasta finalizó sin ofertas.
            </div>
        )
    }

    if (auction.state !== 'ACTIVE') return null

    if (isLeading) {
        return (
            <div className="alert alert-success" role="status">
                <strong>Liderando.</strong> Tu oferta es la más alta.
            </div>
        )
    }

    if (auction.currentUserHasBid) {
        return (
            <div className="alert alert-warning" role="status">
                <strong>Superado.</strong> Otro postor tiene una oferta
                mayor. Podés volver a ofertar mientras la subasta siga
                abierta.
            </div>
        )
    }

    return (
        <div className="alert alert-light border" role="status">
            Todavía no participaste en esta subasta.
        </div>
    )
}

export default BidStatus