import { Navigate, Outlet, useLocation } from "react-router-dom";
import { useAdminAuth } from "../lib/AdminAuthContext";
import { Sidebar } from "./Sidebar";

export function AdminLayout() {
  const { loading, isAdmin } = useAdminAuth();
  const location = useLocation();

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center p-4">
        <div className="bg-white p-8 rounded-2xl shadow-sm border border-gray-100 flex flex-col items-center gap-4">
          <div className="w-10 h-10 border-4 border-emerald-200 border-t-emerald-600 rounded-full animate-spin"></div>
          <p className="text-sm font-medium text-gray-500">
            Inahakiki haki za Admin...
          </p>
        </div>
      </div>
    );
  }

  if (!isAdmin) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  return (
    <div className="min-h-screen bg-gray-50 flex">
      {/* Sidebar navigation */}
      <Sidebar />

      {/* Main content body */}
      <main className="flex-1 min-w-0 p-6 md:p-8 overflow-y-auto">
        <div className="max-w-7xl mx-auto">
          <Outlet />
        </div>
      </main>
    </div>
  );
}
