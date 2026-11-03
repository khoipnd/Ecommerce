import { useEffect, useState } from "react";
import { Navigate, Outlet, useLocation } from "react-router-dom";
import useAuth from "../hooks/useAuth";
import { getCurrentAccount } from "../services/authService";
import Loading from "../components/common/Loading";

export default function ProtectedRoute({ admin = false, children }) {
    const session = useAuth();
    const token = session?.token;
    const location = useLocation();
    const [check, setCheck] = useState({});
    const [attempt, setAttempt] = useState(0);
    useEffect(() => {
        let active = true;
        if (token) getCurrentAccount() // GET /auth/me xác nhận phiên trước khi mở checkout, đơn hàng hoặc admin.
            .then(account => { if (active) setCheck({ token, account, attempt }); })
            .catch(error => { if (active) setCheck({ token, error, attempt }); });
        return () => { active = false; };
    }, [token, attempt]);
    if (!session) return <Navigate to={admin ? "/admin/login" : "/login"} replace state={{ from: location.pathname + location.search, checkoutState: location.state }} />;
    if (check.token !== token || check.attempt !== attempt) return <Loading />;
    if (check.error) return <div role="alert">{check.error.message} <button onClick={() => setAttempt(value => value + 1)}>Thử lại</button></div>;
    if (admin && check.account.role !== "ADMIN") return <Navigate to="/" replace />;
    return children || <Outlet />;
}
