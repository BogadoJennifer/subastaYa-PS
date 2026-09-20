import { Table, Badge } from 'react-bootstrap'

function WalletTransactions({ transactions }) {

    const descriptions = {
        DEPOSIT: 'Depósito',
        HOLD: 'Oferta retenida',
        RELEASE: 'Oferta liberada',
        PAYMENT: 'Pago de subasta',
        CHARGE: 'Cobro de venta'
    }

    const variants = {
        DEPOSIT: 'success',
        HOLD: 'warning',
        RELEASE: 'info',
        PAYMENT: 'danger',
        CHARGE: 'primary'
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

    return (
        <>
            {transactions.length === 0 ? (
                <p className="text-muted mb-0">
                    Todavía no hay movimientos.
                </p>
            ) : (
                <Table responsive hover>
                    <thead>
                    <tr>
                        <th>Movimiento</th>
                        <th>Monto</th>
                        <th>Fecha</th>
                    </tr>
                    </thead>

                    <tbody>
                    {transactions.map((transaction) => (
                        <tr key={transaction.id}>
                            <td>
                                <Badge
                                    bg={variants[transaction.type] || 'secondary'}
                                >
                                    {descriptions[transaction.type] ||
                                        transaction.type}
                                </Badge>
                            </td>

                            <td>
                                {formatMoney(transaction.amount)}
                            </td>

                            <td>
                                {formatDate(transaction.date)}
                            </td>
                        </tr>
                    ))}
                    </tbody>
                </Table>
            )}
        </>
    )
}

export default WalletTransactions