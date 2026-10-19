import { useEffect, useState } from 'react'
import {
    Container,
    Row,
    Col,
    Card,
    Button,
    Form,
    Alert,
    Spinner
} from 'react-bootstrap'
import WalletTransactions from '../components/WalletTransactions.jsx'
import { useParams } from 'react-router-dom'

async function fetchWallet(userId, signal) {
    const response = await fetch(`/api/wallets/user/${userId}`, { signal })
    if (!response.ok) {
        throw new Error('No se pudo obtener la billetera')
    }
    return response.json()
}

async function fetchTransactions(walletId, signal) {
    if (!walletId) return []
    const response = await fetch(`/api/wallets/${walletId}/transactions`, { signal })
    if (!response.ok) {
        throw new Error('No se pudieron obtener los movimientos')
    }
    return response.json()
}

function WalletPage() {
    const { userId } = useParams()
    return <WalletContent key={userId} userId={userId} />
}

function WalletContent({ userId }) {
    const [wallet, setWallet] = useState(null)
    const [transactions, setTransactions] = useState([])
    const [amount, setAmount] = useState('')
    const [isLoading, setIsLoading] = useState(true)
    const [isDepositing, setIsDepositing] = useState(false)
    const [error, setError] = useState('')
    const [success, setSuccess] = useState('')

    const loadWallet = async () => {
        try {
            setError('')

            const data = await fetchWallet(userId)
            setWallet(data)
            setTransactions(await fetchTransactions(data.walletId))

        } catch (error) {
            setError(error.message)
        } finally {
            setIsLoading(false)
        }
    }

    useEffect(() => {
        const controller = new AbortController()
        const { signal } = controller

        async function load() {
            try {
                const data = await fetchWallet(userId, signal)
                if (signal.aborted) return
                setError('')
                setWallet(data)
                setTransactions([])
                const movements = await fetchTransactions(data.walletId, signal)
                if (!signal.aborted) setTransactions(movements)
            } catch (error) {
                if (!signal.aborted) setError(error.message)
            } finally {
                if (!signal.aborted) setIsLoading(false)
            }
        }

        load()
        return () => controller.abort()
    }, [userId])

    const handleDeposit = async (event) => {
        event.preventDefault()

        if (!wallet) {
            return
        }

        const depositAmount = Number(amount)

        if (!depositAmount || depositAmount <= 0) {
            setError('Ingresá un monto mayor a cero')
            return
        }

        try {
            setIsDepositing(true)
            setError('')
            setSuccess('')

            const response = await fetch(
                `/api/wallets/${wallet.walletId}/deposits?amount=${depositAmount}`,
                {
                    method: 'POST'
                }
            )

            if (!response.ok) {
                throw new Error('No se pudo realizar el depósito')
            }

            setAmount('')
            setSuccess('Depósito realizado correctamente')

            await loadWallet()

        } catch (error) {
            setError(error.message)
        } finally {
            setIsDepositing(false)
        }
    }

    const formatMoney = (value) => {
        return new Intl.NumberFormat('es-AR', {
            style: 'currency',
            currency: 'ARS'
        }).format(value ?? 0)
    }

    if (isLoading) {
        return (
            <Container className="py-5 text-center">
                <Spinner animation="border" />
                <p className="mt-3">
                    Cargando billetera...
                </p>
            </Container>
        )
    }

    if (!wallet) {
        return (
            <Container className="py-5">
                <Alert variant="danger">
                    {error || 'No se encontró la billetera'}
                </Alert>
            </Container>
        )
    }

    return (
        <Container className="py-4">

            <h1 className="mb-4">
                Mi billetera
            </h1>

            {error && (
                <Alert
                    variant="danger"
                    dismissible
                    onClose={() => setError('')}
                >
                    {error}
                </Alert>
            )}

            {success && (
                <Alert
                    variant="success"
                    dismissible
                    onClose={() => setSuccess('')}
                >
                    {success}
                </Alert>
            )}

            <Row className="g-4 mb-4">

                <Col md={4}>
                    <Card className="h-100">
                        <Card.Body>
                            <Card.Title>
                                Saldo total
                            </Card.Title>

                            <Card.Text className="fs-3">
                                {formatMoney(wallet.totalBalance)}
                            </Card.Text>
                        </Card.Body>
                    </Card>
                </Col>

                <Col md={4}>
                    <Card className="h-100">
                        <Card.Body>
                            <Card.Title>
                                Saldo retenido
                            </Card.Title>

                            <Card.Text className="fs-3">
                                {formatMoney(wallet.retainedBalance)}
                            </Card.Text>
                        </Card.Body>
                    </Card>
                </Col>

                <Col md={4}>
                    <Card className="h-100">
                        <Card.Body>
                            <Card.Title>
                                Saldo disponible
                            </Card.Title>

                            <Card.Text className="fs-3">
                                {formatMoney(wallet.availableBalance)}
                            </Card.Text>
                        </Card.Body>
                    </Card>
                </Col>

            </Row>

            <Row className="g-4">

                <Col lg={5}>
                    <Card>
                        <Card.Body>

                            <Card.Title className="mb-3">
                                Depositar dinero
                            </Card.Title>

                            <Form onSubmit={handleDeposit}>

                                <Form.Group className="mb-3">
                                    <Form.Label>
                                        Monto
                                    </Form.Label>

                                    <Form.Control
                                        type="number"
                                        min="1"
                                        step="0.01"
                                        value={amount}
                                        onChange={(event) =>
                                            setAmount(event.target.value)
                                        }
                                        placeholder="Ej: 10000"
                                        disabled={isDepositing}
                                    />
                                </Form.Group>

                                <Button
                                    type="submit"
                                    disabled={isDepositing}
                                >
                                    {isDepositing
                                        ? 'Procesando...'
                                        : 'Depositar'}
                                </Button>

                            </Form>

                        </Card.Body>
                    </Card>
                </Col>

                <Col lg={7}>
                    <Card>
                            <Card.Body>
                                <Card.Title className="mb-3">
                                    Movimientos
                                </Card.Title>

                                <WalletTransactions
                                    transactions={transactions}
                                />
                            </Card.Body>
                    </Card>
                </Col>

            </Row>

        </Container>
    )
}

export default WalletPage
