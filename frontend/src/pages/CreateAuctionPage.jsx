//responsabilidad
//coordina:
// - Carga de categorías.
// - Valores del formulario.
// - Validaciones de pantalla.
// - Envío.
// - Respuesta de éxito o error.

import { useEffect, useState } from 'react'
import { Form, Button, Alert, Container, Card, Row, Col } from 'react-bootstrap'

function CreateAuctionPage() {
    const [title, setTitle] = useState('')
    const [description, setDescription] = useState('')
    const [imageUrl, setImageUrl] = useState('')
    const [categoryId, setCategoryId] = useState('')
    const [basePrice, setBasePrice] = useState('')
    const [minimumIncrement, setMinimumIncrement] = useState('')
    const [startDate, setStartDate] = useState('')
    const [endDate, setEndDate] = useState('')
    const [categories, setCategories] = useState([])
    const [errors, setErrors] = useState({})
    const [message, setMessage] = useState('')


    useEffect(() => {
        fetch('/api/categories')
            .then((response) => response.json())
            .then((data) => {
                setCategories(data)
            })
    }, [])

    function validateForm() {
        const newErrors = {}

        if (!title.trim()) {
            newErrors.title = 'El título es obligatorio'
        }

        if (!description.trim()) {
            newErrors.description = 'La descripción es obligatoria'
        }

        if (!imageUrl.trim()) {
            newErrors.imageUrl = 'La URL de la imagen es obligatoria'
        } else if (!/^https?:\/\/.+$/.test(imageUrl)) {
            newErrors.imageUrl = 'La URL debe comenzar con http:// o https://'
        }

        if (!categoryId) {
            newErrors.categoryId = 'Seleccioná una categoría'
        }

        if (!basePrice || Number(basePrice) <= 0) {
            newErrors.basePrice = 'El precio base debe ser mayor que 0'
        }

        if (!minimumIncrement || Number(minimumIncrement) <= 0) {
            newErrors.minimumIncrement = 'El incremento mínimo debe ser mayor que 0'
        }

        if (!startDate) {
            newErrors.startDate = 'Seleccioná una fecha de inicio'
        }

        if (!endDate) {
            newErrors.endDate = 'Seleccioná una fecha de finalización'
        }

        if (startDate && endDate) {
            const start = new Date(startDate)
            const end = new Date(endDate)

            if (end <= start) {
                newErrors.endDate =
                    'La fecha de finalización debe ser posterior a la fecha de inicio'
            }

            if (end <= new Date()) {
                newErrors.endDate =
                    'La fecha de finalización debe ser posterior a la fecha actual'
            }
        }

        setErrors(newErrors)

        return Object.keys(newErrors).length === 0
    }

    async function handleSubmit(event) {
        event.preventDefault()

        if (!validateForm()) {
            return
        }

        const response = await fetch('/api/auctions', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                title,
                description,
                imageUrl,
                categoryId: Number(categoryId),
                basePrice: Number(basePrice),
                minimumIncrement: Number(minimumIncrement),
                startDate,
                endDate,
            }),
        })
        if (response.ok) {
            setMessage('Subasta publicada correctamente')
        } else {
            setMessage('No se pudo publicar la subasta')
        }
    }
    return (
        <Container className="py-4">

            <h1 classname="mb-4">Publicar subasta</h1>

            {message && (
                <Alert
                    variant={message === 'Subasta publicada correctamente' ? 'success' : 'danger'}
                    dismissible
                    onClose={() => setMessage('')}
                >
                    {message}
                </Alert>
            )}

            <Card>
                <Card.Body>

            <Form onSubmit={handleSubmit}>

                <Form.Group className="mb-3">
                    <Form.Label>Título</Form.Label>
                    <Form.Control
                        type="text"
                        value={title}
                        onChange={(event) => setTitle(event.target.value)}
                    />

                    {errors.title && (
                        <div className="text-danger mt-1">
                            {errors.title}
                        </div>
                    )}
                </Form.Group>

                <Form.Group className="mb-3">
                    <Form.Label>Descripción</Form.Label>
                    <Form.Control
                        as="textarea"
                        rows={4}
                        value={description}
                        onChange={(event) => setDescription(event.target.value)}
                    />

                    {errors.description && (
                        <div className="text-danger mt-1">
                            {errors.description}
                        </div>
                    )}
                </Form.Group>

                <Form.Group className="mb-3">
                    <Form.Label>URL de la imagen</Form.Label>
                    <Form.Control
                        type="text"
                        value={imageUrl}
                        onChange={(event) => setImageUrl(event.target.value)}
                    />

                    {errors.imageUrl && (
                        <div className="text-danger mt-1">
                            {errors.imageUrl}
                        </div>
                    )}
                </Form.Group>

                <Form.Group className="mb-3">
                    <Form.Label>Categoría</Form.Label>
                    <Form.Select
                        value={categoryId}
                        onChange={(event) => setCategoryId(event.target.value)}
                    >
                        <option value="">Seleccioná una categoría</option>

                        {categories.map((category) => (
                            <option key={category.id} value={category.id}>
                                {category.name}
                            </option>
                        ))}
                    </Form.Select>

                    {errors.categoryId && (
                        <div className="text-danger mt-1">
                            {errors.categoryId}
                        </div>
                    )}
                </Form.Group>
                <Row>
                    <Col md={6}>
                        <Form.Group className="mb-3">
                            <Form.Label>Precio inicial</Form.Label>
                            <Form.Control
                                type="number"
                                step="0.01"
                                min="0"
                                value={basePrice}
                                onChange={(event) => setBasePrice(event.target.value)}
                                isInvalid={!!errors.basePrice}
                            />

                            {errors.basePrice && (
                                <div className="text-danger mt-1">
                                    {errors.basePrice}
                                </div>
                            )}
                        </Form.Group>
                    </Col>

                    <Col md={6}>
                        <Form.Group className="mb-3">
                            <Form.Label>Incremento mínimo</Form.Label>
                            <Form.Control
                                type="number"
                                step="0.01"
                                min="0"
                                value={minimumIncrement}
                                onChange={(event) => setMinimumIncrement(event.target.value)}
                                isInvalid={!!errors.minimumIncrement}
                            />

                            {errors.minimumIncrement && (
                                <div className="text-danger mt-1">
                                    {errors.minimumIncrement}
                                </div>
                            )}
                        </Form.Group>
                    </Col>
                </Row>
                <Row>
                    <Col md={6}>
                        <Form.Group className="mb-3">
                            <Form.Label>Fecha y hora de inicio</Form.Label>
                            <Form.Control
                                type="datetime-local"
                                value={startDate}
                                onChange={(event) => setStartDate(event.target.value)}
                                isInvalid={!!errors.startDate}
                            />

                            {errors.startDate && (
                                <div className="text-danger mt-1">
                                    {errors.startDate}
                                </div>
                            )}
                        </Form.Group>
                    </Col>

                    <Col md={6}>
                        <Form.Group className="mb-3">
                            <Form.Label>Fecha y hora de finalización</Form.Label>
                            <Form.Control
                                type="datetime-local"
                                value={endDate}
                                onChange={(event) => setEndDate(event.target.value)}
                                isInvalid={!!errors.endDate}
                            />

                            {errors.endDate && (
                                <div className="text-danger mt-1">
                                    {errors.endDate}
                                </div>
                            )}
                        </Form.Group>
                    </Col>
                </Row>
                <Button variant="primary" className="mt-2" type="submit">
                    Publicar subasta
                </Button>
            </Form>

                </Card.Body>
            </Card>

        </Container>
    )
}

export default CreateAuctionPage;
