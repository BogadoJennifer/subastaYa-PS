import { useEffect, useState } from 'react'

export default function useAuctions() {
    const [auctions, setAuctions] = useState([])
    const [isLoading, setIsLoading] = useState(true)
    const [error, setError] = useState('')

    useEffect(() => {
        const controller = new AbortController()
        let timeoutId

        async function loadAuctions() {
            try {
                const response = await fetch('api/auctions/catalog', {
                    signal: controller.signal,
                    cache: 'no-store',
                })

                if (!response.ok) {
                    throw new Error(
                        `No se pudieron actualizar las subastas (${response.status})`
                    )
                }

                const data = await response.json()

                if (!Array.isArray(data)) {
                    throw new Error('El servidor devolvió un formato inesperado')
                }

                if (!controller.signal.aborted) {
                    setAuctions(data)
                    setError('')
                }
            } catch (error) {
                if (!controller.signal.aborted) {
                    setError(error.message)
                }
            } finally {
                if (!controller.signal.aborted) {
                    setIsLoading(false)
                    timeoutId = setTimeout(loadAuctions, 5000)
                }
            }
        }

        loadAuctions()

        return () => {
            controller.abort()
            clearTimeout(timeoutId)
        }
    }, [])

    return { auctions, isLoading, error }
}