import { Navigate, Outlet, useLocation } from "react-router-dom";
import { useAdminAuth } from "../lib/AdminAuthContext";
import { Sidebar } from "./Sidebar";

export function AdminLayout() {
  const { loading, isAdmin } = useAdminAuth();
  const location = useLocation();

  if (loading) {
    return (
      <div className="login-shell">
        <div className="login-card" style={{ textAlign: "center" }}>
          <p style={{ color: "var(--ink-soft)" }}>Verifying admin permissions...</p>
        </div>
      </div>
    );
  }

  if (!isAdmin) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  return (
    <div className="app-shell">
      <Sidebar />
      <main className="content">
        <Outlet />
      </main>
    </div>
  );
}
