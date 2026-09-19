//responsabilidad
//coordina:
// - Carga de categorías.
// - Valores del formulario.
// - Validaciones de pantalla.
// - Envío.
// - Respuesta de éxito o error.

import { useState } from 'react'

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
    function handleSubmit(event) {
        event.preventDefault()

        console.log({
            title,
            description,
            imageUrl,
            categoryId,
            basePrice,
            minimumIncrement,
            startDate,
            endDate,
        })
    }
    return (
        <div>
            <h1>Publicar subasta</h1>

            <form onSubmit={handleSubmit}>
                <div>
                    <label>Título</label>
                    <input
                        type="text"
                        value={title}
                        onChange={(event) => setTitle(event.target.value)}
                    />
                </div>

                <div>
                    <label>Descripción</label>
                    <textarea
                        value={description}
                        onChange={(event) => setDescription(event.target.value)}
                    />
                </div>
                <div>
                    <label>URL de la imagen</label>
                    <input
                        type="text"
                        value={imageUrl}
                        onChange={(event) => setImageUrl(event.target.value)}
                    />
                </div>
                <div>
                    <label>Categoría</label>
                    <select
                        value={categoryId}
                        onChange={(event) => setCategoryId(event.target.value)}
                    >
                    </select>
                </div>
                <div>
                    <label>Precio inicial</label>
                    <input
                        type="number"
                        value={basePrice}
                        onChange={(event) => setBasePrice(event.target.value)}
                    />
                </div>

                <div>
                    <label>Incremento mínimo</label>
                    <input
                        type="number"
                        value={minimumIncrement}
                        onChange={(event) => setMinimumIncrement(event.target.value)}
                    />
                </div>
                <div>
                    <label>Fecha y hora de inicio</label>
                    <input
                        type="datetime-local"
                        value={startDate}
                        onChange={(event) => setStartDate(event.target.value)}
                    />
                </div>

                <div>
                    <label>Fecha y hora de finalización</label>
                    <input
                        type="datetime-local"
                        value={endDate}
                        onChange={(event) => setEndDate(event.target.value)}
                    />
                </div>
                    <button type="submit">
                        Publicar subasta
                    </button>
            </form>
        </div>
    )
}

export default CreateAuctionPage;
