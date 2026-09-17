import React, { useState } from 'react';

export const BidConsole = ({ currentPrice, minIncrement, onBid, disabled }) => {
    const [customAmount, setCustomAmount] = useState('');
    const nextMinBid = Number(currentPrice) + Number(minIncrement);

    const handleSubmitCustom = (e) => {
        e.preventDefault();
        const parsedAmount = parseFloat(customAmount);
        if (!parsedAmount || parsedAmount < nextMinBid) {
            alert(`El monto debe ser igual o mayor al mínimo sugerido ($${nextMinBid})`);
            return;
        }
        onBid(parsedAmount);
        setCustomAmount('');
    };

    return (
        <div style={{ marginTop: '20px', padding: '15px', border: '1px solid #e2e8f0', borderRadius: '8px' }}>
            <h3 style={{ margin: '0 0 10px 0', fontSize: '18px' }}>Consola de Oferta</h3>

            {/* Botón de Puja Rápida Sugerida */}
            <button
                type="button"
                disabled={disabled}
                onClick={() => onBid(nextMinBid)}
                style={{
                    width: '100%',
                    padding: '12px',
                    backgroundColor: disabled ? '#cbd5e1' : '#2563eb',
                    color: '#fff',
                    border: 'none',
                    borderRadius: '6px',
                    fontWeight: 'bold',
                    cursor: disabled ? 'not-allowed' : 'pointer',
                    marginBottom: '12px',
                }}
            >
                Pujar Mínimo Sugerido: ${nextMinBid.toLocaleString()}
            </button>

            {/* Formulario para Puja Personalizada */}
            <form onSubmit={handleSubmitCustom} style={{ display: 'flex', gap: '8px' }}>
                <input
                    type="number"
                    step="0.01"
                    placeholder={`Monto mayor a $${nextMinBid}`}
                    value={customAmount}
                    disabled={disabled}
                    onChange={(e) => setCustomAmount(e.target.value)}
                    style={{
                        flex: 1,
                        padding: '10px',
                        border: '1px solid #cbd5e1',
                        borderRadius: '6px',
                    }}
                />
                <button
                    type="submit"
                    disabled={disabled || !customAmount}
                    style={{
                        padding: '10px 18px',
                        backgroundColor: disabled || !customAmount ? '#cbd5e1' : '#0f172a',
                        color: '#fff',
                        border: 'none',
                        borderRadius: '6px',
                        cursor: disabled || !customAmount ? 'not-allowed' : 'pointer',
                    }}
                >
                    Ofertar
                </button>
            </form>
        </div>
    );
};