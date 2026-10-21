const ACCESS_TOKEN_KEY = "fastserve.accessToken";
const REFRESH_TOKEN_KEY = "fastserve.refreshToken";
const USER_NAME_KEY = "fastserve.userName";

export class ApiError extends Error {
    constructor(message, status) {
        super(message);
        this.name = "ApiError";
        this.status = status;
    }
}

function readToken(key) {
    return window.sessionStorage.getItem(key);
}

export function saveSession({ accessToken, refreshToken, name }) {
    window.sessionStorage.setItem(ACCESS_TOKEN_KEY, accessToken);
    window.sessionStorage.setItem(REFRESH_TOKEN_KEY, refreshToken);
    if (name) window.sessionStorage.setItem(USER_NAME_KEY, name);
}

export function clearSession() {
    window.sessionStorage.removeItem(ACCESS_TOKEN_KEY);
    window.sessionStorage.removeItem(REFRESH_TOKEN_KEY);
    window.sessionStorage.removeItem(USER_NAME_KEY);
}

export function hasSession() {
    return Boolean(readToken(ACCESS_TOKEN_KEY));
}

export function getSavedUser() {
    const name = readToken(USER_NAME_KEY);
    return name ? { name } : null;
}

async function parseResponse(response) {
    const text = await response.text();
    if (!text) return null;

    try {
        return JSON.parse(text);
    } catch {
        return text;
    }
}

function errorMessage(body, status) {
    if (body && typeof body === "object") {
        if (body.message) return body.message;
        if (body.error) return body.error;
    }
    return typeof body === "string" && body ? body : `Request failed (${status})`;
}

async function refreshSession() {
    const refreshToken = readToken(REFRESH_TOKEN_KEY);
    if (!refreshToken) return false;

    const response = await fetch("/users/refresh", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ refreshToken })
    });
    if (!response.ok) {
        clearSession();
        return false;
    }

    const tokens = await parseResponse(response);
    saveSession(tokens);
    return true;
}

async function request(path, options = {}, retry = true) {
    const headers = new Headers(options.headers || {});
    const accessToken = readToken(ACCESS_TOKEN_KEY);
    if (accessToken) headers.set("Authorization", `Bearer ${accessToken}`);
    if (options.body) headers.set("Content-Type", "application/json");

    const response = await fetch(path, { ...options, headers });
    if (response.status === 401 && retry && !path.startsWith("/users/")) {
        if (await refreshSession()) return request(path, options, false);
        clearSession();
        throw new ApiError("Your session expired. Please sign in again.", 401);
    }

    const body = await parseResponse(response);
    if (!response.ok) {
        throw new ApiError(errorMessage(body, response.status), response.status);
    }
    return body;
}

export const api = {
    signUp(data) {
        return request("/users/signup", {
            method: "POST",
            body: JSON.stringify(data)
        });
    },
    login(email, password) {
        const query = new URLSearchParams({ email, password });
        return request(`/users/login?${query}`, { method: "POST" });
    },
    logout() {
        const refreshToken = readToken(REFRESH_TOKEN_KEY);
        if (!refreshToken) return Promise.resolve();
        return request("/users/logout", {
            method: "POST",
            body: JSON.stringify({ refreshToken })
        });
    },
    restaurants(page = 0) {
        return request(`/restaurants?page=${page}&size=12`);
    },
    restaurantMenu(id) {
        return request(`/restaurants/${encodeURIComponent(id)}/menu`);
    },
    createOrder(paymentMode, items) {
        return request("/orders", {
            method: "POST",
            body: JSON.stringify({ paymentMode, items })
        });
    },
    myOrders(page = 0) {
        return request(`/orders/my?page=${page}&size=20`);
    },
    cancelOrder(id) {
        return request(`/orders/${encodeURIComponent(id)}/cancel`, {
            method: "PATCH"
        });
    }
};
