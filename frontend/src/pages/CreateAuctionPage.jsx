//responsabilidad
//coordina:
// - Carga de categorías.
// - Valores del formulario.
// - Validaciones de pantalla.
// - Envío.
// - Respuesta de éxito o error.

import { useEffect, useState } from 'react'

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
        <div>
            <h1>Publicar subasta</h1>

            {message && (
                <div>{message}</div>
            )}

            <form onSubmit={handleSubmit}>
                <div>
                    <label>Título</label>
                    <input
                        type="text"
                        value={title}
                        onChange={(event) => setTitle(event.target.value)}
                    />

                    {errors.title && (
                        <div>{errors.title}</div>
                    )}
                </div>

                <div>
                    <label>Descripción</label>
                    <textarea
                        value={description}
                        onChange={(event) => setDescription(event.target.value)}
                    />
                </div>
                {errors.description && (
                    <div>{errors.description}</div>
                )}
                <div>
                    <label>URL de la imagen</label>
                    <input
                        type="text"
                        value={imageUrl}
                        onChange={(event) => setImageUrl(event.target.value)}
                    />
                </div>
                {errors.imageUrl && (
                    <div>{errors.imageUrl}</div>
                )}
                <div>
                    <label>Categoría</label>
                    <select
                        value={categoryId}
                        onChange={(event) => setCategoryId(event.target.value)}
                    >
                        <option value="">Seleccioná una categoría</option>

                        {categories.map((category) => (
                            <option key={category.id} value={category.id}>
                                {category.name}
                            </option>
                        ))}
                    </select>
                    {errors.categoryId && (
                        <div>{errors.categoryId}</div>
                    )}
                </div>

                <div>
                    <label>Precio inicial</label>
                    <input
                        type="number"
                        value={basePrice}
                        onChange={(event) => setBasePrice(event.target.value)}
                    />
                </div>
                {errors.basePrice && (
                    <div>{errors.basePrice}</div>
                )}
                <div>
                    <label>Incremento mínimo</label>
                    <input
                        type="number"
                        value={minimumIncrement}
                        onChange={(event) => setMinimumIncrement(event.target.value)}
                    />
                </div>
                {errors.minimumIncrement && (
                    <div>{errors.minimumIncrement}</div>
                )}
                <div>
                    <label>Fecha y hora de inicio</label>
                    <input
                        type="datetime-local"
                        value={startDate}
                        onChange={(event) => setStartDate(event.target.value)}
                    />
                </div>
                {errors.startDate && (
                    <div>{errors.startDate}</div>
                )}
                <div>
                    <label>Fecha y hora de finalización</label>
                    <input
                        type="datetime-local"
                        value={endDate}
                        onChange={(event) => setEndDate(event.target.value)}
                    />
                </div>
                {errors.endDate && (
                    <div>{errors.endDate}</div>
                )}
                    <button type="submit">
                        Publicar subasta
                    </button>
            </form>
        </div>
    )
}

export default CreateAuctionPage;
