import SiteHeader from './components/SiteHeader.jsx'
import SiteFooter from './components/SiteFooter.jsx'
import { Routes, Route } from 'react-router-dom'
import AuctionCatalog from './pages/AuctionCatalog.jsx'
import LiveBiddingRoomPage from './pages/LiveBiddingRoomPage.jsx'
import WalletPage from "./pages/WalletPage.jsx";

function App() {
    return (
        <>
            <SiteHeader />

            <Routes>
                <Route
                    path="/"
                    element={<AuctionCatalog />}
                />

                <Route
                    path="/auctions/:auctionId/live"
                    element={<LiveBiddingRoomPage />}
                />

                <Route
                    path="/wallet"
                    element={<WalletPage />}
                />
            </Routes>

            <SiteFooter />
        </>
    )
}

export default App