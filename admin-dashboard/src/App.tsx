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
  const { user, loading } = useAdminAuth();

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50">
        <div className="animate-spin rounded-full h-10 w-10 border-b-2 border-emerald-600"></div>
      </div>
    );
  }

  if (!user) {
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
