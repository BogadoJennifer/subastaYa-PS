function getAuthSession() {
    try {
        return JSON.parse(sessionStorage.getItem('subastaYaAuth') || 'null')
    } catch {
        return null
    }
}

export function apiFetch(url, options = {}) {
    const headers = new Headers(options.headers || {})
    const token = getAuthSession()?.token

    if (token) {
        headers.set('Authorization', `Bearer ${token}`)
    }

    return fetch(url, {
        ...options,
        headers,
    })
}

export function getCurrentUser() {
    return getAuthSession()
}