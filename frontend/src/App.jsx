import SiteHeader from './components/SiteHeader.jsx'
import SiteFooter from './components/SiteFooter.jsx'
import { Routes, Route, Navigate } from 'react-router-dom'
import AuctionCatalog from './pages/AuctionCatalog.jsx'
import LiveBiddingRoomPage from './pages/LiveBiddingRoomPage.jsx'
import CreateAuctionPage from './pages/CreateAuctionPage.jsx'
import WalletPage from "./pages/WalletPage.jsx";
import UserActivitiesPage from './pages/UserActivitiesPage';
import LoginPage from './pages/LoginPage.jsx'
import RegisterPage from './pages/RegisterPage.jsx'
import { getCurrentUser } from './utils/api.js'
import AdminDashboardPage from './pages/AdminDashboardPage.jsx'

function AdminRoute() {
    const currentUser = getCurrentUser()

    if (!currentUser) {
        return <Navigate to="/login" replace />
    }

    if (currentUser.role !== 'ADMIN') {
        return <Navigate to="/" replace />
    }

    return <AdminDashboardPage />
}

function App() {
    return (
        <>
            <SiteHeader />

            <Routes>
                <Route
                    path="/login"
                    element={<LoginPage />}
                />
                <Route
                    path="/register"
                    element={<RegisterPage />}
                />
                <Route
                    path="/admin/login"
                    element={<Navigate to="/login" replace />}
                />
                <Route
                    path="/admin"
                    element={<AdminRoute />} />
                <Route
                    path="/pages/AuctionCatalog"
                    element={<AuctionCatalog />}
                />
                <Route
                    path="/" element={<AuctionCatalog />}
                />
                <Route
                    path="/auctions/:auctionId/live"
                    element={<LiveBiddingRoomPage />}
                />
                <Route
                    path="/auctions/new"
                    element={<CreateAuctionPage/>}
                />

                <Route path="/users/activities"
                       element={<UserActivitiesPage />}
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