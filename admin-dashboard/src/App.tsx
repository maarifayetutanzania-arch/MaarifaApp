import { BrowserRouter, Routes, Route, Navigate, Outlet } from "react-router-dom";
import { AdminAuthProvider, useAdminAuth } from "./lib/AdminAuthContext";
import { AdminLayout } from "./components/AdminLayout";
import { LoginPage } from "./pages/LoginPage";
import { DashboardHome } from "./pages/DashboardHome";
import { TeachersPage } from "./pages/TeachersPage";
import { ContentPage } from "./pages/ContentPage";
import { SubscriptionsPage } from "./pages/SubscriptionsPage";
import { PayoutsPage } from "./pages/PayoutsPage";

// Kizuizi cha kuhakikisha Admin tu ndiye anayeingia (Protected Route)
function ProtectedRoute() {
  const { firebaseUser, isAdmin, loading } = useAdminAuth();

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50 p-4">
        <div className="bg-white p-8 rounded-2xl shadow-sm border border-gray-100 flex flex-col items-center gap-4">
          <div className="w-10 h-10 border-4 border-emerald-200 border-t-emerald-600 rounded-full animate-spin"></div>
          <p className="text-sm font-medium text-gray-500">Inahakiki haki za Admin...</p>
        </div>
      </div>
    );
  }

  if (!firebaseUser || !isAdmin) {
    return <Navigate to="/login" replace />;
  }

  return <Outlet />;
}

export default function App() {
  return (
    <AdminAuthProvider>
      <BrowserRouter>
        <Routes>
          {/* Public Route */}
          <Route path="/login" element={<LoginPage />} />

          {/* Protected Admin Routes */}
          <Route element={<ProtectedRoute />}>
            <Route element={<AdminLayout />}>
              <Route path="/" element={<DashboardHome />} />
              <Route path="/teachers" element={<TeachersPage />} />
              <Route path="/content" element={<ContentPage />} />
              <Route path="/subscriptions" element={<SubscriptionsPage />} />
              <Route path="/payouts" element={<PayoutsPage />} />
            </Route>
          </Route>

          {/* Catch-all 404 Route */}
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </BrowserRouter>
    </AdminAuthProvider>
  );
}
