export const INITIAL_FILTERS = {
    searchTerm: '',
    selectedState: 'ALL',
    selectedCategory: 'ALL',
    minimumPrice: '',
    maximumPrice: '',
    sortOrder: 'DEFAULT',
}

export function getCategories(auctions) {
    return Array.from(
        new Map(
            auctions
                .filter((auction) => auction.categoryId != null)
                .map((auction) => [
                    String(auction.categoryId),
                    {
                        id: String(auction.categoryId),
                        name: auction.categoryName ?? 'Sin nombre',
                    },
                ])
        ).values()
    ).sort((first, second) =>
        first.name.localeCompare(second.name, 'es')
    )
}

function parsePrice(value) {
    return value === '' ? null : Number(value)
}

export function hasInvalidPriceRange(filters) {
    const minimum = parsePrice(filters.minimumPrice)
    const maximum = parsePrice(filters.maximumPrice)

    return (
        (minimum !== null &&
            (!Number.isFinite(minimum) || minimum < 0)) ||
        (maximum !== null &&
            (!Number.isFinite(maximum) || maximum < 0)) ||
        (minimum !== null && maximum !== null && minimum > maximum)
    )
}

export function filterAuctions(auctions, filters) {
    if (hasInvalidPriceRange(filters)) {
        return []
    }

    const search = filters.searchTerm.trim().toLocaleLowerCase('es')
    const minimum = parsePrice(filters.minimumPrice)
    const maximum = parsePrice(filters.maximumPrice)

    return auctions.filter((auction) => {
        const searchableText = [
            auction.title,
            auction.description,
        ]
            .filter(Boolean)
            .join(' ')
            .toLocaleLowerCase('es')

        const matchesSearch = searchableText.includes(search)

        const matchesState =
            filters.selectedState === 'ALL' ||
            (filters.selectedState === 'FINISHED'
                ? ['FINISHED', 'UNSOLD'].includes(auction.state)
                : auction.state === filters.selectedState)

        const matchesCategory =
            filters.selectedCategory === 'ALL' ||
            String(auction.categoryId) === filters.selectedCategory

        const currentPrice = Number(
            auction.highestBid ?? auction.basePrice
        )

        const matchesPrice =
            (minimum === null || currentPrice >= minimum) &&
            (maximum === null || currentPrice <= maximum)

        return matchesSearch && matchesState && matchesCategory && matchesPrice
    })
}

function getTimeGroup(auction) {
    if (auction.state === 'ACTIVE') return 0
    if (auction.state === 'SCHEDULED') return 1
    return 2
}

function getTargetTimestamp(auction) {
    const targetDate =
        auction.state === 'SCHEDULED'
            ? auction.startDate
            : auction.endDate

    const timestamp = targetDate
        ? new Date(targetDate).getTime()
        : NaN

    return Number.isFinite(timestamp)
        ? timestamp
        : Number.MAX_SAFE_INTEGER
}

export function sortAuctions(auctions, sortOrder) {
    return [...auctions].sort((first, second) => {
        if (sortOrder === 'HIGHEST_BID') {
            const firstHasBids = first.highestBid != null
            const secondHasBids = second.highestBid != null

            if (firstHasBids !== secondHasBids) {
                return firstHasBids ? -1 : 1
            }

            if (firstHasBids && secondHasBids) {
                const difference =
                    Number(second.highestBid) - Number(first.highestBid)

                if (difference !== 0) {
                    return difference
                }
            }
        }

        if (sortOrder === 'ENDING_SOON') {
            const groupDifference =
                getTimeGroup(first) - getTimeGroup(second)

            if (groupDifference !== 0) {
                return groupDifference
            }

            if (getTimeGroup(first) !== 2) {
                const timeDifference =
                    getTargetTimestamp(first) - getTargetTimestamp(second)

                if (timeDifference !== 0) {
                    return timeDifference
                }
            }
        }

        return first.id - second.id
    })
}