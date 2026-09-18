import { useState } from 'react'

const priceFormatter = new Intl.NumberFormat('es-AR', {
    style: 'currency',
    currency: 'ARS',
})

export function BidConsole({
                               currentPrice,
                               minIncrement,
                               onBid,
                               disabled,
                               isSubmitting,
                           }) {
    const [customAmount, setCustomAmount] = useState('')
    const [validationError, setValidationError] = useState('')

    const nextMinimumBid = Number(
        (Number(currentPrice) + Number(minIncrement)).toFixed(2)
    )

    const isDisabled = disabled || isSubmitting

    async function handleCustomSubmit(event) {
        event.preventDefault()

        if (isDisabled) return

        const normalizedAmount = customAmount.trim()

        if (!/^\d+(?:\.\d{1,2})?$/.test(normalizedAmount)) {
            setValidationError(
                'Ingresá un monto positivo con un máximo de dos decimales.'
            )
            return
        }

        const amount = Number(normalizedAmount)

        if (!Number.isFinite(amount) || amount < nextMinimumBid) {
            setValidationError(
                `La oferta debe ser de al menos ${
                    priceFormatter.format(nextMinimumBid)
                }.`
            )
            return
        }

        setValidationError('')

        const wasAccepted = await onBid(amount)

        if (wasAccepted) {
            setCustomAmount('')
        }
    }

    return (
        <section className="border rounded p-3 mt-3">
            <h3 className="h5">Realizar una oferta</h3>

            <button
                type="button"
                className="btn btn-success w-100 mb-3"
                disabled={isDisabled}
                onClick={() => {
                    setValidationError('')
                    void onBid(nextMinimumBid)
                }}
            >
                {isSubmitting
                    ? 'Enviando oferta...'
                    : `Ofertar ${priceFormatter.format(nextMinimumBid)}`}
            </button>

            <form onSubmit={handleCustomSubmit}>
                <label htmlFor="custom-bid-amount" className="form-label">
                    Otro importe
                </label>

                <div className="input-group">
                    <span className="input-group-text">$</span>

                    <input
                        id="custom-bid-amount"
                        type="number"
                        min={nextMinimumBid}
                        step="0.01"
                        required
                        value={customAmount}
                        disabled={isDisabled}
                        className={`form-control ${
                            validationError ? 'is-invalid' : ''
                        }`}
                        aria-describedby="custom-bid-help custom-bid-error"
                        aria-invalid={Boolean(validationError)}
                        onChange={(event) => {
                            setCustomAmount(event.target.value)
                            setValidationError('')
                        }}
                    />

                    <button
                        type="submit"
                        className="btn btn-outline-success"
                        disabled={isDisabled || !customAmount.trim()}
                    >
                        Ofertar
                    </button>
                </div>

                <div id="custom-bid-help" className="form-text">
                    Mínimo actual: {priceFormatter.format(nextMinimumBid)}.
                    Podés ingresar un importe mayor.
                </div>

                {validationError && (
                    <div
                        id="custom-bid-error"
                        className="text-danger small mt-2"
                        role="alert"
                    >
                        {validationError}
                    </div>
                )}
            </form>
        </section>
    )
}