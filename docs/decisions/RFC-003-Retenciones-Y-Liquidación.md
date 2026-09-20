# RFC-003 — Retenciones y liquidación

- **Estado:** Aceptado
- **Fecha:** 10/09/2026
- **Ámbito:** Gestión de fondos durante las ofertas y finalización de subastas.

##  Problema

Una oferta válida puede requerir que parte de los fondos disponibles del oferente quede temporalmente bloqueada.

El sistema debe distinguir entre:

- dinero total;
- dinero retenido;
- dinero disponible.

También debe registrar los movimientos financieros que producen las ofertas y la liquidación final.

##  Decisión

Las retenciones se reflejarán en `Wallet.retainedBalance`.

La regla es:

```text
availableBalance = totalBalance - retainedBalance
```

Una retención genera un movimiento de ledger de tipo:

```text
HOLD
```

Cuando una oferta deja de ser la oferta líder, su retención debe liberarse mediante:

```text
RELEASE
```

Cuando la subasta termina y el usuario resulta ganador, el monto correspondiente se liquida mediante:

```text
PAYMENT
```

Los movimientos relacionados con una subasta deben guardar su `auctionId`.

##  Ejemplo

Si una billetera tiene:

```text
totalBalance    = 50.000
retainedBalance = 0
available       = 50.000
```

Y realiza una oferta de 10.000:

```text
totalBalance    = 50.000
retainedBalance = 10.000
available       = 40.000
```

Se registra:

```text
type      = HOLD
amount    = 10.000
auctionId = <subasta>
```

Si otro usuario supera la oferta, se libera la retención:

```text
retainedBalance = 0
available       = 50.000
```

Y se registra:

```text
type = RELEASE
```

##  Principio de consistencia

Una operación de retención, liberación o liquidación debe modificar el estado de la billetera y registrar su movimiento dentro de la misma transacción.

##  Alternativas consideradas

### Retener dinero solamente en la oferta

No se adopta porque el estado financiero de la billetera debe ser consultable independientemente de una oferta individual.

### Registrar movimientos sin modificar la billetera

No se adopta porque el historial no reemplaza al estado actual de los fondos.

##  Consecuencias

- permite conocer el saldo disponible en todo momento;
- permite reconstruir el historial de movimientos;
- facilita la trazabilidad de operaciones de una subasta;
- exige controlar cuidadosamente operaciones repetidas y concurrencia.
