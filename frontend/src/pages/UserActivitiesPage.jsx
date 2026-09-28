import React, { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { Container, Row, Col, Card, Badge, Button, Collapse } from 'react-bootstrap';

function UserActivitiesPage() {
    const { userId } = useParams();

    const [activities, setActivities] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    const [showPublications, setShowPublications] = useState(false);
    const [showParticipations, setShowParticipations] = useState(false);

    const navigate = useNavigate();

    function formatDate(dateString) {
        const date = new Date(dateString);

        if (Number.isNaN(date.getTime())) {
            return dateString;
        }

        return new Intl.DateTimeFormat('es-AR', {
            day: '2-digit',
            month: '2-digit',
            year: 'numeric',
            hour: '2-digit',
            minute: '2-digit',
            hour12: false
        }).format(date) + ' hs';
    }

    function getStateLabel(state) {
        switch (state) {
            case 'SCHEDULED':
                return 'Programada';
            case 'ACTIVE':
                return 'Abierta';
            case 'FINISHED':
                return 'Finalizada';
            case 'UNSOLD':
                return 'Sin adjudicar';
            default:
                return state;
        }
    }

    function getParticipationLabel(participation) {
        if (participation.open) {
            return 'Abierta';
        }

        if (participation.won) {
            return 'Ganaste';
        }

        if (participation.state === 'SCHEDULED') {
            return 'Programada';
        }

        if (participation.state === 'FINISHED') {
            return 'No ganaste';
        }

        if (participation.state === 'UNSOLD') {
            return 'Sin adjudicar';
        }

        return '';
    }

    function getStateVariant(state) {
        switch (state) {
            case 'ACTIVE':
                return 'success';
            case 'SCHEDULED':
                return 'warning';
            case 'FINISHED':
                return 'secondary';
            case 'UNSOLD':
                return 'dark';
            default:
                return 'secondary';
        }
    }

    function getParticipationVariant(participation) {
        if (participation.open) {
            return 'success';
        }

        if (participation.won) {
            return 'primary';
        }

        if (participation.state === 'SCHEDULED') {
            return 'warning';
        }

        if (participation.state === 'FINISHED') {
            return 'secondary';
        }

        if (participation.state === 'UNSOLD') {
            return 'dark';
        }

        return 'secondary';
    }

    useEffect(() => {
        fetch(`http://localhost:8080/api/users/${userId}/activities`)
    .then(response => {
    if (!response.ok) {
        throw new Error('No se pudieron cargar las actividades');
    }

    return response.json();
})
    .then(data => {
        setActivities(data);
        setLoading(false);
    })
    .catch(error => {
        setError(error.message);
        setLoading(false);
    });
}, [userId]);

if (loading) {
    return (
        <Container className="py-5">
            <div className="text-center">
                <p className="text-muted">
                    Cargando actividades...
                </p>
            </div>
        </Container>
    );
}

if (error) {
    return (
        <Container className="py-5">
            <Card className="border-danger">
                <Card.Body className="text-center">
                    <h5 className="text-danger">
                        No se pudieron cargar las actividades
                    </h5>

                    <p className="text-muted mb-0">
                        {error}
                    </p>
                </Card.Body>
            </Card>
        </Container>
    );
}

return (
    <Container className="py-4">

        <div className="mb-4">
            <h1 className="fw-bold mb-1">
                Mis Actividades
            </h1>

            <p className="text-muted mb-0">
                Consultá tus publicaciones y las subastas en las que participaste.
            </p>
        </div>

        <Card className="shadow-sm border-0 mb-4">
            <Card.Body>
                <Row className="align-items-center">

                    <Col md={8}>
                        <h5 className="fw-bold mb-1">
                            Resumen de actividad
                        </h5>

                        <p className="text-muted mb-0">
                            Usuario #{activities.userId}
                        </p>
                    </Col>

                    <Col md={4} className="text-md-end mt-3 mt-md-0">
                        <small className="text-muted d-block">
                            Ingresos totales
                        </small>

                        <h3 className="fw-bold mb-0">
                            ${activities.totalRevenue}
                        </h3>
                    </Col>

                </Row>
            </Card.Body>
        </Card>

        {/* MIS PUBLICACIONES */}

        <div className="mb-5">

            <div className="d-flex justify-content-between align-items-center mb-3">
                <div>
                    <h2 className="h4 fw-bold mb-1">
                        Mis Publicaciones
                    </h2>

                    <p className="text-muted mb-0">
                        Subastas que publicaste.
                    </p>
                </div>

                <div className="d-flex align-items-center gap-2">
                    <Badge bg="primary" pill>
                        {activities.publications.length}
                    </Badge>

                    <Button
                        variant="outline-primary"
                        size="sm"
                        onClick={() => setShowPublications(!showPublications)}
                        aria-expanded={showPublications}
                    >
                        {showPublications
                            ? 'Ocultar publicaciones'
                            : 'Mostrar publicaciones'}
                    </Button>
                </div>
            </div>

            <Collapse in={showPublications}>
                <div>

                    {activities.publications.length === 0 ? (
                        <Card className="border-0 shadow-sm">
                            <Card.Body className="text-center py-4">
                                <p className="text-muted mb-0">
                                    No tenés publicaciones.
                                </p>
                            </Card.Body>
                        </Card>
                    ) : (
                        <Row>
                            {activities.publications.map(publication => (
                                <Col
                                    key={publication.auctionId}
                                    xs={12}
                                    md={6}
                                    lg={4}
                                    className="mb-4"
                                >
                                    <Card className="h-100 shadow-sm border-4">
                                        <Card.Body>

                                            <div className="d-flex justify-content-between align-items-start mb-3">

                                                <h5
                                                    className="fw-bold mb-0"
                                                    role="button"
                                                    onClick={() =>
                                                        navigate(`/auctions/${publication.auctionId}/live?demoUserId=${activities.userId}`)
                                                    }
                                                >
                                                    {publication.title}
                                                </h5>

                                                <Badge
                                                    bg={getStateVariant(publication.state)}
                                                >
                                                    {getStateLabel(publication.state)}
                                                </Badge>

                                            </div>

                                            <p className="text-muted small mb-1">
                                                Cierre
                                            </p>

                                            <p className="mb-3">
                                                {formatDate(publication.endDate)}
                                            </p>

                                            <div className="mb-3">

                                                <p className="text-muted small mb-1">
                                                    Adjudicación
                                                </p>

                                                <Badge
                                                    bg={
                                                        publication.awarded
                                                            ? 'success'
                                                            : 'secondary'
                                                    }
                                                >
                                                    {publication.awarded
                                                        ? 'Adjudicada'
                                                        : 'Sin adjudicar'}
                                                </Badge>

                                            </div>

                                            <div className="border-top pt-3">

                                                <p className="text-muted small mb-1">
                                                    Ingreso
                                                </p>

                                                <h5 className="fw-bold mb-0">
                                                    ${publication.revenue}
                                                </h5>

                                            </div>

                                        </Card.Body>

                                    </Card>
                                </Col>
                            ))}
                        </Row>
                    )}

                </div>
            </Collapse>
        </div>

        {/* MIS COMPRAS / PUJAS */}

        <div className="mb-5">

            <div className="d-flex justify-content-between align-items-center mb-3">
                <div>
                    <h2 className="h4 fw-bold mb-1">
                        Mis Compras / Pujas
                    </h2>

                    <p className="text-muted mb-0">
                        Subastas en las que participaste.
                    </p>
                </div>

                <div className="d-flex align-items-center gap-2">
                    <Badge bg="primary" pill>
                        {activities.participations.length}
                    </Badge>

                    <Button
                        variant="outline-primary"
                        size="sm"
                        onClick={() => setShowParticipations(!showParticipations)}
                        aria-expanded={showParticipations}
                    >
                        {showParticipations
                            ? 'Ocultar pujas'
                            : 'Mostrar pujas'}
                    </Button>
                </div>
            </div>

            <Collapse in={showParticipations}>
                <div>

                    {activities.participations.length === 0 ? (
                        <Card className="border-0 shadow-sm">
                            <Card.Body className="text-center py-4">
                                <p className="text-muted mb-0">
                                    No participaste en ninguna subasta.
                                </p>
                            </Card.Body>
                        </Card>
                    ) : (
                        <Row>
                            {activities.participations.map(participation => (
                                <Col
                                    key={participation.auctionId}
                                    xs={12}
                                    md={6}
                                    lg={4}
                                    className="mb-4"
                                >
                                    <Card className="h-100 shadow-sm border-4">

                                        <Card.Body>

                                            <div className="d-flex justify-content-between align-items-start mb-3">

                                                <h5 className="fw-bold mb-0">
                                                    {participation.title}
                                                </h5>

                                                <Badge
                                                    bg={getStateVariant(participation.state)}
                                                >
                                                    {getStateLabel(participation.state)}
                                                </Badge>

                                            </div>

                                            <p className="text-muted small mb-1">
                                                Cierre
                                            </p>

                                            <p className="mb-3">
                                                {formatDate(participation.endDate)}
                                            </p>

                                            <div className="border-top pt-3">

                                                <p className="text-muted small mb-1">
                                                    Resultado
                                                </p>

                                                <Badge
                                                    bg={getParticipationVariant(participation)}
                                                >
                                                    {getParticipationLabel(participation)}
                                                </Badge>

                                            </div>

                                        </Card.Body>

                                    </Card>
                                </Col>
                            ))}
                        </Row>
                    )}

                </div>
            </Collapse>
        </div>

    </Container>
);
}

export default UserActivitiesPage;