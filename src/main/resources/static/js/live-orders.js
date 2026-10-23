export class LiveOrderSubscription {
    constructor({ getToken, onUpdate, onConnectionChange }) {
        this.getToken = getToken;
        this.onUpdate = onUpdate;
        this.onConnectionChange = onConnectionChange;
        this.socket = null;
        this.reconnectTimer = null;
        this.reconnectAttempt = 0;
        this.stopped = true;
    }

    connect() {
        if (!this.stopped || !this.getToken()) return;
        this.stopped = false;
        this.openSocket();
    }

    disconnect() {
        this.stopped = true;
        window.clearTimeout(this.reconnectTimer);
        this.reconnectTimer = null;
        if (this.socket) {
            this.socket.onclose = null;
            this.socket.close();
            this.socket = null;
        }
        this.onConnectionChange(false);
    }

    openSocket() {
        if (this.stopped) return;
        const token = this.getToken();
        if (!token) {
            this.disconnect();
            return;
        }

        const protocol = window.location.protocol === "https:" ? "wss:" : "ws:";
        const socket = new WebSocket(`${protocol}//${window.location.host}/ws`, ["v12.stomp"]);
        this.socket = socket;

        socket.onopen = () => {
            socket.send([
                "CONNECT",
                "accept-version:1.2",
                "Authorization:Bearer " + token,
                "",
                ""
            ].join("\n") + "\0");
        };

        socket.onmessage = (event) => this.handleFrame(event.data);
        socket.onerror = () => socket.close();
        socket.onclose = () => {
            if (this.socket === socket) this.socket = null;
            this.onConnectionChange(false);
            this.scheduleReconnect();
        };
    }

    handleFrame(data) {
        const frame = data.replace(/^\n+/, "").replace(/\0+$/, "");
        const separator = frame.indexOf("\n\n");
        if (separator < 0) return;

        const headerLines = frame.slice(0, separator).split("\n");
        const command = headerLines.shift();
        const body = frame.slice(separator + 2);
        if (command === "CONNECTED") {
            this.reconnectAttempt = 0;
            this.onConnectionChange(true);
            this.socket?.send([
                "SUBSCRIBE",
                "id:fastserve-orders",
                "destination:/user/queue/orders",
                "ack:auto",
                "",
                ""
            ].join("\n") + "\0");
        } else if (command === "MESSAGE" && body) {
            this.onUpdate(JSON.parse(body));
        } else if (command === "ERROR") {
            this.socket?.close();
        }
    }

    scheduleReconnect() {
        if (this.stopped || this.reconnectTimer) return;
        const delay = Math.min(1000 * (2 ** this.reconnectAttempt), 30000);
        this.reconnectAttempt += 1;
        this.reconnectTimer = window.setTimeout(() => {
            this.reconnectTimer = null;
            this.openSocket();
        }, delay);
    }
}
