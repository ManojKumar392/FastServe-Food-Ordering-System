import { api, clearSession, getAccessToken, getSavedUser, hasSession, saveSession } from "./api.js";
import { LiveOrderSubscription } from "./live-orders.js";

const app = document.querySelector("#app");
const sessionActions = document.querySelector("#session-actions");
const toast = document.querySelector("#toast");

const state = {
    user: getSavedUser(),
    restaurants: [],
    selectedRestaurant: null,
    menu: [],
    cart: new Map(),
    view: "restaurants",
    authMode: "login",
    liveConnected: false
};

const liveOrders = new LiveOrderSubscription({
    getToken: getAccessToken,
    onUpdate: handleOrderUpdate,
    onConnectionChange(connected) {
        state.liveConnected = connected;
        const indicator = document.querySelector("#live-status");
        if (indicator) {
            indicator.classList.toggle("live-connected", connected);
            indicator.innerHTML = `<span></span>${connected ? "Live updates on" : "Connecting to live updates..."}`;
        }
    }
});

document.querySelector("#year").textContent = new Date().getFullYear();
render();

function escapeHtml(value) {
    return String(value ?? "").replace(/[&<>"']/g, (character) => ({
        "&": "&amp;",
        "<": "&lt;",
        ">": "&gt;",
        '"': "&quot;",
        "'": "&#39;"
    })[character]);
}

function showToast(message, isError = false) {
    toast.textContent = message;
    toast.classList.toggle("toast-error", isError);
    toast.classList.add("toast-visible");
    window.clearTimeout(showToast.timeout);
    showToast.timeout = window.setTimeout(() => toast.classList.remove("toast-visible"), 3600);
}

function render() {
    if (!hasSession()) {
        state.user = null;
        renderAuth();
        return;
    }

    sessionActions.innerHTML = `
        <span class="welcome">Hi, ${escapeHtml(state.user?.name || "there")}</span>
        <button class="button button-quiet" type="button" data-action="logout">Sign out</button>`;
    renderDashboard();
}

function renderAuth() {
    sessionActions.replaceChildren();
    const isLogin = state.authMode === "login";
    app.innerHTML = `
        <section class="hero auth-hero">
            <div class="hero-copy">
                <p class="eyebrow">YOUR NEXT GOOD MEAL</p>
                <h1>Fresh favorites,<br><span>right around the corner.</span></h1>
                <p class="hero-description">Find something delicious from restaurants in your neighborhood.</p>
            </div>
            <div class="auth-card">
                <div class="auth-tabs" role="tablist" aria-label="Account">
                    <button class="${isLogin ? "active" : ""}" type="button" data-action="auth-mode" data-mode="login">Sign in</button>
                    <button class="${!isLogin ? "active" : ""}" type="button" data-action="auth-mode" data-mode="signup">Create account</button>
                </div>
                <form id="auth-form" class="form-stack">
                    ${isLogin ? "" : `
                        <label>Your name<input name="name" autocomplete="name" required></label>
                        <label>Delivery address<input name="address" autocomplete="street-address" required></label>`}
                    <label>Email address<input name="email" type="email" autocomplete="email" required></label>
                    <label>Password<input name="password" type="password" minlength="6" autocomplete="${isLogin ? "current-password" : "new-password"}" required></label>
                    <button class="button button-primary button-full" type="submit">${isLogin ? "Sign in" : "Create account"}</button>
                </form>
                <p class="form-note">${isLogin ? "New to FastServe? Create an account to get started." : "Already have an account? Sign in instead."}</p>
            </div>
        </section>
        <section class="feature-row" aria-label="FastServe highlights">
            <div><span class="feature-icon">01</span><span><strong>Discover nearby</strong><small>Restaurants worth knowing</small></span></div>
            <div><span class="feature-icon">02</span><span><strong>Order your way</strong><small>Pick your favorites and go</small></span></div>
            <div><span class="feature-icon">03</span><span><strong>Stay in the loop</strong><small>Keep an eye on your orders</small></span></div>
        </section>`;
}

