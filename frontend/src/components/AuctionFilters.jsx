import { Alert, Button, Col, Form, Row } from 'react-bootstrap'

function AuctionFilters({
                            filters,
                            categories,
                            isInvalidPriceRange,
                            onFilterChange,
                            onReset,
                        }) {
    return (
        <aside className="catalog-sidebar" aria-label="Filtros de subastas">
            <h3 className="sidebar-title">Filtrar subastas</h3>

            <Row className="g-3 mb-4">
                <Col xs={12}>
                    <Form.Group controlId="auctionSearch">
                        <Form.Label>Buscar subastas</Form.Label>

                        <Form.Control
                            type="search"
                            placeholder="Buscar por título o descripción"
                            value={filters.searchTerm}
                            onChange={(event) =>
                                onFilterChange('searchTerm', event.target.value)
                            }
                        />
                    </Form.Group>
                </Col>

                <Col xs={12}>
                    <Form.Group controlId="auctionState">
                        <Form.Label>Estado</Form.Label>

                        <Form.Select
                            value={filters.selectedState}
                            onChange={(event) =>
                                onFilterChange('selectedState', event.target.value)
                            }
                        >
                            <option value="ALL">Todos los estados</option>
                            <option value="ACTIVE">Activas</option>
                            <option value="SCHEDULED">Próximas</option>
                            <option value="FINISHED">Finalizadas</option>
                        </Form.Select>
                    </Form.Group>
                </Col>

                <Col xs={12}>
                    <Form.Group controlId="auctionCategory">
                        <Form.Label>Categoría</Form.Label>

                        <Form.Select
                            value={filters.selectedCategory}
                            onChange={(event) =>
                                onFilterChange('selectedCategory', event.target.value)
                            }
                        >
                            <option value="ALL">Todas las categorías</option>

                            {categories.map((category) => (
                                <option key={category.id} value={category.id}>
                                    {category.name}
                                </option>
                            ))}
                        </Form.Select>
                    </Form.Group>
                </Col>
            </Row>

            <Row className="g-3 mb-4">
                <Col xs={12}>
                    <Form.Group controlId="minimumPrice">
                        <Form.Label>Precio mínimo</Form.Label>

                        <Form.Control
                            type="number"
                            min="0"
                            step="0.01"
                            placeholder="Sin mínimo"
                            value={filters.minimumPrice}
                            isInvalid={isInvalidPriceRange}
                            onChange={(event) =>
                                onFilterChange('minimumPrice', event.target.value)
                            }
                        />
                    </Form.Group>
                </Col>

                <Col xs={12}>
                    <Form.Group controlId="maximumPrice">
                        <Form.Label>Precio máximo</Form.Label>

                        <Form.Control
                            type="number"
                            min="0"
                            step="0.01"
                            placeholder="Sin máximo"
                            value={filters.maximumPrice}
                            isInvalid={isInvalidPriceRange}
                            onChange={(event) =>
                                onFilterChange('maximumPrice', event.target.value)
                            }
                        />
                    </Form.Group>
                </Col>
            </Row>

            <p className="small text-secondary">
                El rango usa la oferta más alta o el precio base si no hay ofertas.
            </p>

            {isInvalidPriceRange && (
                <Alert variant="warning">
                    Ingresá precios válidos, mayores o iguales a cero. El mínimo no
                    puede superar al máximo.
                </Alert>
            )}

            <Button
                type="button"
                variant="outline-success"
                className="w-100"
                onClick={onReset}
            >
                Limpiar filtros
            </Button>
        </aside>
    )
}

export default AuctionFilters