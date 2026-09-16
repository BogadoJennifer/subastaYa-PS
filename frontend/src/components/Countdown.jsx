import { useEffect, useState } from 'react'

function formatTime(milliseconds) {
    const totalSeconds = Math.max(
        0,
        Math.ceil(milliseconds / 1000)
    )

    const days = Math.floor(totalSeconds / 86400)
    const hours = Math.floor((totalSeconds % 86400) / 3600)
    const minutes = Math.floor((totalSeconds % 3600) / 60)
    const seconds = totalSeconds % 60

    const clock = [hours, minutes, seconds]
        .map((value) => String(value).padStart(2, '0'))
        .join(':')

    return days > 0 ? `${days} d ${clock}` : clock
}

function Countdown({ state, startDate, endDate }) {
    const [currentTime, setCurrentTime] = useState(() => Date.now())

    useEffect(() => {
        if (state !== 'ACTIVE' && state !== 'SCHEDULED') {
            return
        }

        const intervalId = setInterval(() => {
            setCurrentTime(Date.now())
        }, 1000)

        return () => clearInterval(intervalId)
    }, [state])

    if (state === 'FINISHED') {
        return <div className="text-secondary">Subasta finalizada</div>
    }

    if (state === 'UNSOLD') {
        return <div className="text-secondary">Finalizada sin ofertas</div>
    }

    if (state !== 'ACTIVE' && state !== 'SCHEDULED') {
        return <div className="text-secondary">Estado no disponible</div>
    }

    const isUpcoming = state === 'SCHEDULED'
    const targetDate = isUpcoming ? startDate : endDate
    const targetTimestamp = targetDate
        ? new Date(targetDate).getTime()
        : NaN

    if (!Number.isFinite(targetTimestamp)) {
        return <div className="text-secondary">Fecha no disponible</div>
    }

    const remainingTime = targetTimestamp - currentTime

    if (remainingTime <= 0) {
        return (
            <div className="text-secondary">
                {isUpcoming
                    ? 'Inicio pendiente de confirmación'
                    : 'Tiempo agotado · cierre pendiente'}
            </div>
        )
    }

    const isEndingSoon = !isUpcoming && remainingTime <= 60000

    return (
        <div>
            <div className="small text-secondary">
                {isUpcoming ? 'Comienza en' : 'Termina en'}
            </div>

            <div
                className={`fw-bold fs-5 ${
                    isEndingSoon ? 'text-danger' : 'text-dark'
                }`}
                style={{ fontVariantNumeric: 'tabular-nums' }}
            >
                {formatTime(remainingTime)}
            </div>

            {isEndingSoon && (
                <div className="small text-danger">¡Último minuto!</div>
            )}
        </div>
    )
}

export default Countdown