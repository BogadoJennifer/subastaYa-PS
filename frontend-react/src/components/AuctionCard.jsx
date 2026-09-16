import { Badge, Card } from 'react-bootstrap'

const estados = {
    SCHEDULED: { texto: 'Próxima', color: 'primary' },
    ACTIVE: { texto: 'Activa', color: 'success' },
    FINISHED: { texto: 'Finalizada', color: 'secondary' },
    UNSOLD: { texto: 'Desierta', color: 'secondary' },
}

const formatoPrecio = new Intl.NumberFormat('es-AR', {
    style: 'currency',
    currency: 'ARS',
})

function TarjetaSubasta({ subasta }) {
    const estado = estados[subasta.state] ?? {
        texto: subasta.state,
        color: 'secondary',
    }

    return (
        <Card className="h-100 shadow-sm overflow-hidden">
            {subasta.imageUrl ? (
                <Card.Img
                    variant="top"
                    src={subasta.imageUrl}
                    alt={subasta.title}
                    loading="lazy"
                    style={{ height: '200px', objectFit: 'cover' }}
                />
            ) : (
                <div
                    className="bg-light text-secondary d-flex
                     align-items-center justify-content-center"
                    style={{ height: '200px' }}
                >
                    Sin imagen disponible
                </div>
            )}

            <Card.Body>
                <div className="d-flex justify-content-between gap-2 mb-3">
          <span className="text-secondary small">
            {subasta.categories?.name ?? 'Sin categoría'}
          </span>

                    <Badge bg={estado.color}>{estado.texto}</Badge>
                </div>

                <Card.Title as="h2" className="h5">
                    {subasta.title}
                </Card.Title>

                <Card.Text className="text-secondary">
                    {subasta.description || 'Sin descripción.'}
                </Card.Text>

                <div className="border-top pt-3">
                    <div className="small text-secondary">Precio base</div>

                    <div className="fs-4 fw-bold">
                        {formatoPrecio.format(subasta.basePrice)}
                    </div>
                </div>
            </Card.Body>
        </Card>
    )
}

export default TarjetaSubasta