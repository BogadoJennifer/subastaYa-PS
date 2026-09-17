import { useEffect, useState } from 'react'
import { Routes, Route } from 'react-router-dom'
import AuctionCard from './components/AuctionCard.jsx'
import LiveBiddingRoom from './pages/LiveBiddingRoomPage.jsx'
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
    const [selectedCategory, setSelectedCategory] = useState('ALL')
    const [minimumPrice, setMinimumPrice] = useState('')
    const [maximumPrice, setMaximumPrice] = useState('')
    const [sortOrder, setSortOrder] = useState('DEFAULT')

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

    const categories = Array.from(
        new Map(
            auctions
                .filter((auction) => auction.categoryId != null)
                .map((auction) => [
                    String(auction.categoryId),
                    {
                        id: String(auction.categoryId),
                        name: auction.categoryName ?? 'Sin nombre',
                    },
                ])
        ).values()
    ).sort((first, second) =>
        first.name.localeCompare(second.name, 'es')
    )

    const normalizedSearch = searchTerm.trim().toLocaleLowerCase('es')

    const minimumValue =
        minimumPrice === '' ? null : Number(minimumPrice)

    const maximumValue =
        maximumPrice === '' ? null : Number(maximumPrice)

    const isInvalidPriceRange =
        (minimumValue !== null &&
            (!Number.isFinite(minimumValue) || minimumValue < 0)) ||
        (maximumValue !== null &&
            (!Number.isFinite(maximumValue) || maximumValue < 0)) ||
        (minimumValue !== null &&
            maximumValue !== null &&
            minimumValue > maximumValue)

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

        const matchesCategory =
            selectedCategory === 'ALL' ||
            String(auction.categoryId) === selectedCategory

        const currentPrice = Number(
            auction.highestBid ?? auction.basePrice
        )

        const matchesPrice =
            (minimumValue === null || currentPrice >= minimumValue) &&
            (maximumValue === null || currentPrice <= maximumValue)

        return (
            !isInvalidPriceRange &&
            matchesSearch &&
            matchesState &&
            matchesCategory &&
            matchesPrice
        )
    })

    function getTimeGroup(auction) {
        if (auction.state === 'ACTIVE') return 0
        if (auction.state === 'SCHEDULED') return 1
        return 2
    }

    function getTargetTimestamp(auction) {
        const targetDate =
            auction.state === 'SCHEDULED'
                ? auction.startDate
                : auction.endDate

        const timestamp = targetDate
            ? new Date(targetDate).getTime()
            : NaN

        return Number.isFinite(timestamp)
            ? timestamp
            : Number.MAX_SAFE_INTEGER
    }

    const sortedAuctions = [...filteredAuctions].sort((first, second) => {
        if (sortOrder === 'HIGHEST_BID') {
            const firstHasBids = first.highestBid != null
            const secondHasBids = second.highestBid != null

            if (firstHasBids !== secondHasBids) {
                return firstHasBids ? -1 : 1
            }

            if (firstHasBids && secondHasBids) {
                const amountDifference =
                    Number(second.highestBid) - Number(first.highestBid)

                if (amountDifference !== 0) {
                    return amountDifference
                }
            }
        }

        if (sortOrder === 'ENDING_SOON') {
            const groupDifference =
                getTimeGroup(first) - getTimeGroup(second)

            if (groupDifference !== 0) {
                return groupDifference
            }

            if (getTimeGroup(first) !== 2) {
                const timeDifference =
                    getTargetTimestamp(first) -
                    getTargetTimestamp(second)

                if (timeDifference !== 0) {
                    return timeDifference
                }
            }
        }

        return first.id - second.id
    })

    return (
        <Routes>

            {/* PÁGINA PRINCIPAL */}
            <Route
                path="/"
                element={
                    <>
                        <Navbar bg="dark" data-bs-theme="dark">
                            <Container>
                                <Navbar.Brand>
                                    SubastaYa
                                </Navbar.Brand>
                            </Container>
                        </Navbar>

                        <Container className="py-4">
                            <h1 className="mb-4">
                                Subastas
                            </h1>

                            <Row className="g-3 mb-4">
                                <Col xs={12} md={6}>
                                    <Form.Group controlId="auctionSearch">
                                        <Form.Label>
                                            Buscar subastas
                                        </Form.Label>

                                        <Form.Control
                                            type="search"
                                            placeholder="Buscar por título o descripción"
                                            value={searchTerm}
                                            onChange={(event) =>
                                                setSearchTerm(event.target.value)
                                            }
                                        />
                                    </Form.Group>
                                </Col>

                                <Col xs={12} md={3}>
                                    <Form.Group controlId="auctionState">
                                        <Form.Label>
                                            Estado
                                        </Form.Label>

                                        <Form.Select
                                            value={selectedState}
                                            onChange={(event) =>
                                                setSelectedState(event.target.value)
                                            }
                                        >
                                            <option value="ALL">
                                                Todos los estados
                                            </option>
                                            <option value="ACTIVE">
                                                Activas
                                            </option>
                                            <option value="SCHEDULED">
                                                Próximas
                                            </option>
                                            <option value="FINISHED">
                                                Finalizadas
                                            </option>
                                        </Form.Select>
                                    </Form.Group>
                                </Col>

                                <Col xs={12} md={3}>
                                    <Form.Group controlId="auctionCategory">
                                        <Form.Label>
                                            Categoría
                                        </Form.Label>

                                        <Form.Select
                                            value={selectedCategory}
                                            onChange={(event) =>
                                                setSelectedCategory(event.target.value)
                                            }
                                        >
                                            <option value="ALL">
                                                Todas las categorías
                                            </option>

                                            {categories.map((category) => (
                                                <option
                                                    key={category.id}
                                                    value={category.id}
                                                >
                                                    {category.name}
                                                </option>
                                            ))}
                                        </Form.Select>
                                    </Form.Group>
                                </Col>
                            </Row>

                            <Row className="g-3 mb-4">
                                <Col xs={12} md={4}>
                                    <Form.Group controlId="minimumPrice">
                                        <Form.Label>
                                            Precio mínimo
                                        </Form.Label>

                                        <Form.Control
                                            type="number"
                                            min="0"
                                            step="0.01"
                                            placeholder="Sin mínimo"
                                            value={minimumPrice}
                                            isInvalid={isInvalidPriceRange}
                                            onChange={(event) =>
                                                setMinimumPrice(event.target.value)
                                            }
                                        />
                                    </Form.Group>
                                </Col>

                                <Col xs={12} md={4}>
                                    <Form.Group controlId="maximumPrice">
                                        <Form.Label>
                                            Precio máximo
                                        </Form.Label>

                                        <Form.Control
                                            type="number"
                                            min="0"
                                            step="0.01"
                                            placeholder="Sin máximo"
                                            value={maximumPrice}
                                            isInvalid={isInvalidPriceRange}
                                            onChange={(event) =>
                                                setMaximumPrice(event.target.value)
                                            }
                                        />
                                    </Form.Group>
                                </Col>

                                <Col xs={12} md={4}>
                                    <Form.Group controlId="auctionSort">
                                        <Form.Label>
                                            Ordenar por
                                        </Form.Label>

                                        <Form.Select
                                            value={sortOrder}
                                            onChange={(event) =>
                                                setSortOrder(event.target.value)
                                            }
                                        >
                                            <option value="DEFAULT">
                                                Orden predeterminado
                                            </option>

                                            <option value="ENDING_SOON">
                                                Menor tiempo restante
                                            </option>

                                            <option value="HIGHEST_BID">
                                                Mayor puja
                                            </option>
                                        </Form.Select>
                                    </Form.Group>
                                </Col>
                            </Row>

                            <p className="small text-secondary">
                                El rango usa la oferta más alta o el precio
                                base si no hay ofertas.
                            </p>

                            {isInvalidPriceRange && (
                                <Alert variant="warning">
                                    Ingresá precios válidos, mayores o
                                    iguales a cero. El mínimo no puede
                                    superar al máximo.
                                </Alert>
                            )}

                            {isLoading && (
                                <div role="status">
                                    <Spinner size="sm" className="me-2" />
                                    Cargando subastas…
                                </div>
                            )}

                            {error && (
                                <Alert variant="danger">
                                    {error}
                                </Alert>
                            )}

                            {!isLoading &&
                                !error &&
                                auctions.length === 0 && (
                                    <Alert variant="info">
                                        Todavía no hay subastas registradas.
                                    </Alert>
                                )}

                            {!isLoading &&
                                !error &&
                                !isInvalidPriceRange &&
                                auctions.length > 0 &&
                                filteredAuctions.length === 0 && (
                                    <Alert variant="info">
                                        No hay subastas que coincidan con
                                        tu búsqueda y los filtros.
                                    </Alert>
                                )}

                            {!isLoading && !error && (
                                <Row
                                    xs={1}
                                    md={2}
                                    lg={3}
                                    className="g-4"
                                >
                                    {sortedAuctions.map((auction) => (
                                        <Col key={auction.id}>
                                            <AuctionCard
                                                auction={auction}
                                            />
                                        </Col>
                                    ))}
                                </Row>
                            )}
                        </Container>
                    </>
                }
            />

            {/* SALA DE SUBASTA EN VIVO */}
            <Route
                path="/auctions/:auctionId/live"
                element={<LiveBiddingRoom />}
            />

        </Routes>
    )
}

export default App