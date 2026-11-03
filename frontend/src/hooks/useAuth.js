import { useSyncExternalStore } from "react";
import { AUTH_EVENT, getSessionSnapshot } from "../services/authStorage";

const subscribe = (listener) => {
    window.addEventListener(AUTH_EVENT, listener);
    window.addEventListener("storage", listener);
    return () => {
        window.removeEventListener(AUTH_EVENT, listener);
        window.removeEventListener("storage", listener);
    };
};
export default function useAuth() {
    const snapshot = useSyncExternalStore(subscribe, getSessionSnapshot, () => null);
    return snapshot ? JSON.parse(snapshot) : null;
}
