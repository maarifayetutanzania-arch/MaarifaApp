import { Navigate, Outlet, useLocation } from "react-router-dom";
import { useAdminAuth } from "../lib/AdminAuthContext";
import { Sidebar } from "./Sidebar";

export function AdminLayout() {
  const { loading, isAdmin } = useAdminAuth();
  const location = useLocation();

  if (loading) {
    return (
      <div className="login-shell">
        <div className="login-card" style={{ textCenter: "center", alignItems: "center" }}>
          <p style={{ margin: 0, fontWeight: 500 }}>Inahakiki haki za Admin...</p>
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
      <main className="main">
        <Outlet />
      </main>
    </div>
  );
}
