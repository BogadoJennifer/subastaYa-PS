import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'

function RegisterPage() {
    const [name, setName] = useState('')
    const [email, setEmail] = useState('')
    const [password, setPassword] = useState('')
    const [error, setError] = useState('')
    const [isSubmitting, setIsSubmitting] = useState(false)

    const navigate = useNavigate()

    async function handleSubmit(event) {
        event.preventDefault()
        setError('')
        setIsSubmitting(true)

        try {
            const response = await fetch('/api/auth/register', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    name: name.trim(),
                    email: email.trim(),
                    password,
                }),
            })

            const data = await response.json().catch(() => null)

            if (!response.ok) {
                if (response.status === 409) {
                    throw new Error('Ese email ya está registrado')
                }

                throw new Error(
                    data?.message || data?.detail || 'No se pudo crear la cuenta'
                )
            }

            navigate('/login')

        } catch (requestError) {
            setError(requestError.message)
        } finally {
            setIsSubmitting(false)
        }
    }

    return (
        <main className="container py-5" style={{ maxWidth: '480px' }}>
            <h1 className="h3 mb-4">Crear cuenta</h1>

            {error && (
                <div className="alert alert-danger" role="alert">
                    {error}
                </div>
            )}

            <form onSubmit={handleSubmit}>
                <div className="mb-3">
                    <label htmlFor="name" className="form-label">
                        Nombre
                    </label>
                    <input
                        id="name"
                        type="text"
                        className="form-control"
                        value={name}
                        onChange={(event) => setName(event.target.value)}
                        required
                    />
                </div>

                <div className="mb-3">
                    <label htmlFor="email" className="form-label">
                        Email
                    </label>
                    <input
                        id="email"
                        type="email"
                        className="form-control"
                        value={email}
                        onChange={(event) => setEmail(event.target.value)}
                        required
                    />
                </div>

                <div className="mb-3">
                    <label htmlFor="password" className="form-label">
                        Contraseña
                    </label>
                    <input
                        id="password"
                        type="password"
                        className="form-control"
                        value={password}
                        onChange={(event) => setPassword(event.target.value)}
                        minLength={8}
                        required
                    />
                    <div className="form-text">
                        Debe tener al menos 8 caracteres.
                    </div>
                </div>

                <button
                    type="submit"
                    className="btn btn-primary w-100"
                    disabled={isSubmitting}
                >
                    {isSubmitting ? 'Creando cuenta...' : 'Crear cuenta'}
                </button>
            </form>

            <p className="mt-3 mb-0 text-center">
                ¿Ya tenés una cuenta? <Link to="/login">Iniciá sesión</Link>
            </p>
        </main>
    )
}

export default RegisterPage