function renderDashboard() {
    app.innerHTML = `
        <section class="dashboard-heading">
            <div>
                <p class="eyebrow">MADE FOR YOUR NEIGHBORHOOD</p>
                <h1>What sounds good?</h1>
                <p class="muted">Browse local spots and find your next favorite.</p>
            </div>
            <div class="dashboard-tabs" role="tablist" aria-label="FastServe">
                <button class="${state.view === "restaurants" ? "active" : ""}" data-action="view" data-view="restaurants" type="button">Restaurants</button>
                <button class="${state.view === "orders" ? "active" : ""}" data-action="view" data-view="orders" type="button">Your orders</button>
            </div>
        </section>
        ${state.view === "restaurants" ? renderRestaurants() : renderOrders()}`;
}

function renderRestaurants() {
    const restaurants = state.restaurants.length
        ? state.restaurants.map((restaurant, index) => `
            <article class="restaurant-card">
                <div class="restaurant-art art-${index % 4}" aria-hidden="true">
                    <span>${escapeHtml(restaurant.name?.slice(0, 1) || "F")}</span>
                    <span class="art-sparkle">✳</span>
                </div>
                <div class="restaurant-info">
                    <div><h2>${escapeHtml(restaurant.name)}</h2><p>${escapeHtml(restaurant.location)}</p></div>
                    <button class="button button-outline" type="button" data-action="menu" data-id="${escapeHtml(restaurant.id)}">View menu <span aria-hidden="true">→</span></button>
                </div>
            </article>`).join("")
        : `<p class="empty-state">No restaurants to show just yet. Check back soon.</p>`;

    const selected = state.selectedRestaurant
        ? renderMenu(state.selectedRestaurant)
        : `<aside class="side-note"><span class="side-note-mark">✳</span><h2>A good meal is closer than you think.</h2><p>Choose a restaurant to explore what's cooking.</p></aside>`;

    return `
        <div class="food-layout">
            <section class="restaurant-section" aria-label="Restaurants">
                <div class="section-heading"><div><h2>Local favorites</h2><p>Pick a spot to see its menu</p></div></div>
                <div class="restaurant-grid">${restaurants}</div>
            </section>
            <section id="menu-panel" class="menu-panel" aria-label="Menu and basket">${selected}</section>
        </div>`;
}

function renderMenu(restaurant) {
    const items = state.menu.length
        ? state.menu.map((item) => {
            const quantity = state.cart.get(item.id)?.quantity || 0;
            return `
                <article class="menu-item">
                    <div><h3>${escapeHtml(item.name)}</h3><p>${formatPrice(item.price)}</p></div>
                    <button class="add-button" type="button" data-action="add" data-id="${escapeHtml(item.id)}" aria-label="Add ${escapeHtml(item.name)} to basket">${quantity ? `+${quantity}` : "+"}</button>
                </article>`;
        }).join("")
        : `<p class="empty-state compact">No menu items available right now.</p>`;

    return `
        <div class="menu-heading">
            <div><p class="eyebrow">THE MENU</p><h2>${escapeHtml(restaurant.name)}</h2><p>${escapeHtml(restaurant.location)}</p></div>
            <button class="icon-button" type="button" data-action="close-menu" aria-label="Close menu">×</button>
        </div>
        <div class="menu-items">${items}</div>
        ${renderCart()}`;
}

function renderCart() {
    const entries = [...state.cart.values()];
    if (!entries.length) {
        return `<div class="cart-empty"><span aria-hidden="true">＋</span><p>Add an item to start your order.</p></div>`;
    }
    const total = entries.reduce((sum, entry) => sum + entry.item.price * entry.quantity, 0);
    const lines = entries.map(({ item, quantity }) => `
        <div class="cart-line">
            <span>${escapeHtml(item.name)} <small>× ${quantity}</small></span>
            <span>${formatPrice(item.price * quantity)}</span>
            <button class="remove-button" type="button" data-action="remove" data-id="${escapeHtml(item.id)}" aria-label="Remove one ${escapeHtml(item.name)}">−</button>
        </div>`).join("");

    return `
        <div class="cart">
            <div class="cart-title"><h3>Your basket</h3><span>${entries.reduce((sum, entry) => sum + entry.quantity, 0)} items</span></div>
            ${lines}
            <div class="cart-total"><span>Subtotal</span><strong>${formatPrice(total)}</strong></div>
            <label class="payment-select">Payment
                <select id="payment-mode">
                    <option value="CARD">Card</option><option value="UPI">UPI</option><option value="CASH">Cash on delivery</option>
                </select>
            </label>
            <button class="button button-primary button-full" type="button" data-action="checkout">Place order <span aria-hidden="true">→</span></button>
        </div>`;
}

