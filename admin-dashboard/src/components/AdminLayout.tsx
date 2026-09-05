import { Navigate, Outlet, useLocation } from "react-router-dom";
import { useAdminAuth } from "../lib/AdminAuthContext";
import { Sidebar } from "./Sidebar";

export function AdminLayout() {
  const { loading, isAdmin } = useAdminAuth();
  const location = useLocation();

  if (loading) {
    return (
      <div className="h-screen w-full bg-gray-50 flex items-center justify-center p-4">
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
    <div className="h-screen w-full bg-gray-50 flex overflow-hidden">
      {/* Sidebar navigation */}
      <Sidebar />

      {/* Main content body (Marekebisho ya kuzuia kuvimba kwa screen) */}
      <main className="flex-1 min-w-0 h-full overflow-y-auto p-4 sm:p-6 lg:p-8">
        <div className="max-w-7xl mx-auto w-full">
          <Outlet />
        </div>
      </main>
    </div>
  );
}
