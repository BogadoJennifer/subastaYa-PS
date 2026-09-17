import { useParams } from 'react-router-dom'

function LiveBiddingRoom() {

    const { auctionId } = useParams()

    return (
        <div className="container py-4">

            <h1>Sala de Subasta en Vivo</h1>

            <p>
                Subasta seleccionada: {auctionId}
            </p>

        </div>
    )
}

export default LiveBiddingRoom