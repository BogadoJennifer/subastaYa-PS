import { useEffect, useState } from 'react'
import {
    Alert,
    Card,
    Col,
    Container,
    Row,
    Spinner,
    Table,
} from 'react-bootstrap'
import { apiFetch, getCurrentUser } from '../utils/api.js'

async function fetchJson(url) {
    const response = await apiFetch(url)

    if (!response.ok) {
        throw new Error(`Error al consultar ${url}: HTTP ${response.status}`)
    }

    return response.json()
}

function AdminDashboardPage() {
    const currentUser = getCurrentUser()
    const [data, setData] = useState({
        auctions: [],
        users: [],
        logs: [],
    })
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState('')

    useEffect(() => {
        async function loadDashboard() {
            try {
                const [auctions, users, logs] = await Promise.all([
                    fetchJson('/api/auctions/catalog'),
                    fetchJson('/api/users'),
                    fetchJson('/api/audit-logs'),
                ])

                setData({ auctions, users, logs })
            } catch (requestError) {
                setError(requestError.message || 'No se pudo cargar el panel')
            } finally {
                setLoading(false)
            }
        }

        loadDashboard()
    }, [])

    const activeAuctions = data.auctions.filter(
        (auction) => auction.state === 'ACTIVE'
    ).length

    const scheduledAuctions = data.auctions.filter(
        (auction) => auction.state === 'SCHEDULED'
    ).length

    const finishedAuctions = data.auctions.filter(
        (auction) => auction.state === 'FINISHED'
    ).length

    const recentLogs = data.logs.slice(0, 8)

    return (
        <Container as="main" className="py-5">
            <p>Administración</p>
            <h1>Panel de control</h1>
            <p>Bienvenido, {currentUser?.name}.</p>

            {loading && (
                <div className="py-4">
                    <Spinner animation="border" />
                    <span className="ms-2">Cargando información...</span>
                </div>
            )}

            {error && <Alert variant="danger">{error}</Alert>}

            {!loading && !error && (
                <>
                    <Row xs={1} md={2} lg={4} className="g-4 mb-4">
                        <Col>
                            <Card className="h-100">
                                <Card.Body>
                                    <Card.Title>Usuarios</Card.Title>
                                    <Card.Text>{data.users.length}</Card.Text>
                                </Card.Body>
                            </Card>
                        </Col>

                        <Col>
                            <Card className="h-100">
                                <Card.Body>
                                    <Card.Title>Subastas activas</Card.Title>
                                    <Card.Text>{activeAuctions}</Card.Text>
                                </Card.Body>
                            </Card>
                        </Col>

                        <Col>
                            <Card className="h-100">
                                <Card.Body>
                                    <Card.Title>Subastas próximas</Card.Title>
                                    <Card.Text>{scheduledAuctions}</Card.Text>
                                </Card.Body>
                            </Card>
                        </Col>

                        <Col>
                            <Card className="h-100">
                                <Card.Body>
                                    <Card.Title>Eventos auditados</Card.Title>
                                    <Card.Text>{data.logs.length}</Card.Text>
                                </Card.Body>
                            </Card>
                        </Col>
                    </Row>

                    <h2 className="h4 mt-5">Subastas</h2>

                    {data.auctions.length === 0 ? (
                        <p>Todavía no hay subastas registradas.</p>
                    ) : (
                        <Table responsive striped bordered>
                            <thead>
                            <tr>
                                <th>Título</th>
                                <th>Categoría</th>
                                <th>Estado</th>
                                <th>Puja más alta</th>
                                <th>Fecha de cierre</th>
                            </tr>
                            </thead>
                            <tbody>
                            {data.auctions.map((auction) => (
                                <tr key={auction.id}>
                                    <td>{auction.title || '—'}</td>
                                    <td>{auction.categoryName || '—'}</td>
                                    <td>{auction.state || '—'}</td>
                                    <td>
                                        {auction.highestBid != null
                                            ? `$${Number(auction.highestBid).toLocaleString('es-AR')}`
                                            : '—'}
                                    </td>
                                    <td>
                                        {auction.endDate
                                            ? new Date(auction.endDate).toLocaleString('es-AR')
                                            : '—'}
                                    </td>
                                </tr>
                            ))}
                            </tbody>
                        </Table>
                    )}

                    <h2 className="h4 mt-5">Usuarios registrados</h2>

                    {data.users.length === 0 ? (
                        <p>Todavía no hay usuarios registrados.</p>
                    ) : (
                        <Table responsive striped bordered>
                            <thead>
                            <tr>
                                <th>Nombre</th>
                                <th>Email</th>
                                <th>Rol</th>
                                <th>Fecha de registro</th>
                            </tr>
                            </thead>
                            <tbody>
                            {data.users.map((user) => (
                                <tr key={user.id}>
                                    <td>{user.name || '—'}</td>
                                    <td>{user.email || '—'}</td>
                                    <td>{user.role || '—'}</td>
                                    <td>
                                        {user.registrationDate
                                            ? new Date(user.registrationDate).toLocaleString('es-AR')
                                            : '—'}
                                    </td>
                                </tr>
                            ))}
                            </tbody>
                        </Table>
                    )}

                    <p className="text-muted">
                        Subastas finalizadas: {finishedAuctions}
                    </p>
                </>
            )}
        </Container>
    )
}

export default AdminDashboardPage