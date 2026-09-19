import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import {
    Alert,
    Button,
    Col,
    Container,
    Form,
    Row,
    Spinner,
} from 'react-bootstrap'
import AuctionCard from '../components/AuctionCard.jsx'
import AuctionFilters from '../components/AuctionFilters.jsx'
import useAuctions from '../hooks/useAuctions.js'
import {
    INITIAL_FILTERS,
    filterAuctions,
    getCategories,
    hasInvalidPriceRange,
    sortAuctions,
} from '../utils/auctionFilters.js'

function AuctionCatalog() {
    const { auctions, isLoading, error } = useAuctions()
    const navigate = useNavigate()
    const [filters, setFilters] = useState({ ...INITIAL_FILTERS })

    const categories = getCategories(auctions)
    const isInvalidPriceRange = hasInvalidPriceRange(filters)
    const filteredAuctions = filterAuctions(auctions, filters)
    const sortedAuctions = sortAuctions(
        filteredAuctions,
        filters.sortOrder
    )

    function updateFilter(name, value) {
        setFilters((previousFilters) => ({
            ...previousFilters,
            [name]: value,
        }))
    }

    function resetFilters() {
        setFilters({ ...INITIAL_FILTERS })
    }
    return (
        <Container as="main" id="catalog" className="catalog-section">
            <div className="d-flex justify-content-between align-items-center mb-4">
                <h2 className="h4 fw-bold mb-0">Explorá las subastas</h2>
                <Button
                    variant="primary"
                    onClick={() => navigate('/auctions/new')}
                >
                    Publicar subasta
                </Button>
                <Button onClick={() => navigate('/wallet')}>
                    Mi billetera
                </Button>
                <Button onClick={() => navigate('/users/1/activities')}>
                    Mis actividades
                </Button>

            </div>

            <div className="catalog-layout">
                <AuctionFilters
                    filters={filters}
                    categories={categories}
                    isInvalidPriceRange={isInvalidPriceRange}
                    onFilterChange={updateFilter}
                    onReset={resetFilters}
                />

                <div className="catalog-results">
                    <div className="catalog-toolbar">
                        <p className="small text-secondary mb-0">
                            {isLoading
                                ? 'Cargando catálogo…'
                                : isInvalidPriceRange
                                    ? 'Revisá el rango de precios'
                                    : `Mostrando ${sortedAuctions.length} de ${auctions.length} subastas`}
                        </p>

                        <Form.Group
                            controlId="auctionSort"
                            className="catalog-sort"
                        >
                            <Form.Label className="visually-hidden">
                                Ordenar subastas
                            </Form.Label>

                            <Form.Select
                                value={filters.sortOrder}
                                onChange={(event) =>
                                    updateFilter('sortOrder', event.target.value)
                                }
                            >
                                <option value="DEFAULT">Orden predeterminado</option>
                                <option value="ENDING_SOON">Menor tiempo restante</option>
                                <option value="HIGHEST_BID">Mayor puja</option>
                            </Form.Select>
                        </Form.Group>
                    </div>

                    {isLoading && (
                        <div role="status">
                            <Spinner size="sm" className="me-2" />
                            Cargando subastas…
                        </div>
                    )}

                    {error && (
                        <Alert variant={auctions.length > 0 ? 'warning' : 'danger'}>
                            <div>{error}</div>

                            <div className="small mt-1">
                                {auctions.length > 0
                                    ? 'Mostramos los últimos datos recibidos. Reintentaremos automáticamente.'
                                    : 'Reintentaremos automáticamente en unos segundos.'}
                            </div>
                        </Alert>
                    )}

                    {!isLoading && !error && auctions.length === 0 && (
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
                                No hay subastas que coincidan con tu búsqueda y los filtros.
                            </Alert>
                        )}

                    {!isLoading && (
                        <Row xs={1} md={2} xl={3} className="g-4">
                            {sortedAuctions.map((auction) => (
                                <Col key={auction.id}>
                                    <AuctionCard
                                        auction={auction}
                                        onEnterRoom={() => onSelectAuction(auction.id)}
                                    />
                                </Col>
                            ))}
                        </Row>
                    )}
                </div>
            </div>
        </Container>
    )
}

export default AuctionCatalog