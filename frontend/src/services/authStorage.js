const SESSION_KEY = "rest_auth_session";
export const AUTH_EVENT = "rest-auth-changed";

export const readSession = () => {
    try {
        const session = JSON.parse(sessionStorage.getItem(SESSION_KEY));
        return session?.token && session.expiresAt > Date.now() ? session : null;
    } catch { return null; }
};
export const saveSession = (data) => {
    sessionStorage.setItem(SESSION_KEY, JSON.stringify({ ...data, expiresAt: Date.now() + data.expiresInSeconds * 1000 }));
    localStorage.removeItem("admin_auth");
    localStorage.removeItem("userEmail");
    window.dispatchEvent(new Event(AUTH_EVENT));
};
export const clearSession = () => {
    sessionStorage.removeItem(SESSION_KEY);
    localStorage.removeItem("admin_auth");
    localStorage.removeItem("userEmail");
    window.dispatchEvent(new Event(AUTH_EVENT));
};
export const getSessionSnapshot = () => readSession() ? sessionStorage.getItem(SESSION_KEY) : null;
