import { Container, Navbar } from 'react-bootstrap'

function SiteHeader() {
    return (
        <header>
            <div className="site-topbar">
                <Container className="d-flex justify-content-between gap-3">
                    <span>Encontrá tu próxima oportunidad</span>

                    <span className="d-none d-sm-inline">
            Tecnología · Coleccionables · Vehículos
          </span>
                </Container>
            </div>

            <Navbar className="site-navbar">
                <Container>
                    <Navbar.Brand href="#catalog" className="site-brand">
                        SubastaYa<span aria-hidden="true">.</span>
                    </Navbar.Brand>

                    <a href="#catalog" className="catalog-link">
                        Explorar subastas
                    </a>
                </Container>
            </Navbar>

            <section className="catalog-hero" aria-labelledby="catalogTitle">
                <Container className="hero-layout">
                    <div>
                        <p className="hero-eyebrow">Descubrí SubastaYa</p>

                        <h1 id="catalogTitle" className="hero-title">
                            Cada oferta,
                            <br />
                            una oportunidad.
                        </h1>

                        <p className="hero-description">
                            Explorá el catálogo, encontrá tus favoritos y seguí
                            las subastas que más te interesan.
                        </p>
                    </div>

                    <div className="hero-gallery" aria-hidden="true">
                        <img src="/images/nintendo.jpg" alt="" />
                        <img src="/images/ford2006.png" alt="" />
                        <img src="/images/comic.jpg" alt="" />
                    </div>
                </Container>
            </section>
        </header>
    )
}

export default SiteHeader