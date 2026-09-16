import { CountdownTimer } from '../components/CountdownTimer';
import { BidConsole } from '../components/BidConsole';
import { BidHistoryList } from '../components/BidHistoryList';

export const LiveAuctionPage = () => {
    // ...conexión STOMP y estados...

    return (
        <div className="room-container">
            <h1>{auction.title}</h1>
            <CountdownTimer endDate={auction.endDate} />
            <BidConsole
                currentPrice={auction.currentPrice}
                minIncrement={auction.minIncrement}
                onBid={handleBid}
            />
            <BidHistoryList bids={bids} />
        </div>
    );
};