function renderOrders() {
    return `<section class="orders-section">
        <div class="section-heading"><div><h2>Your recent orders</h2><p>Everything you've ordered, all in one place</p>
            <span id="live-status" class="live-indicator ${state.liveConnected ? "live-connected" : ""}"><span></span>${state.liveConnected ? "Live updates on" : "Connecting to live updates..."}</span></div>
            <button class="button button-outline" type="button" data-action="refresh-orders">Refresh</button>
        </div>
        <div id="orders-list" class="orders-list"><p class="loading">Loading your orders...</p></div>
    </section>`;
}

function formatPrice(amount) {
    return new Intl.NumberFormat(undefined, { style: "currency", currency: "INR" }).format(Number(amount) || 0);
}

async function loadRestaurants() {
    try {
        const page = await api.restaurants();
        state.restaurants = page.content || [];
        if (state.selectedRestaurant) {
            state.selectedRestaurant = state.restaurants.find((restaurant) => restaurant.id === state.selectedRestaurant.id) || null;
        }
        render();
    } catch (error) {
        showToast(error.message, true);
    }
}

async function loadMenu(id) {
    const restaurant = state.restaurants.find((item) => String(item.id) === String(id));
    if (!restaurant) return;
    try {
        const menu = await api.restaurantMenu(id);
        if (state.selectedRestaurant?.id !== restaurant.id) state.cart.clear();
        state.selectedRestaurant = restaurant;
        state.menu = menu || [];
        render();
        document.querySelector("#menu-panel")?.scrollIntoView({ behavior: "smooth", block: "start" });
    } catch (error) {
        showToast(error.message, true);
    }
}

async function loadOrders() {
    const list = document.querySelector("#orders-list");
    if (!list) return;
    list.innerHTML = `<p class="loading">Loading your orders...</p>`;
    try {
        const page = await api.myOrders();
        const orders = page.content || [];
        list.innerHTML = orders.length ? orders.map((order) => {
            const canCancel = !["CANCELLED", "DELIVERED"].includes(order.status);
            const date = order.createdAt ? new Date(order.createdAt).toLocaleString() : "Just now";
            return `
                <article class="order-card">
                    <div class="order-icon" aria-hidden="true">#${escapeHtml(order.id)}</div>
                    <div class="order-detail"><div class="order-title"><h3>Order ${escapeHtml(order.id)}</h3><span class="status-pill status-${escapeHtml(String(order.status || "").toLowerCase())}">${escapeHtml(order.status || "PLACED")}</span></div>
                        <p>${escapeHtml(date)} · ${escapeHtml(order.paymentMode || "Payment pending")}</p>
                        ${order.paymentStatus ? `<small>Payment ${escapeHtml(String(order.paymentStatus).toLowerCase())}</small>` : ""}
                    </div>
                    <div class="order-total"><strong>${formatPrice(order.totalPrice)}</strong>
                        ${canCancel ? `<button class="text-button" type="button" data-action="cancel-order" data-id="${escapeHtml(order.id)}">Cancel order</button>` : ""}
                    </div>
                    ${renderTrackingProgress(order.status)}
                </article>`;
        }).join("") : `<p class="empty-state">No orders yet. Your next favorite is out there.</p>`;
    } catch (error) {
        list.innerHTML = `<p class="empty-state">${escapeHtml(error.message)}</p>`;
    }
}

