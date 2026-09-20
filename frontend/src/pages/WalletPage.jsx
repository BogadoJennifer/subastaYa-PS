import { useEffect, useState } from 'react'
import {
    Container,
    Row,
    Col,
    Card,
    Button,
    Form,
    Alert,
    Spinner,
    Table
} from 'react-bootstrap'
import WalletTransactions from '../components/WalletTransactions.jsx'
import { useParams } from 'react-router-dom'

function WalletPage() {
    const { userId } = useParams()

    // Temporalmente usamos el usuario 1.
    // Después lo reemplazamos por el usuario autenticado.
    //const userId = 1

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

            const response = await fetch(
                `/api/wallets/user/${userId}`
            )

            if (!response.ok) {
                throw new Error('No se pudo obtener la billetera')
            }

            const data = await response.json()
            console.log("WALLET:", data)

            setWallet(data)

            if (data.walletId) {
                await loadTransactions(data.walletId)
            }

        } catch (error) {
            setError(error.message)
        } finally {
            setIsLoading(false)
        }
    }

    const loadTransactions = async (walletId) => {
        try {
            const response = await fetch(
                `/api/wallets/${walletId}/transactions`
            )

            if (!response.ok) {
                throw new Error('No se pudieron obtener los movimientos')
            }

            const data = await response.json()

            setTransactions(data)

        } catch (error) {
            setError(error.message)
        }
    }

    useEffect(() => {
        loadWallet()
    }, [])

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

    const formatDate = (date) => {
        if (!date) {
            return '-'
        }

        return new Date(date).toLocaleString('es-AR')
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