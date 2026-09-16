import { useEffect, useState } from 'react'
import AuctionCard from './components/AuctionCard.jsx'
import {
    Alert,
    Col,
    Container,
    Form,
    Navbar,
    Row,
    Spinner,
} from 'react-bootstrap'

function App() {
    const [auctions, setAuctions] = useState([])
    const [isLoading, setIsLoading] = useState(true)
    const [error, setError] = useState('')
    const [searchTerm, setSearchTerm] = useState('')
    const [selectedState, setSelectedState] = useState('ALL')

    useEffect(() => {
        const controller = new AbortController()

        async function loadAuctions() {
            try {
                const response = await fetch('/api/auctions/catalog', {
                    signal: controller.signal,
                })

                if (!response.ok) {
                    throw new Error(
                        `No se pudieron cargar las subastas (${response.status})`
                    )
                }

                const data = await response.json()

                if (!Array.isArray(data)) {
                    throw new Error('El servidor devolvió un formato inesperado')
                }

                if (!controller.signal.aborted) {
                    setAuctions(data)
                }
            } catch (error) {
                if (!controller.signal.aborted) {
                    setError(error.message)
                }
            } finally {
                if (!controller.signal.aborted) {
                    setIsLoading(false)
                }
            }
        }

        loadAuctions()

        return () => controller.abort()
    }, [])

    // Calculate filters outside the effect on every render.
    const normalizedSearch = searchTerm.trim().toLocaleLowerCase('es')

    const filteredAuctions = auctions.filter((auction) => {
        const searchableText = [
            auction.title,
            auction.description,
        ]
            .filter(Boolean)
            .join(' ')
            .toLocaleLowerCase('es')

        const matchesSearch = searchableText.includes(normalizedSearch)

        const matchesState =
            selectedState === 'ALL' ||
            (selectedState === 'FINISHED'
                ? ['FINISHED', 'UNSOLD'].includes(auction.state)
                : auction.state === selectedState)

        return matchesSearch && matchesState
    })

    return (
        <>
            <Navbar bg="dark" data-bs-theme="dark">
                <Container>
                    <Navbar.Brand>SubastaYa</Navbar.Brand>
                </Container>
            </Navbar>

            <Container className="py-4">
                <h1 className="mb-4">Subastas</h1>

                <Row className="g-3 mb-4">
                    <Col xs={12} md={8}>
                        <Form.Group controlId="auctionSearch">
                            <Form.Label>Buscar subastas</Form.Label>

                            <Form.Control
                                type="search"
                                placeholder="Buscar por título o descripción"
                                value={searchTerm}
                                onChange={(event) => setSearchTerm(event.target.value)}
                            />
                        </Form.Group>
                    </Col>

                    <Col xs={12} md={4}>
                        <Form.Group controlId="auctionState">
                            <Form.Label>Estado</Form.Label>

                            <Form.Select
                                value={selectedState}
                                onChange={(event) => setSelectedState(event.target.value)}
                            >
                                <option value="ALL">Todos los estados</option>
                                <option value="ACTIVE">Activas</option>
                                <option value="SCHEDULED">Próximas</option>
                                <option value="FINISHED">Finalizadas</option>
                            </Form.Select>
                        </Form.Group>
                    </Col>
                </Row>

                {isLoading && (
                    <div role="status">
                        <Spinner size="sm" className="me-2" />
                        Cargando subastas…
                    </div>
                )}

                {error && <Alert variant="danger">{error}</Alert>}

                {!isLoading && !error && auctions.length === 0 && (
                    <Alert variant="info">
                        Todavía no hay subastas registradas.
                    </Alert>
                )}

                {!isLoading &&
                    !error &&
                    auctions.length > 0 &&
                    filteredAuctions.length === 0 && (
                        <Alert variant="info">
                            No hay subastas que coincidan con tu búsqueda y los filtros.
                        </Alert>
                    )}

                {!isLoading && !error && (
                    <Row xs={1} md={2} lg={3} className="g-4">
                        {filteredAuctions.map((auction) => (
                            <Col key={auction.id}>
                                <AuctionCard auction={auction} />
                            </Col>
                        ))}
                    </Row>
                )}
            </Container>
        </>
    )
}

export default App