function renderTrackingProgress(status) {
    if (status === "CANCELLED") {
        return `<div class="tracking-cancelled">This order was cancelled.</div>`;
    }
    const stages = ["PLACED", "PAID", "PREPARING", "OUT_FOR_DELIVERY", "DELIVERED"];
    const currentIndex = stages.indexOf(status);
    return `<div class="tracking-progress" aria-label="Order progress">
        ${stages.map((stage, index) => `
            <div class="tracking-stage ${index < currentIndex ? "stage-complete" : ""} ${index === currentIndex ? "stage-current" : ""}">
                <span class="tracking-dot"></span><small>${stage.replaceAll("_", " ")}</small>
            </div>`).join("")}
    </div>`;
}

function handleOrderUpdate(order) {
    showToast(`Order #${order.id} updated: ${String(order.status || "").replaceAll("_", " ").toLowerCase()}.`);
    if (state.view === "orders") loadOrders();
}

app.addEventListener("click", async (event) => {
    const button = event.target.closest("[data-action]");
    if (!button) return;
    const { action, id } = button.dataset;

    if (action === "auth-mode") {
        state.authMode = button.dataset.mode;
        renderAuth();
    } else if (action === "view") {
        state.view = button.dataset.view;
        render();
        if (state.view === "orders") await loadOrders();
    } else if (action === "menu") {
        await loadMenu(id);
    } else if (action === "close-menu") {
        state.selectedRestaurant = null;
        state.menu = [];
        state.cart.clear();
        render();
    } else if (action === "add") {
        const item = state.menu.find((entry) => String(entry.id) === String(id));
        if (!item) return;
        const current = state.cart.get(item.id)?.quantity || 0;
        state.cart.set(item.id, { item, quantity: current + 1 });
        render();
    } else if (action === "remove") {
        const current = state.cart.get(Number(id));
        if (!current) return;
        if (current.quantity <= 1) state.cart.delete(current.item.id);
        else state.cart.set(current.item.id, { ...current, quantity: current.quantity - 1 });
        render();
    } else if (action === "checkout") {
        await placeOrder();
    } else if (action === "cancel-order") {
        await cancelOrder(id);
    } else if (action === "refresh-orders") {
        await loadOrders();
    } else if (action === "logout") {
        await signOut();
    }
});

sessionActions.addEventListener("click", async (event) => {
    const button = event.target.closest('[data-action="logout"]');
    if (button) await signOut();
});

app.addEventListener("submit", async (event) => {
    if (event.target.id !== "auth-form") return;
    event.preventDefault();
    const formData = new FormData(event.target);
    const email = String(formData.get("email")).trim();
    const password = String(formData.get("password"));

    try {
        if (state.authMode === "signup") {
            await api.signUp({
                name: String(formData.get("name")).trim(),
                email,
                password,
                address: String(formData.get("address")).trim()
            });
        }
        const session = await api.login(email, password);
        saveSession(session);
        state.user = session;
        liveOrders.connect();
        state.view = "restaurants";
        render();
        await loadRestaurants();
        showToast(state.authMode === "signup" ? "Welcome to FastServe!" : `Welcome back, ${session.name || "there"}!`);
    } catch (error) {
        showToast(error.message, true);
    }
});

async function placeOrder() {
    if (!state.cart.size) return;
    const paymentMode = document.querySelector("#payment-mode").value;
    const items = [...state.cart.values()].map(({ item, quantity }) => ({
        menuItemId: item.id,
        quantity
    }));
    try {
        const order = await api.createOrder(paymentMode, items);
        state.cart.clear();
        showToast(`Order #${order.id} placed successfully.`);
        state.view = "orders";
        render();
        await loadOrders();
    } catch (error) {
        showToast(error.message, true);
    }
}

async function cancelOrder(id) {
    try {
        await api.cancelOrder(id);
        showToast(`Order #${id} cancelled.`);
        await loadOrders();
    } catch (error) {
        showToast(error.message, true);
    }
}

async function signOut() {
    liveOrders.disconnect();
    const logoutRequest = api.logout();
    clearSession();
    state.user = null;
    state.cart.clear();
    state.selectedRestaurant = null;
    render();

    try {
        await logoutRequest;
    } catch (error) {
        showToast(error.message, true);
    }
}

if (hasSession()) {
    render();
    liveOrders.connect();
    loadRestaurants();
} else {
    renderAuth();
}
