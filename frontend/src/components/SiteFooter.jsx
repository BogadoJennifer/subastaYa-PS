import { Col, Container, Row } from 'react-bootstrap'

function SiteFooter() {
    const currentYear = new Date().getFullYear()

    return (
        <footer className="site-footer">
            <Container>
                <Row className="g-4">
                    <Col xs={12} md={5}>
                        <a href="#catalog" className="footer-brand">
                            SubastaYa<span>.</span>
                        </a>

                        <p className="footer-description">
                            Encontrá productos, explorá oportunidades y seguí
                            tus subastas favoritas en un solo lugar.
                        </p>
                    </Col>

                    <Col xs={12} sm={6} md={3}>
                        <h2 className="footer-title">Explorá</h2>

                        <nav aria-label="Navegación del pie de página">
                            <ul className="footer-links">
                                <li>
                                    <a href="#catalog">Catálogo de subastas</a>
                                </li>
                                <li>
                                    <a href="#auctionSearch">Buscar productos</a>
                                </li>
                                <li>
                                    <a href="#auctionCategory">Filtrar por categoría</a>
                                </li>
                            </ul>
                        </nav>
                    </Col>

                    <Col xs={12} sm={6} md={4}>
                        <h2 className="footer-title">Cómo participar</h2>

                        <ol className="footer-steps">
                            <li>Explorá el catálogo.</li>
                            <li>Revisá el precio y el tiempo restante.</li>
                            <li>Elegí la subasta que te interesa.</li>
                        </ol>
                    </Col>
                </Row>

                <div className="footer-bottom">
                    <span>© {currentYear} SubastaYa</span>
                    <span>Proyecto académico · UNAJ</span>
                </div>
            </Container>
        </footer>
    )
}

export default SiteFooter