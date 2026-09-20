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
            <div className="sidebar-heading">
                <h3 className="sidebar-title">Filtrar subastas</h3>
                <button
                    type="button"
                    className="sidebar-reset-link"
                    onClick={onReset}
                >
                    Limpiar
                </button>
            </div>

            <div className="sidebar-filter-group">
                <Form.Group controlId="auctionSearch">
                    <Form.Label>Buscar</Form.Label>

                    <Form.Control
                        type="search"
                        placeholder="Título o descripción"
                        value={filters.searchTerm}
                        onChange={(event) =>
                            onFilterChange('searchTerm', event.target.value)
                        }
                    />
                </Form.Group>
            </div>

            <div className="sidebar-filter-group">
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
            </div>

            <div className="sidebar-filter-group">
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
            </div>

            <div className="sidebar-filter-group">
                <Form.Label>Rango de precio</Form.Label>

                <Row className="g-2">
                    <Col xs={6}>
                        <Form.Control
                            type="number"
                            min="0"
                            step="0.01"
                            placeholder="Mínimo"
                            value={filters.minimumPrice}
                            isInvalid={isInvalidPriceRange}
                            onChange={(event) =>
                                onFilterChange('minimumPrice', event.target.value)
                            }
                        />
                    </Col>

                    <Col xs={6}>
                        <Form.Control
                            type="number"
                            min="0"
                            step="0.01"
                            placeholder="Máximo"
                            value={filters.maximumPrice}
                            isInvalid={isInvalidPriceRange}
                            onChange={(event) =>
                                onFilterChange('maximumPrice', event.target.value)
                            }
                        />
                    </Col>
                </Row>
            </div>

            <p className="sidebar-help">
                El rango usa la oferta más alta o el precio base si no hay ofertas.
            </p>

            {isInvalidPriceRange && (
                <Alert variant="warning" className="sidebar-alert">
                    El mínimo no puede superar al máximo.
                </Alert>
            )}
        </aside>
    )
}

export default AuctionFilters