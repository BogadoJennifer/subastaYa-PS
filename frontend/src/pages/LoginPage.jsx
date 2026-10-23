import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'

function LoginPage() {
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
            const response = await fetch('/api/auth/login', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({ email, password }),
            })

            const data = await response.json().catch(() => null)

            if (!response.ok) {
                throw new Error(data?.message || 'Email o contraseña incorrectos')
            }

            if (data.role !== 'USER' && data.role !== 'ADMIN') {
                throw new Error('El servidor devolvió un rol desconocido')
            }

            sessionStorage.setItem('subastaYaAuth', JSON.stringify(data))
            navigate(data.role === 'ADMIN' ? '/admin' : '/')
        } catch (requestError) {
            setError(requestError.message)
        } finally {
            setIsSubmitting(false)
        }
    }

    return (
        <main className="container py-5" style={{ maxWidth: '480px' }}>
            <h1 className="h3 mb-4">
                Iniciar sesión
            </h1>

            {error && (
                <div className="alert alert-danger" role="alert">
                    {error}
                </div>
            )}

            <form onSubmit={handleSubmit}>
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
                        required
                    />
                </div>

                <button
                    type="submit"
                    className="btn btn-primary w-100"
                    disabled={isSubmitting}
                >
                    {isSubmitting ? 'Ingresando...' : 'Ingresar'}
                </button>
            </form>

                <p className="mt-3 mb-0 text-center">
                    ¿No tenés cuenta? <Link to="/register">Crear una cuenta</Link>
                </p>

        </main>
    )
}

export default LoginPage