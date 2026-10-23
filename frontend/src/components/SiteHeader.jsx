import { Container, Navbar } from 'react-bootstrap'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { getCurrentUser } from '../utils/api.js'

function SiteHeader() {
    const location = useLocation()
    const navigate = useNavigate()
    const currentUser = getCurrentUser()
    const isAdminLogin = location.pathname === '/admin/login'

    function handleLogout() {
        sessionStorage.removeItem('subastaYaAuth')
        navigate('/login', { replace: true })
    }

    const isActivitiesPage = location.pathname.includes('/activities')
    const isWalletPage = location.pathname.includes('/wallet')

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
                <Container className="d-flex align-items-center justify-content-between">
                    <a
                        href="/"
                        className="catalog-link"
                        onClick={(event) => {
                            event.preventDefault()
                            navigate('/')
                        }}
                    >
                        Explorar subastas
                    </a>

                    <div className="d-flex align-items-center gap-3">
                        {currentUser ? (
                            <>
                                <span> {currentUser.name} · {currentUser.role === 'ADMIN' ? 'Administrador' : 'Usuario'}</span>
                                <button
                                    type="button"
                                    className="btn btn-outline-secondary btn-sm"
                                    onClick={handleLogout}
                                >
                                    Cerrar sesión
                                </button>
                            </>
                        ) : (
                            !isAdminLogin && (
                                <>
                                    <Link to="/login">Iniciar sesión</Link>
                                    <Link to="/register">Crear cuenta</Link>
                                </>
                            )
                        )}
                    </div>
                </Container>
            </Navbar>

            {!isActivitiesPage && !isWalletPage && (
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
            )}
        </header>
    )
}

export default SiteHeader