import { Navigate, Outlet } from "react-router-dom";
import { useAdminAuth } from "../lib/AdminAuthContext";
import { Sidebar } from "./Sidebar";

export function AdminLayout() {
  const { loading, isAdmin } = useAdminAuth();

  if (loading) {
    return <div className="empty-state">Loading…</div>;
  }
  if (!isAdmin) {
    return <Navigate to="/login" replace />;
  }

  return (
    <div className="app-shell">
      <Sidebar />
      <Outlet />
    </div>
  );
}
