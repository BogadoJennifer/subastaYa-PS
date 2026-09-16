import { useEffect, useState } from 'react'
import AuctionCard from './components/AuctionCard.jsx'

import {
    Alert,
    Col,
    Container,
    Navbar,
    Row,
    Spinner,
} from 'react-bootstrap'

function App() {
    const [subastas, setSubastas] = useState([])
    const [cargando, setCargando] = useState(true)
    const [error, setError] = useState('')

    useEffect(() => {
        const controller = new AbortController()

        async function cargarSubastas() {
            try {
                const respuesta = await fetch('/api/auctions', {
                    signal: controller.signal,
                })

                if (!respuesta.ok) {
                    throw new Error(`No se pudieron cargar las subastas (${respuesta.status})`)
                }

                const datos = await respuesta.json()

                if (!Array.isArray(datos)) {
                    throw new Error('El servidor devolvió un formato inesperado')
                }

                setSubastas(datos)
            } catch (error) {
                if (!controller.signal.aborted) {
                    setError(error.message)
                }
            } finally {
                if (!controller.signal.aborted) {
                    setCargando(false)
                }
            }
        }

        cargarSubastas()

        return () => controller.abort()
    }, [])

    return (
        <>
            <Navbar bg="dark" data-bs-theme="dark">
                <Container>
                    <Navbar.Brand>SubastaYa</Navbar.Brand>
                </Container>
            </Navbar>

            <Container className="py-4">
                <h1 className="mb-4">Subastas</h1>

                {cargando && (
                    <div role="status">
                        <Spinner size="sm" className="me-2" />
                        Cargando subastas…
                    </div>
                )}

                {error && <Alert variant="danger">{error}</Alert>}

                {!cargando && !error && subastas.length === 0 && (
                    <Alert variant="info">
                        Todavía no hay subastas registradas.
                    </Alert>
                )}

                {!cargando && !error && (
                    <Row xs={1} md={2} lg={3} className="g-4">
                        {subastas.map((subasta) => (
                            <Col key={subasta.id}>
                                <AuctionCard subasta={subasta} />
                            </Col>
                        ))}
                    </Row>
                )}
            </Container>
        </>
    )
}

export default App